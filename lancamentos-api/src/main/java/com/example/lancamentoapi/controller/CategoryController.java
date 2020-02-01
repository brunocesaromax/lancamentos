package com.example.lancamentoapi.controller;

import com.example.lancamentoapi.event.ResourceCreatedEvent;
import com.example.lancamentoapi.model.Category;
import com.example.lancamentoapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher publisher;

//    @CrossOrigin(maxAge = 10) // Permitir que todas origens consiguem fazer essa requisição
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SEARCH_CATEGORY') and #oauth2.hasScope('read')")
    public List<Category> list() {

        return categoryRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE_CATEGORY') and #oauth2.hasScope('write')")
    public ResponseEntity<Category> save(@Valid @RequestBody Category category, HttpServletResponse response) {

        Category categorySave = categoryRepository.save(category);

        publisher.publishEvent(new ResourceCreatedEvent(this, response, categorySave.getId()));
        
        /*Retornando objeto criado para o cliente, facilita o desenvolvimento da parte cliente*/
        return ResponseEntity.status(HttpStatus.CREATED).body(categorySave);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SEARCH_CATEGORY') and #oauth2.hasScope('read')")
    public ResponseEntity<?> findById(@PathVariable Long id){

        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()){
            return ResponseEntity.ok(category.get());
        }

        return ResponseEntity.notFound().build();
    }
}
