package com.evaluacion.gestion.dto.response;

import com.evaluacion.gestion.entity.Solicitud;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lo que la API devuelve de una solicitud.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitudResponse {

    private Integer id;
    private String descripcion;
    private String prioridad;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAtencion;

    private Integer clienteId;
    private String clienteNombre;

    private Integer empleadoId;
    private String empleadoNombre;

    public static SolicitudResponse desde(Solicitud solicitud) {
        SolicitudResponse response = SolicitudResponse.builder()
                .id(solicitud.getId())
                .descripcion(solicitud.getDescripcion())
                .prioridad(solicitud.getPrioridad().name())
                .estado(solicitud.getEstado().name())
                .fechaCreacion(solicitud.getFechaCreacion())
                .fechaAtencion(solicitud.getFechaAtencion())
                .clienteId(solicitud.getCliente().getId())
                .clienteNombre(solicitud.getCliente().getNombre())
                .build();

        if (solicitud.getEmpleadoAsignado() != null) {
            response.setEmpleadoId(solicitud.getEmpleadoAsignado().getId());
            response.setEmpleadoNombre(solicitud.getEmpleadoAsignado().getNombre());
        }
        return response;
    }

    public static List<SolicitudResponse> desde(List<Solicitud> solicitudes) {
        List<SolicitudResponse> respuestas = new ArrayList<>();
        for (Solicitud solicitud : solicitudes) {
            respuestas.add(desde(solicitud));
        }
        return respuestas;
    }
}
