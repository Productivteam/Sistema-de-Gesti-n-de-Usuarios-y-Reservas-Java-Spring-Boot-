package com.evaluacion.gestion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos que llegan en el POST /usuarios.
 * "tipo" indica qué usuario se crea: ADMINISTRADOR, EMPLEADO o CLIENTE.
 * Cada tipo usa solo sus campos (nivelAcceso, cargo/salario o puntosFidelidad).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequest {

    @NotBlank(message = "El tipo es obligatorio (ADMINISTRADOR, EMPLEADO o CLIENTE)")
    private String tipo;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(regexp = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$", message = "El correo no tiene un formato válido")
    private String correo;

    // Administrador
    @Min(value = 1, message = "El nivel de acceso mínimo es 1")
    @Max(value = 5, message = "El nivel de acceso máximo es 5")
    private Integer nivelAcceso;

    // Empleado
    private String cargo;

    @Positive(message = "El salario debe ser mayor que cero")
    private Double salario;

    // Cliente
    @PositiveOrZero(message = "Los puntos de fidelidad no pueden ser negativos")
    private Integer puntosFidelidad;
}
