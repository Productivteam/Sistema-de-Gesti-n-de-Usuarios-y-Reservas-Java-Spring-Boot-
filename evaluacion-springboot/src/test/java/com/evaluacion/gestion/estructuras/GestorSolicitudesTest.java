package com.evaluacion.gestion.estructuras;

import com.evaluacion.gestion.entity.Cliente;
import com.evaluacion.gestion.entity.Empleado;
import com.evaluacion.gestion.entity.Prioridad;
import com.evaluacion.gestion.entity.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la lista, la cola, la pila y los algoritmos (sin base de datos).
 */
class GestorSolicitudesTest {

    private GestorSolicitudes gestor;
    private final Empleado empleado = new Empleado("Pedro", "pedro@empresa.com", "Soporte", 2000000.0);

    @BeforeEach
    void preparar() {
        gestor = new GestorSolicitudes();
        Cliente cliente = new Cliente("Laura Gómez", "laura@correo.com", 0);
        LocalDateTime base = LocalDateTime.of(2026, 10, 1, 8, 0);

        // Se registran en desorden de id para probar el ordenamiento
        gestor.registrar(crear(3, "Error al pagar factura", Prioridad.BAJA, base.plusMinutes(10), cliente));
        gestor.registrar(crear(1, "No puedo iniciar sesión", Prioridad.MEDIA, base.plusMinutes(30), cliente));
        gestor.registrar(crear(2, "Servicio caído", Prioridad.ALTA, base.plusMinutes(20), cliente));
    }

    private Solicitud crear(Integer id, String descripcion, Prioridad prioridad, LocalDateTime fecha, Cliente cliente) {
        Solicitud solicitud = new Solicitud(descripcion, prioridad);
        solicitud.setId(id);
        solicitud.setFechaCreacion(fecha);
        solicitud.setCliente(cliente);
        return solicitud;
    }

    @Test
    void laColaAtiendeEnOrdenDeLlegada() {
        assertEquals(3, gestor.tomarSiguiente().getId());
        assertEquals(1, gestor.tomarSiguiente().getId());
        assertEquals(2, gestor.tomarSiguiente().getId());
        assertFalse(gestor.hayPendientes());
        assertNull(gestor.tomarSiguiente());
    }

    @Test
    void laPilaMuestraPrimeroLaUltimaAtendida() {
        while (gestor.hayPendientes()) {
            Solicitud siguiente = gestor.tomarSiguiente();
            siguiente.atender(empleado);
            gestor.registrarEnHistorial(siguiente);
        }

        List<Solicitud> historial = gestor.verHistorial();
        assertEquals(List.of(2, 1, 3), historial.stream().map(Solicitud::getId).toList());
        assertEquals(2, gestor.ultimaAtendida().getId());
    }

    @Test
    void ordenaPorPrioridadConBurbuja() {
        List<Solicitud> ordenadas = gestor.ordenar("prioridad");
        assertEquals(List.of(Prioridad.ALTA, Prioridad.MEDIA, Prioridad.BAJA),
                ordenadas.stream().map(Solicitud::getPrioridad).toList());
    }

    @Test
    void ordenaPorFechaConInsercion() {
        List<Solicitud> ordenadas = gestor.ordenar("fecha");
        assertEquals(List.of(3, 2, 1), ordenadas.stream().map(Solicitud::getId).toList());
    }

    @Test
    void ordenaPorIdConSeleccionSinModificarLaListaOriginal() {
        List<Solicitud> ordenadas = gestor.ordenar("id");
        assertEquals(List.of(1, 2, 3), ordenadas.stream().map(Solicitud::getId).toList());
        assertEquals(3, gestor.listarTodas().get(0).getId());
    }

    @Test
    void criterioDeOrdenamientoInvalidoLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> gestor.ordenar("color"));
    }

    @Test
    void busquedaBinariaEncuentraPorId() {
        assertEquals("Servicio caído", gestor.buscarPorIdBinaria(2).getDescripcion());
        assertNull(gestor.buscarPorIdBinaria(99));
    }

    @Test
    void busquedaLinealPorTexto() {
        assertEquals(1, gestor.buscarPorTexto("SESIÓN").size());
        assertEquals(3, gestor.buscarPorTexto("laura").size());
        assertTrue(gestor.buscarPorTexto("inexistente").isEmpty());
    }

    @Test
    void noSePuedeAtenderDosVeces() {
        Solicitud solicitud = gestor.tomarSiguiente();
        solicitud.atender(empleado);
        assertThrows(IllegalStateException.class, () -> solicitud.atender(empleado));
    }
}
