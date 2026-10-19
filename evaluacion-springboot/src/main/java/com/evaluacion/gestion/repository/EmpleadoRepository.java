package com.evaluacion.gestion.repository;

import com.evaluacion.gestion.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Solo devuelve filas de la tabla usuarios cuyo tipo es EMPLEADO.
 */
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
}
