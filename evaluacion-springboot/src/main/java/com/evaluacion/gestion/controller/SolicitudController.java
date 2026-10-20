package com.evaluacion.gestion.controller;

import com.evaluacion.gestion.dto.request.SolicitudRequest;
import com.evaluacion.gestion.dto.response.SolicitudResponse;
import com.evaluacion.gestion.entity.EstadoSolicitud;
import com.evaluacion.gestion.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(request));
    }

    /**
     * Lista paginada. Filtro opcional por estado.
     * Ejemplo: GET /solicitudes?estado=PENDIENTE&page=0&size=5&sort=fechaCreacion,desc
     */
    @GetMapping
    public PagedModel<SolicitudResponse> listar(
            @RequestParam(required = false) EstadoSolicitud estado,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PagedModel<>(solicitudService.listar(estado, pageable));
    }

    @GetMapping("/{id}")
    public SolicitudResponse buscarPorId(@PathVariable Integer id) {
        return solicitudService.buscarPorId(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<SolicitudResponse> listarPorCliente(@PathVariable Integer clienteId) {
        return solicitudService.listarPorCliente(clienteId);
    }

    /** Solicitudes atendidas por un empleado. Ejemplo: GET /solicitudes/empleado/2 */
    @GetMapping("/empleado/{empleadoId}")
    public List<SolicitudResponse> listarPorEmpleado(@PathVariable Integer empleadoId) {
        return solicitudService.listarPorEmpleado(empleadoId);
    }

    /** Cuántas solicitudes hay en cada estado, contadas en la base de datos. */
    @GetMapping("/conteo")
    public Map<String, Long> contarPorEstado() {
        return solicitudService.contarPorEstado();
    }

    // ============ Estructuras de datos: cola, pila y lista ============

    /** Solicitudes pendientes en orden de llegada (cola FIFO). */
    @GetMapping("/cola")
    public List<SolicitudResponse> verCola() {
        return solicitudService.verCola();
    }

    /** Atiende la siguiente solicitud de la cola. Ejemplo: POST /solicitudes/procesar?empleadoId=2 */
    @PostMapping("/procesar")
    public SolicitudResponse procesarSiguiente(@RequestParam Integer empleadoId) {
        return solicitudService.procesarSiguiente(empleadoId);
    }

    /** Atiende todas las solicitudes pendientes de la cola. */
    @PostMapping("/procesar-todas")
    public List<SolicitudResponse> procesarTodas(@RequestParam Integer empleadoId) {
        return solicitudService.procesarTodas(empleadoId);
    }

    /** Historial de atendidas, la más reciente primero (pila LIFO). */
    @GetMapping("/historial")
    public List<SolicitudResponse> verHistorial() {
        return solicitudService.verHistorial();
    }

    /** Cima de la pila: la última solicitud atendida. */
    @GetMapping("/historial/ultima")
    public ResponseEntity<SolicitudResponse> ultimaAtendida() {
        SolicitudResponse ultima = solicitudService.ultimaAtendida();
        if (ultima == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ultima);
    }

    /** Ordenamiento manual: criterio = prioridad (burbuja), fecha (inserción) o id (selección). */
    @GetMapping("/ordenadas")
    public List<SolicitudResponse> ordenar(@RequestParam(defaultValue = "prioridad") String criterio) {
        return solicitudService.ordenar(criterio);
    }

    /** Búsqueda lineal por texto en la descripción o el nombre del cliente. */
    @GetMapping("/buscar")
    public List<SolicitudResponse> buscarPorTexto(@RequestParam String texto) {
        return solicitudService.buscarPorTexto(texto);
    }

    /** Búsqueda binaria por id sobre la lista en memoria. */
    @GetMapping("/buscar-binaria/{id}")
    public SolicitudResponse buscarBinaria(@PathVariable Integer id) {
        return solicitudService.buscarEnMemoriaPorId(id);
    }

    /** Totales por estado y por prioridad. */
    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return solicitudService.resumen();
    }
}