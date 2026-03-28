package com.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameAndActivoTrueAndEliminadoFalse(String username);

    Optional<Usuario> findByUsernameAndEliminadoFalse(String username);

    Optional<Usuario> findByIdAndEliminadoFalse(Long id);

    List<Usuario> findAllByEliminadoFalseOrderByIdAsc();

    List<Usuario> findAllByActivoAndEliminadoFalseOrderByIdAsc(Boolean activo);

    boolean existsByUsernameIgnoreCaseAndEliminadoFalseAndIdNot(String username, Long id);

    boolean existsByUsernameIgnoreCaseAndEliminadoFalse(String username);

    long countByRolNombreIgnoreCaseAndActivoTrueAndEliminadoFalse(String rolNombre);
}
