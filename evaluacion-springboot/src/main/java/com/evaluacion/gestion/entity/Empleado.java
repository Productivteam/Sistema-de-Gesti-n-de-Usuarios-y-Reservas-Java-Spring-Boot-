package com.evaluacion.gestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("EMPLEADO")
@Getter
@Setter
@NoArgsConstructor
public class Empleado extends Usuario {

    @NotBlank(message = "El cargo es obligatorio")
    @Column(name = "cargo", length = 80)
    private String cargo;

    @NotNull(message = "El salario es obligatorio")
    @Positive(message = "El salario debe ser mayor que cero")
    @Column(name = "salario")
    private Double salario;

    public Empleado(String nombre, String correo, String cargo, Double salario) {
        super(nombre, correo);
        this.cargo = cargo;
        this.salario = salario;
    }

    @Override
    public String obtenerDescripcion() {
        return "Empleado " + getNombre() + " - cargo: " + cargo + ", salario: " + salario;
    }
}
