package com.evaluacion.gestion.config;

import com.evaluacion.gestion.entity.Solicitud;
import com.evaluacion.gestion.estructuras.GestorSolicitudes;
import com.evaluacion.gestion.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Se ejecuta una vez al arrancar la aplicación.
 * Carga en el GestorSolicitudes (lista, cola y pila) las solicitudes que ya
 * estaban guardadas en la base de datos. Sin esto, al reiniciar la aplicación
 * la cola quedaría vacía aunque en MySQL haya solicitudes pendientes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CargaInicialDatos implements CommandLineRunner {

    private final SolicitudRepository solicitudRepository;
    private final GestorSolicitudes gestorSolicitudes;

    @Override
    public void run(String... args) {
        cargarSolicitudesEnGestor();
    }

    private void cargarSolicitudesEnGestor() {
        gestorSolicitudes.limpiar();

        // Ordenadas por id para que la cola respete el orden de llegada
        List<Solicitud> guardadas = solicitudRepository.findAll(Sort.by("id"));
        for (Solicitud solicitud : guardadas) {
            gestorSolicitudes.registrar(solicitud);
        }
        log.info("GestorSolicitudes cargado con {} solicitudes ({} en cola)",
                guardadas.size(), gestorSolicitudes.verCola().size());
    }
}
