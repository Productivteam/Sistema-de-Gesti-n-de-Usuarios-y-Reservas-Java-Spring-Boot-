package com.evaluacion.gestion.service;

import com.evaluacion.gestion.dto.request.SolicitudRequest;
import com.evaluacion.gestion.dto.request.UsuarioRequest;
import com.evaluacion.gestion.dto.response.SolicitudResponse;
import com.evaluacion.gestion.dto.response.UsuarioResponse;
import com.evaluacion.gestion.entity.Prioridad;
import com.evaluacion.gestion.estructuras.GestorSolicitudes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración: servicios + base de datos H2 + GestorSolicitudes.
 */
@SpringBootTest
class SolicitudServiceTest {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GestorSolicitudes gestorSolicitudes;

    private UsuarioResponse cliente;
    private UsuarioResponse empleado;

    @BeforeEach
    void preparar() {
        gestorSolicitudes.limpiar();
        String sufijo = String.valueOf(System.nanoTime());

        cliente = usuarioService.crear(UsuarioRequest.builder()
                .tipo("CLIENTE").nombre("Cliente Servicio").correo("cliente" + sufijo + "@correo.com")
                .build());

        empleado = usuarioService.crear(UsuarioRequest.builder()
                .tipo("EMPLEADO").nombre("Empleado Servicio").correo("empleado" + sufijo + "@empresa.com")
                .cargo("Soporte").salario(2000000.0)
                .build());
    }

    @Test
    void crearUsuarioDevuelveTipoYDescripcion() {
        assertEquals("CLIENTE", cliente.getTipo());
        assertEquals(0, cliente.getPuntosFidelidad());                   // puntos por defecto
        assertTrue(cliente.getDescripcion().startsWith("Cliente"));     // polimorfismo
        assertEquals("EMPLEADO", empleado.getTipo());
        assertEquals("Soporte", usuarioService.buscarPorId(empleado.getId()).getCargo());
    }

    @Test
    void crearSolicitudLaGuardaYLaPoneEnLaCola() {
        SolicitudResponse creada = solicitudService.crear(
                new SolicitudRequest(cliente.getId(), "Necesito ayuda urgente", null));

        assertNotNull(creada.getId());
        assertEquals("MEDIA", creada.getPrioridad());                    // prioridad por defecto
        assertEquals("PENDIENTE", creada.getEstado());
        assertEquals(1, solicitudService.verCola().size());
        assertEquals(creada.getId(), solicitudService.buscarPorId(creada.getId()).getId());
        assertEquals(1, solicitudService.listarPorCliente(cliente.getId()).size());
    }

    @Test
    void procesarRespetaElOrdenDeLlegadaYLlenaElHistorial() {
        SolicitudResponse primera = solicitudService.crear(
                new SolicitudRequest(cliente.getId(), "Primera solicitud", Prioridad.BAJA));
        SolicitudResponse segunda = solicitudService.crear(
                new SolicitudRequest(cliente.getId(), "Segunda solicitud", Prioridad.ALTA));

        SolicitudResponse atendida = solicitudService.procesarSiguiente(empleado.getId());
        assertEquals(primera.getId(), atendida.getId());
        assertEquals("ATENDIDA", solicitudService.buscarPorId(primera.getId()).getEstado());

        List<SolicitudResponse> resto = solicitudService.procesarTodas(empleado.getId());
        assertEquals(1, resto.size());
        assertEquals(segunda.getId(), solicitudService.ultimaAtendida().getId());
        assertEquals(2, solicitudService.verHistorial().size());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> solicitudService.procesarSiguiente(empleado.getId()));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void soloUnClientePuedeCrearSolicitudes() {
        SolicitudRequest conIdDeEmpleado = new SolicitudRequest(empleado.getId(), "Solicitud inválida", Prioridad.ALTA);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> solicitudService.crear(conIdDeEmpleado));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void noSePermitenCorreosRepetidos() {
        UsuarioRequest repetido = UsuarioRequest.builder()
                .tipo("CLIENTE").nombre("Otro").correo(cliente.getCorreo().toUpperCase())
                .build();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.crear(repetido));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void tipoDeUsuarioInvalidoEsRechazado() {
        UsuarioRequest invalido = UsuarioRequest.builder()
                .tipo("GERENTE").nombre("Alguien").correo("alguien" + System.nanoTime() + "@correo.com")
                .build();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.crear(invalido));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }
}
