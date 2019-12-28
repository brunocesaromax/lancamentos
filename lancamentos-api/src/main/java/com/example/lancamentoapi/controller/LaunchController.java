package com.example.lancamentoapi.controller;

import com.example.lancamentoapi.event.ResourceCreatedEvent;
import com.example.lancamentoapi.exceptionHandler.LaunchExceptionHandler.Error;
import com.example.lancamentoapi.model.Launch;
import com.example.lancamentoapi.repository.filter.LaunchFilter;
import com.example.lancamentoapi.repository.projection.LaunchSummary;
import com.example.lancamentoapi.service.LaunchService;
import com.example.lancamentoapi.service.exception.PersonInexistentOrInactiveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/launchs")
public class LaunchController {

    @Autowired
    private LaunchService launchService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SEARCH_LAUNCH') and #oauth2.hasScope('read')")
    public Page<Launch> list(@RequestBody(required = false) LaunchFilter launchFilter,
                             Pageable pageable) {
        return launchService.findAll(launchFilter, pageable);
    }

    @GetMapping(params = "summary")// Parametro de decisão para escolher este endpoint
    @PreAuthorize("hasAuthority('ROLE_SEARCH_LAUNCH') and #oauth2.hasScope('read')")
    public Page<LaunchSummary> sumUp(@RequestBody(required = false) LaunchFilter launchFilter,
                                     Pageable pageable) {
        return launchService.sumUp(launchFilter, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SEARCH_LAUNCH') and #oauth2.hasScope('read')")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return launchService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE_LAUNCH') and #oauth2.hasScope('write')")
    public ResponseEntity<Launch> save(@Valid @RequestBody Launch launch, HttpServletResponse response) {

        Launch launchSave = launchService.save(launch);

        publisher.publishEvent(new ResourceCreatedEvent(this, response, launchSave.getId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(launchSave);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Sucesso porém sem conteúdo
    @PreAuthorize("hasAuthority('ROLE_REMOVE_LAUNCH') and #oauth2.hasScope('write')")
    public void remove(@PathVariable Long id) {
        launchService.delete(id);
    }

    /*Como é um tratamento particular de Lançamento pode ser tratado no próprio controlador*/
    @ExceptionHandler({PersonInexistentOrInactiveException.class})
    public ResponseEntity<Object> handlePersonInexistentOrInactiveException(PersonInexistentOrInactiveException ex) {

        String msgUser = messageSource.getMessage("person.inesistent-or-inactive", null, LocaleContextHolder.getLocale());
        String msgDev = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
        List<Error> errors = Collections.singletonList(new Error(msgUser, msgDev));

        return ResponseEntity.badRequest().body(errors);
    }

}
