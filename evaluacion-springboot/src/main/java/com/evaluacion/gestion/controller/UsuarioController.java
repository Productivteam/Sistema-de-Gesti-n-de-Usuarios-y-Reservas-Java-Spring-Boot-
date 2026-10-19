package com.evaluacion.gestion.controller;

import com.evaluacion.gestion.dto.request.UsuarioRequest;
import com.evaluacion.gestion.dto.response.UsuarioResponse;
import com.evaluacion.gestion.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Crea un Administrador, Empleado o Cliente según el campo "tipo".
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    /**
     * Lista paginada y ordenada. Ejemplo: GET /usuarios?page=0&size=5&sort=nombre,asc
     */
    @GetMapping
    public PagedModel<UsuarioResponse> listar(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PagedModel<>(usuarioService.listar(pageable));
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id);
    }
}
