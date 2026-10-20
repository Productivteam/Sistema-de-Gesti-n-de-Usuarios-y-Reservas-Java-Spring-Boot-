package com.evaluacion.gestion.service;

import com.evaluacion.gestion.dto.request.UsuarioRequest;
import com.evaluacion.gestion.dto.response.UsuarioResponse;
import com.evaluacion.gestion.entity.Administrador;
import com.evaluacion.gestion.entity.Cliente;
import com.evaluacion.gestion.entity.Empleado;
import com.evaluacion.gestion.entity.Usuario;
import com.evaluacion.gestion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo " + correo);
        }

        Usuario usuario = construirUsuario(request, request.getNombre().trim(), correo);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /**
     * Según el "tipo" se crea la subclase correspondiente (herencia).
     */
    private Usuario construirUsuario(UsuarioRequest request, String nombre, String correo) {
        String tipo = request.getTipo().trim().toUpperCase();

        switch (tipo) {
            case "ADMINISTRADOR":
                if (request.getNivelAcceso() == null) {
                    throw error("El administrador necesita nivelAcceso (de 1 a 5)");
                }
                return new Administrador(nombre, correo, request.getNivelAcceso());

            case "EMPLEADO":
                if (request.getCargo() == null || request.getCargo().isBlank() || request.getSalario() == null) {
                    throw error("El empleado necesita cargo y salario");
                }
                return new Empleado(nombre, correo, request.getCargo().trim(), request.getSalario());

            case "CLIENTE":
                int puntos = (request.getPuntosFidelidad() == null) ? 0 : request.getPuntosFidelidad();
                return new Cliente(nombre, correo, puntos);

            default:
                throw error("Tipo de usuario no válido: " + request.getTipo()
                        + ". Use ADMINISTRADOR, EMPLEADO o CLIENTE");
        }
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponse::desde);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un usuario con id " + id));
        return UsuarioResponse.desde(usuario);
    }

    /**
     * Lista los usuarios de un solo tipo: ADMINISTRADOR, EMPLEADO o CLIENTE.
     * Recorre todos los usuarios y se queda con los del tipo pedido (búsqueda lineal con for + if).
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarPorTipo(String tipo) {
        String tipoBuscado = tipo.trim().toUpperCase();

        if (!List.of("ADMINISTRADOR", "EMPLEADO", "CLIENTE").contains(tipoBuscado)) {
            throw error("Tipo de usuario no válido: " + tipo + ". Use ADMINISTRADOR, EMPLEADO o CLIENTE");
        }

        List<UsuarioResponse> resultado = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findAll(Sort.by("id"))) {
            UsuarioResponse response = UsuarioResponse.desde(usuario);
            if (response.getTipo().equals(tipoBuscado)) {
                resultado.add(response);
            }
        }
        return resultado;
    }

    private ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}