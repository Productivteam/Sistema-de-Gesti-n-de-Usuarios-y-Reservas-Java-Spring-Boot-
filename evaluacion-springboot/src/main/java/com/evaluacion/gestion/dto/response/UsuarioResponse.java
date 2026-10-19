package com.evaluacion.gestion.dto.response;

import com.evaluacion.gestion.entity.Administrador;
import com.evaluacion.gestion.entity.Cliente;
import com.evaluacion.gestion.entity.Empleado;
import com.evaluacion.gestion.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lo que la API devuelve de un usuario.
 * @JsonInclude(NON_NULL): no muestra los campos vacíos (un cliente no muestra salario).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioResponse {

    private Integer id;
    private String tipo;
    private String nombre;
    private String correo;
    private String descripcion;

    private Integer nivelAcceso;
    private String cargo;
    private Double salario;
    private Integer puntosFidelidad;

    /**
     * Convierte la entidad en respuesta. obtenerDescripcion() es polimórfico:
     * se ejecuta la versión de Administrador, Empleado o Cliente según el objeto real.
     */
    public static UsuarioResponse desde(Usuario usuario) {
        UsuarioResponse response = UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .descripcion(usuario.obtenerDescripcion())
                .build();

        if (usuario instanceof Administrador admin) {
            response.setTipo("ADMINISTRADOR");
            response.setNivelAcceso(admin.getNivelAcceso());
        } else if (usuario instanceof Empleado empleado) {
            response.setTipo("EMPLEADO");
            response.setCargo(empleado.getCargo());
            response.setSalario(empleado.getSalario());
        } else if (usuario instanceof Cliente cliente) {
            response.setTipo("CLIENTE");
            response.setPuntosFidelidad(cliente.getPuntosFidelidad());
        }
        return response;
    }
}
