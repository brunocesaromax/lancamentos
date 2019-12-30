package com.example.lancamentoapi.controller;

import com.example.lancamentoapi.event.ResourceCreatedEvent;
import com.example.lancamentoapi.model.Person;
import com.example.lancamentoapi.repository.PersonRepository;
import com.example.lancamentoapi.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/persons")
public class PersonController {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PersonService personService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SEARCH_PERSON') and #oauth2.hasScope('read')")
    public List<Person> list() {

        return personRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE_PERSON') and #oauth2.hasScope('write')")
    public ResponseEntity<Person> save(@Valid @RequestBody Person person, HttpServletResponse response) {

        Person personSave = personRepository.save(person);

        publisher.publishEvent(new ResourceCreatedEvent(this, response, personSave.getId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(personSave);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SEARCH_PERSON') and #oauth2.hasScope('read')")
    public ResponseEntity<?> findById(@PathVariable Long id) {

        Optional<Person> person = personRepository.findById(id);

        if (person.isPresent()) {
            return ResponseEntity.ok(person.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Sucesso porém sem conteúdo
    @PreAuthorize("hasAuthority('ROLE_REMOVE_PERSON') and #oauth2.hasScope('write')")
    public void remover(@PathVariable Long id) {
            personRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE_PERSON') and #oauth2.hasScope('write')")
    public ResponseEntity<Person> update(@PathVariable Long id, @Valid @RequestBody Person person) {

        Person personBD = personService.update(id, person);
        return ResponseEntity.ok(personBD);
    }

    @PutMapping("/{id}/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_UPDATE_PERSON') and #oauth2.hasScope('write')")
    public void UpdateFieldActive(@PathVariable Long id, @RequestParam Boolean active) {
        personService.updateFieldActive(id, active);
    }

}