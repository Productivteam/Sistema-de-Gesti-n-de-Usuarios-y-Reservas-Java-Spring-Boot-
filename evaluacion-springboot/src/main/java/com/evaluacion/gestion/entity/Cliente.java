package com.evaluacion.gestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("CLIENTE")
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Usuario {

    @PositiveOrZero(message = "Los puntos de fidelidad no pueden ser negativos")
    @Column(name = "puntos_fidelidad")
    private Integer puntosFidelidad = 0;

    // Un cliente puede tener muchas solicitudes (relación 1 a N).
    // mappedBy = "cliente": la llave foránea (cliente_id) está en la tabla solicitudes.
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Solicitud> solicitudes = new ArrayList<>();

    public Cliente(String nombre, String correo, Integer puntosFidelidad) {
        super(nombre, correo);
        this.puntosFidelidad = puntosFidelidad;
    }

    @Override
    public String obtenerDescripcion() {
        int puntos = (puntosFidelidad == null) ? 0 : puntosFidelidad;

        String categoria;
        if (puntos >= 1000) {
            categoria = "Oro";
        } else if (puntos >= 500) {
            categoria = "Plata";
        } else {
            categoria = "Bronce";
        }
        return "Cliente " + getNombre() + " - " + puntos + " puntos (categoría " + categoria + ")";
    }
}
