package com.evaluacion.gestion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Atrapa los errores de toda la API y los devuelve como un JSON claro.
 * @RestControllerAdvice hace que aplique a todos los controladores.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Errores de @Valid: @NotBlank, @Email, @Positive, @Min, @Max...
     * Devuelve cada campo con su mensaje, por ejemplo: "salario": "El salario debe ser mayor que cero".
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> cuerpo = crearCuerpo(HttpStatus.BAD_REQUEST, "Se encontraron errores de validación");
        cuerpo.put("errores", errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /**
     * Errores lanzados por los servicios: cliente no existe (404), correo repetido (409), etc.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresDeNegocio(ResponseStatusException ex) {
        HttpStatus estado = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(estado).body(crearCuerpo(estado, ex.getReason()));
    }

    /**
     * JSON mal escrito o un valor que no existe, por ejemplo "prioridad": "URGENTE".
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(crearCuerpo(HttpStatus.BAD_REQUEST,
                "El JSON enviado no es válido. Revise el formato y los valores (prioridad: ALTA, MEDIA o BAJA)"));
    }

    private Map<String, Object> crearCuerpo(HttpStatus estado, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("fecha", LocalDateTime.now().toString());
        cuerpo.put("estado", estado.value());
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}