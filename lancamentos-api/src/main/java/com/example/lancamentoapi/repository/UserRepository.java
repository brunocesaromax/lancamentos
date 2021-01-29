package com.example.lancamentoapi.repository;

import com.example.lancamentoapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{

    //Não precisa verificar se é diferente de null, por isso Optional
    Optional<User> findByEmail(String email);

    List<User> findByPermissionsDescription(String permission);
}
