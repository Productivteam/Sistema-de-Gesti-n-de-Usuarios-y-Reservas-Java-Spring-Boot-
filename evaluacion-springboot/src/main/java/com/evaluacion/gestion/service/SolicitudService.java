package com.evaluacion.gestion.service;

import com.evaluacion.gestion.dto.request.SolicitudRequest;
import com.evaluacion.gestion.dto.response.SolicitudResponse;
import com.evaluacion.gestion.entity.Cliente;
import com.evaluacion.gestion.entity.Empleado;
import com.evaluacion.gestion.entity.EstadoSolicitud;
import com.evaluacion.gestion.entity.Prioridad;
import com.evaluacion.gestion.entity.Solicitud;
import com.evaluacion.gestion.estructuras.GestorSolicitudes;
import com.evaluacion.gestion.repository.ClienteRepository;
import com.evaluacion.gestion.repository.EmpleadoRepository;
import com.evaluacion.gestion.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final GestorSolicitudes gestorSolicitudes;

    @Transactional
    public SolicitudResponse crear(SolicitudRequest request) {
        // Solo un CLIENTE puede crear solicitudes: si el id es de un empleado o admin no se encuentra
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un cliente con id " + request.getClienteId()));

        Prioridad prioridad = request.getPrioridad();
        if (prioridad == null) {
            prioridad = Prioridad.MEDIA;
        }

        Solicitud solicitud = new Solicitud(request.getDescripcion().trim(), prioridad);
        solicitud.setCliente(cliente);
        Solicitud guardada = solicitudRepository.save(solicitud);

        // Además de la base de datos, la solicitud entra a la lista y a la cola
        gestorSolicitudes.registrar(guardada);
        return SolicitudResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudResponse> listar(EstadoSolicitud estado, Pageable pageable) {
        Page<Solicitud> pagina;
        if (estado == null) {
            pagina = solicitudRepository.findAll(pageable);
        } else {
            pagina = solicitudRepository.findByEstado(estado, pageable);
        }
        return pagina.map(SolicitudResponse::desde);
    }

    @Transactional(readOnly = true)
    public SolicitudResponse buscarPorId(Integer id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una solicitud con id " + id));
        return SolicitudResponse.desde(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarPorCliente(Integer clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un cliente con id " + clienteId);
        }
        return SolicitudResponse.desde(solicitudRepository.findByClienteId(clienteId));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarPorEmpleado(Integer empleadoId) {
        if (!empleadoRepository.existsById(empleadoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un empleado con id " + empleadoId);
        }
        return SolicitudResponse.desde(solicitudRepository.findByEmpleadoAsignadoId(empleadoId));
    }

    /**
     * Cuenta en la base de datos cuántas solicitudes hay en cada estado.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> contarPorEstado() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            conteo.put(estado.name(), solicitudRepository.countByEstado(estado));
        }
        return conteo;
    }

    // ============ Procesamiento con cola y pila (GestorSolicitudes) ============

    /**
     * Atiende la primera solicitud de la cola (FIFO) y la guarda en el historial (pila).
     */
    @Transactional
    public SolicitudResponse procesarSiguiente(Integer empleadoId) {
        Empleado empleado = buscarEmpleado(empleadoId);
        return SolicitudResponse.desde(atenderSiguiente(empleado));
    }

    /**
     * Atiende todas las solicitudes de la cola, una por una, mientras queden pendientes.
     */
    @Transactional
    public List<SolicitudResponse> procesarTodas(Integer empleadoId) {
        Empleado empleado = buscarEmpleado(empleadoId);
        List<Solicitud> atendidas = new ArrayList<>();

        while (gestorSolicitudes.hayPendientes()) {
            atendidas.add(atenderSiguiente(empleado));
        }
        return SolicitudResponse.desde(atendidas);
    }

    private Solicitud atenderSiguiente(Empleado empleado) {
        Solicitud siguiente = gestorSolicitudes.tomarSiguiente();
        if (siguiente == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay solicitudes pendientes en la cola");
        }

        siguiente.atender(empleado);
        solicitudRepository.save(siguiente);
        gestorSolicitudes.registrarEnHistorial(siguiente);
        return siguiente;
    }

    private Empleado buscarEmpleado(Integer empleadoId) {
        return empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un empleado con id " + empleadoId));
    }

    public List<SolicitudResponse> verCola() {
        return SolicitudResponse.desde(gestorSolicitudes.verCola());
    }

    public List<SolicitudResponse> verHistorial() {
        return SolicitudResponse.desde(gestorSolicitudes.verHistorial());
    }

    /**
     * Mira la primera solicitud de la cola SIN sacarla (peek). Devuelve null si la cola está vacía.
     */
    public SolicitudResponse verSiguienteEnCola() {
        Solicitud siguiente = gestorSolicitudes.verSiguiente();
        if (siguiente == null) {
            return null;
        }
        return SolicitudResponse.desde(siguiente);
    }

    /**
     * Cima de la pila. Devuelve null si todavía no se ha atendido ninguna.
     */
    public SolicitudResponse ultimaAtendida() {
        Solicitud ultima = gestorSolicitudes.ultimaAtendida();
        if (ultima == null) {
            return null;
        }
        return SolicitudResponse.desde(ultima);
    }

    public List<SolicitudResponse> ordenar(String criterio) {
        try {
            return SolicitudResponse.desde(gestorSolicitudes.ordenar(criterio));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public SolicitudResponse buscarEnMemoriaPorId(Integer id) {
        Solicitud encontrada = gestorSolicitudes.buscarPorIdBinaria(id);
        if (encontrada == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "La búsqueda binaria no encontró la solicitud con id " + id);
        }
        return SolicitudResponse.desde(encontrada);
    }

    public List<SolicitudResponse> buscarPorTexto(String texto) {
        try {
            return SolicitudResponse.desde(gestorSolicitudes.buscarPorTexto(texto));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Map<String, Object> resumen() {
        return gestorSolicitudes.resumen();
    }
}