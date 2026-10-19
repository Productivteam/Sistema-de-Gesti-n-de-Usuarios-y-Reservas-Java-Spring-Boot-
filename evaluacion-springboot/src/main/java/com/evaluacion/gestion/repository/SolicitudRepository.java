package com.evaluacion.gestion.repository;

import com.evaluacion.gestion.entity.EstadoSolicitud;
import com.evaluacion.gestion.entity.Solicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {

    // Todas las solicitudes de un cliente (relación Cliente -> Solicitudes)
    List<Solicitud> findByClienteId(Integer clienteId);

    // Solicitudes filtradas por estado, con paginación
    Page<Solicitud> findByEstado(EstadoSolicitud estado, Pageable pageable);
}
