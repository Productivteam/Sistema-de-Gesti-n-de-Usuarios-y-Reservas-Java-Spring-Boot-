package com.evaluacion.gestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Solicitud de atención creada por un cliente y atendida por un empleado.
 */
@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    // EnumType.STRING guarda el texto (ALTA, MEDIA...) y no el número
    @NotNull(message = "La prioridad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 10)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoSolicitud estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    // Muchas solicitudes pertenecen a un cliente: cliente_id es la llave foránea
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Empleado que atendió la solicitud (null mientras está pendiente)
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleadoAsignado;

    public Solicitud(String descripcion, Prioridad prioridad) {
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Se ejecuta automáticamente antes de insertar la fila en la base de datos.
     */
    @PrePersist
    protected void antesDeGuardar() {
        if (estado == null) {
            estado = EstadoSolicitud.PENDIENTE;
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    /**
     * Marca la solicitud como atendida por un empleado.
     */
    public void atender(Empleado empleado) {
        if (this.estado == EstadoSolicitud.ATENDIDA) {
            throw new IllegalStateException("La solicitud " + id + " ya fue atendida");
        }
        this.empleadoAsignado = empleado;
        this.estado = EstadoSolicitud.ATENDIDA;
        this.fechaAtencion = LocalDateTime.now();
    }
}
