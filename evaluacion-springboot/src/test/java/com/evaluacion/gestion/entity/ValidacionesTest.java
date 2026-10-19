package com.evaluacion.gestion.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de las validaciones de las entidades (sin levantar Spring).
 */
class ValidacionesTest {

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void clienteValidoNoTieneErrores() {
        Cliente cliente = new Cliente("Laura Gómez", "laura@correo.com", 50);
        assertTrue(validator.validate(cliente).isEmpty());
    }

    @Test
    void nombreVacioEsInvalido() {
        Cliente cliente = new Cliente("   ", "laura@correo.com", 0);
        assertTieneErrorEn(validator.validate(cliente), "nombre");
    }

    @Test
    void correoConFormatoIncorrectoEsInvalido() {
        Cliente sinArroba = new Cliente("Laura", "laura.correo.com", 0);
        Cliente sinDominio = new Cliente("Laura", "laura@correo", 0);

        assertTieneErrorEn(validator.validate(sinArroba), "correo");
        assertTieneErrorEn(validator.validate(sinDominio), "correo");
    }

    @Test
    void salarioDebeSerMayorQueCero() {
        Empleado salarioCero = new Empleado("Pedro", "pedro@empresa.com", "Soporte", 0.0);
        Empleado salarioNegativo = new Empleado("Pedro", "pedro@empresa.com", "Soporte", -100.0);
        Empleado salarioValido = new Empleado("Pedro", "pedro@empresa.com", "Soporte", 1500000.0);

        assertTieneErrorEn(validator.validate(salarioCero), "salario");
        assertTieneErrorEn(validator.validate(salarioNegativo), "salario");
        assertTrue(validator.validate(salarioValido).isEmpty());
    }

    @Test
    void nivelAccesoDebeEstarEntreUnoYCinco() {
        int[] nivelesInvalidos = {0, 6, -1};
        for (int nivel : nivelesInvalidos) {
            Administrador admin = new Administrador("Ana", "ana@empresa.com", nivel);
            assertTieneErrorEn(validator.validate(admin), "nivelAcceso");
        }

        for (int nivel = 1; nivel <= 5; nivel++) {
            Administrador admin = new Administrador("Ana", "ana@empresa.com", nivel);
            assertTrue(validator.validate(admin).isEmpty(), "El nivel " + nivel + " debería ser válido");
        }
    }

    @Test
    void solicitudSinDescripcionEsInvalida() {
        Solicitud solicitud = new Solicitud("", Prioridad.ALTA);
        assertTieneErrorEn(validator.validate(solicitud), "descripcion");
    }

    private <T> void assertTieneErrorEn(Set<ConstraintViolation<T>> errores, String campo) {
        boolean encontrado = false;
        for (ConstraintViolation<T> error : errores) {
            if (error.getPropertyPath().toString().equals(campo)) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "Se esperaba un error de validación en el campo '" + campo + "'");
    }
}
