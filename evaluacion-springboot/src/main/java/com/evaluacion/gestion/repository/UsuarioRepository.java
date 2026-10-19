package com.evaluacion.gestion.repository;

import com.evaluacion.gestion.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de todos los usuarios (Administrador, Empleado y Cliente).
 * JpaRepository ya trae save, findAll, findById, deleteById, count y
 * findAll(Pageable) para la paginación y el ordenamiento.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Spring genera la consulta a partir del nombre del método
    boolean existsByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);
}
