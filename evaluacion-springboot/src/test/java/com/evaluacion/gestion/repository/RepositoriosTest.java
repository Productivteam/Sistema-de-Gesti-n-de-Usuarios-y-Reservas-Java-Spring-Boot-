package com.evaluacion.gestion.repository;

import com.evaluacion.gestion.entity.Cliente;
import com.evaluacion.gestion.entity.Empleado;
import com.evaluacion.gestion.entity.EstadoSolicitud;
import com.evaluacion.gestion.entity.Prioridad;
import com.evaluacion.gestion.entity.Solicitud;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de persistencia sobre H2 en memoria.
 * @Transactional deshace los cambios al terminar cada prueba.
 */
@SpringBootTest
@Transactional
class RepositoriosTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void guardaUnClienteConSusSolicitudes() {
        Cliente cliente = clienteRepository.save(new Cliente("Cliente Prueba", "cliente.repo@correo.com", 100));

        Solicitud s1 = new Solicitud("No puedo ingresar a mi cuenta", Prioridad.ALTA);
        s1.setCliente(cliente);
        Solicitud s2 = new Solicitud("Quiero cambiar mi dirección", Prioridad.BAJA);
        s2.setCliente(cliente);
        solicitudRepository.save(s1);
        solicitudRepository.save(s2);

        List<Solicitud> delCliente = solicitudRepository.findByClienteId(cliente.getId());

        assertEquals(2, delCliente.size());
        assertNotNull(s1.getId());
        assertEquals(EstadoSolicitud.PENDIENTE, s1.getEstado());
    }

    @Test
    void buscaUsuarioPorCorreoSinImportarMayusculas() {
        usuarioRepository.save(new Empleado("Empleado Prueba", "empleado.repo@empresa.com", "Soporte", 2500000.0));

        assertTrue(usuarioRepository.existsByCorreoIgnoreCase("EMPLEADO.REPO@empresa.com"));
        assertTrue(usuarioRepository.findByCorreoIgnoreCase("empleado.repo@empresa.com").isPresent());
    }

    @Test
    void clienteRepositoryNoDevuelveEmpleados() {
        Empleado empleado = usuarioRepository.save(
                new Empleado("Otro Empleado", "otro.repo@empresa.com", "Analista", 3000000.0));
        entityManager.flush();
        entityManager.clear(); // obliga a consultar de nuevo la base de datos

        // El id existe en la tabla usuarios, pero su tipo es EMPLEADO, no CLIENTE
        assertTrue(clienteRepository.findById(empleado.getId()).isEmpty());
    }
}
