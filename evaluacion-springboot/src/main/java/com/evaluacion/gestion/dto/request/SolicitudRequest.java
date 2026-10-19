package com.evaluacion.gestion.dto.request;

import com.evaluacion.gestion.entity.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos que llegan en el POST /solicitudes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRequest {

    @NotNull(message = "El id del cliente es obligatorio")
    private Integer clienteId;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
    private String descripcion;

    // Opcional: ALTA, MEDIA o BAJA. Si no se envía queda en MEDIA.
    private Prioridad prioridad;
}
