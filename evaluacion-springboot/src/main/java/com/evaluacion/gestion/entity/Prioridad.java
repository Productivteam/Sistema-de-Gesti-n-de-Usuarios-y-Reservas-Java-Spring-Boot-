package com.evaluacion.gestion.entity;

/**
 * Prioridad de una solicitud. El peso se usa para ordenar:
 * un peso menor significa que la solicitud es más urgente.
 */
public enum Prioridad {
    ALTA(1),
    MEDIA(2),
    BAJA(3);

    private final int peso;

    Prioridad(int peso) {
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }
}
