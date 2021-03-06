package com.example.lancamentoapi.service;

import com.example.lancamentoapi.dto.LaunchStatisticByDay;
import com.example.lancamentoapi.dto.LaunchStatisticCategory;
import com.example.lancamentoapi.dto.LaunchStatisticPerson;
import com.example.lancamentoapi.mail.Mailer;
import com.example.lancamentoapi.model.Launch;
import com.example.lancamentoapi.model.Launch_;
import com.example.lancamentoapi.model.Person;
import com.example.lancamentoapi.model.User;
import com.example.lancamentoapi.repository.LaunchRepository;
import com.example.lancamentoapi.repository.UserRepository;
import com.example.lancamentoapi.repository.filter.LaunchFilter;
import com.example.lancamentoapi.repository.projection.LaunchSummary;
import com.example.lancamentoapi.service.exception.PersonInexistentOrInactiveException;
import com.example.lancamentoapi.storage.S3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LaunchService {

    private static final String RECIPIENTS = "ROLE_SEARCH_LAUNCH";

    private final LaunchRepository launchRepository;
    private final UserRepository userRepository;

    private final Mailer mailer;
    private final S3 s3;

    private PersonService personService;

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public Page<Launch> findAll(LaunchFilter launchFilter, Pageable pageable) {
        return launchRepository.filterOut(launchFilter, pageable);
    }

    public Optional<Launch> findById(Long id) {
        return launchRepository.findById(id);
    }

    public Launch save(Launch launch) {
        validatePerson(launch.getPerson());

        if (StringUtils.hasText(launch.getAttachment())) {
            s3.save(launch.getAttachment());
        }

        return launchRepository.save(launch);
    }

    public void delete(Long id) {
        launchRepository.deleteById(id);
    }

    public Page<LaunchSummary> sumUp(LaunchFilter launchFilter, Pageable pageable) {
        return launchRepository.sumUp(launchFilter, pageable);
    }

    public Launch update(Long id, Launch launch) {
        Launch launchBD = findExistentLaunch(id);

        if (!launch.getPerson().equals(launchBD.getPerson())) {
            validatePerson(launch.getPerson());
        }

        if (StringUtils.isEmpty(launch.getAttachment())
            && StringUtils.hasText(launchBD.getAttachment())) {
            s3.delete(launchBD.getAttachment());

        } else if (StringUtils.hasText(launch.getAttachment())
                   && !launch.getAttachment().equals(launchBD.getAttachment())) {
            s3.update(launchBD.getAttachment(), launch.getAttachment());
        }

        BeanUtils.copyProperties(launch, launchBD, Launch_.ID);
        return launchRepository.save(launchBD);
    }

    private void validatePerson(Person person) {
        if (Optional.ofNullable(person).map(Person::getId).isPresent()) {
            person = personService.findById(person.getId());
        }

        if (!Optional.ofNullable(person).isPresent() || person.isInactive()) {
            throw new PersonInexistentOrInactiveException();
        }
    }

    private Launch findExistentLaunch(Long id) {
        Optional<Launch> launchBD = launchRepository.findById(id);

        if (!launchBD.isPresent()) {
            throw new IllegalArgumentException();
        } else {
            return launchBD.get();
        }
    }

    public byte[] reportByPerson(LocalDate start, LocalDate end) throws JRException {
        List<LaunchStatisticPerson> result = launchRepository.findByPerson(start, end);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("DT_BEGIN", Date.valueOf(start));
        parameters.put("DT_END", Date.valueOf(end));
        parameters.put("REPORT_LOCALE", new Locale("pt", "BR"));

        InputStream inputStream = this.getClass().getResourceAsStream("/reports/launchs-by-person.jasper");

        JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parameters, new JRBeanCollectionDataSource(result));

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    //    @Scheduled(fixedDelay = 1000 * 60 * 30) //Evita enfileiramento de execuções do método, somente quando uma execução termina que outra começa
    @Scheduled(cron = "0 0 6 * * *") // SS MM HH DAY_OF_MONTH MONTH DAY_OF_WEEK
    public void alertOverdueLaunchs() {
        if (log.isDebugEnabled()) {
            log.debug("Preparando envio de e-mails de aviso de lançamentos vencidos.");
        }

        List<Launch> overdueLaunchs = launchRepository.findByDueDateLessThanEqualAndPaydayIsNull(LocalDate.now());

        if (overdueLaunchs.isEmpty()) {
            log.info("Sem lançamentos vencidos para aviso.");
            return;
        }

        log.info("Existem {} lançamentos vencidos.", overdueLaunchs.size());

        List<User> users = userRepository.findByPermissionsDescription(RECIPIENTS);

        if (users.isEmpty()) {
            log.warn("Existem lançamentos vencidos, mas o sistema não encontrou destinatários.");
            return;
        }

        mailer.alertOverdueLaunchs(overdueLaunchs, users);

        log.info("Envio de e-mails de aviso concluído.");
    }

    @Transactional(readOnly = true)
    public boolean existsWithPersonId(Long id) {
        return launchRepository.existsByPersonId(id);
    }

    @Transactional(readOnly = true)
    public List<LaunchStatisticCategory> findByCategory(LocalDate date) {
        return launchRepository.findByCategory(date);
    }

    @Transactional(readOnly = true)
    public List<LaunchStatisticByDay> findByDay(LocalDate date) {
        return launchRepository.findByDay(date);
    }
}
