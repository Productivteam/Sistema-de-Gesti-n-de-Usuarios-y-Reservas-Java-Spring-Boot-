package com.evaluacion.gestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Usuario {

    // En SINGLE_TABLE las columnas propias de cada rol deben aceptar null en la tabla
    @NotNull(message = "El nivel de acceso es obligatorio")
    @Min(value = 1, message = "El nivel de acceso mínimo es 1")
    @Max(value = 5, message = "El nivel de acceso máximo es 5")
    @Column(name = "nivel_acceso")
    private Integer nivelAcceso;

    public Administrador(String nombre, String correo, Integer nivelAcceso) {
        super(nombre, correo);
        this.nivelAcceso = nivelAcceso;
    }

    @Override
    public String obtenerDescripcion() {
        if (nivelAcceso == null) {
            return "Administrador " + getNombre() + " sin nivel de acceso asignado";
        }

        String permisos = switch (nivelAcceso) {
            case 1 -> "solo lectura";
            case 2 -> "lectura y edición";
            case 3 -> "gestión de usuarios";
            case 4 -> "gestión de usuarios y reportes";
            case 5 -> "control total del sistema";
            default -> "nivel desconocido";
        };
        return "Administrador " + getNombre() + " - nivel " + nivelAcceso + " (" + permisos + ")";
    }
}
