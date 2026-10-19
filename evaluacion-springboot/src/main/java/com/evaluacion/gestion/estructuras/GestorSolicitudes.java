package com.evaluacion.gestion.estructuras;

import com.evaluacion.gestion.entity.EstadoSolicitud;
import com.evaluacion.gestion.entity.Solicitud;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

/**
 * Maneja las solicitudes en memoria con tres estructuras de datos:
 *
 *  - Lista (ArrayList): guarda TODAS las solicitudes. Se usa para buscar y ordenar.
 *  - Cola  (Queue):     solicitudes PENDIENTES en orden de llegada (FIFO: la primera
 *                       que entra es la primera que se atiende).
 *  - Pila  (Stack):     historial de solicitudes atendidas (LIFO: la última atendida
 *                       queda arriba).
 *
 * Es un @Component, así que Spring crea una sola instancia para toda la aplicación.
 */
@Component
public class GestorSolicitudes {

    private final ArrayList<Solicitud> solicitudes = new ArrayList<>();
    private final Queue<Solicitud> colaPendientes = new LinkedList<>();
    private final Stack<Solicitud> historial = new Stack<>();

    // ======================= LISTA + COLA =======================

    /**
     * Agrega una solicitud a la lista y, si está pendiente, al final de la cola.
     */
    public void registrar(Solicitud solicitud) {
        if (solicitud == null || solicitud.getId() == null) {
            throw new IllegalArgumentException("Solo se pueden registrar solicitudes guardadas (con id)");
        }

        solicitudes.add(solicitud);

        if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE) {
            colaPendientes.offer(solicitud);
        } else {
            // Las ya atendidas van directo al historial
            historial.push(solicitud);
        }
    }

    /**
     * Saca la primera solicitud de la cola (la más antigua). Devuelve null si la cola está vacía.
     */
    public Solicitud tomarSiguiente() {
        return colaPendientes.poll();
    }

    /**
     * Mira la siguiente solicitud sin sacarla de la cola.
     */
    public Solicitud verSiguiente() {
        return colaPendientes.peek();
    }

    public boolean hayPendientes() {
        return !colaPendientes.isEmpty();
    }

    public List<Solicitud> verCola() {
        return new ArrayList<>(colaPendientes);
    }

    public List<Solicitud> listarTodas() {
        return new ArrayList<>(solicitudes);
    }

    // ======================= PILA =======================

    public void registrarEnHistorial(Solicitud solicitud) {
        historial.push(solicitud);
    }

    /**
     * Última solicitud atendida (cima de la pila) o null si no hay historial.
     */
    public Solicitud ultimaAtendida() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.peek();
    }

    /**
     * Historial desde la más reciente hasta la más antigua (de la cima hacia abajo).
     */
    public List<Solicitud> verHistorial() {
        List<Solicitud> resultado = new ArrayList<>();
        for (int i = historial.size() - 1; i >= 0; i--) {
            resultado.add(historial.get(i));
        }
        return resultado;
    }

    public void limpiar() {
        solicitudes.clear();
        colaPendientes.clear();
        historial.clear();
    }

    // ======================= ORDENAMIENTO =======================

    /**
     * Devuelve una copia de la lista ordenada según el criterio. No modifica la lista original.
     */
    public List<Solicitud> ordenar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            criterio = "id";
        }

        List<Solicitud> copia = new ArrayList<>(solicitudes);

        switch (criterio.trim().toLowerCase()) {
            case "prioridad" -> ordenarPorPrioridadBurbuja(copia);
            case "fecha" -> ordenarPorFechaInsercion(copia);
            case "id" -> ordenarPorIdSeleccion(copia);
            default -> throw new IllegalArgumentException(
                    "Criterio de ordenamiento no válido: '" + criterio + "'. Use prioridad, fecha o id");
        }
        return copia;
    }

    /**
     * Ordenamiento burbuja: compara pares vecinos y los intercambia si están en desorden.
     * ALTA primero, luego MEDIA y BAJA. Si la prioridad es igual, la más antigua primero.
     */
    private void ordenarPorPrioridadBurbuja(List<Solicitud> lista) {
        int n = lista.size();
        for (int i = 0; i < n - 1; i++) {
            boolean huboIntercambio = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (compararPorPrioridad(lista.get(j), lista.get(j + 1)) > 0) {
                    Solicitud temporal = lista.get(j);
                    lista.set(j, lista.get(j + 1));
                    lista.set(j + 1, temporal);
                    huboIntercambio = true;
                }
            }
            if (!huboIntercambio) {
                break; // ya está ordenada
            }
        }
    }

    private int compararPorPrioridad(Solicitud a, Solicitud b) {
        int diferencia = a.getPrioridad().getPeso() - b.getPrioridad().getPeso();
        if (diferencia != 0) {
            return diferencia;
        }
        return a.getFechaCreacion().compareTo(b.getFechaCreacion());
    }

    /**
     * Ordenamiento por inserción: toma cada elemento y lo inserta en su lugar
     * dentro de la parte ya ordenada. De la más antigua a la más reciente.
     */
    private void ordenarPorFechaInsercion(List<Solicitud> lista) {
        for (int i = 1; i < lista.size(); i++) {
            Solicitud actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && lista.get(j).getFechaCreacion().isAfter(actual.getFechaCreacion())) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
    }

    /**
     * Ordenamiento por selección: busca el menor id y lo pone al inicio, y así sucesivamente.
     */
    private void ordenarPorIdSeleccion(List<Solicitud> lista) {
        for (int i = 0; i < lista.size() - 1; i++) {
            int posicionMenor = i;
            for (int j = i + 1; j < lista.size(); j++) {
                if (lista.get(j).getId() < lista.get(posicionMenor).getId()) {
                    posicionMenor = j;
                }
            }
            if (posicionMenor != i) {
                Solicitud temporal = lista.get(i);
                lista.set(i, lista.get(posicionMenor));
                lista.set(posicionMenor, temporal);
            }
        }
    }

    // ======================= BÚSQUEDA =======================

    /**
     * Búsqueda binaria por id: la lista debe estar ordenada por id.
     * En cada paso descarta la mitad de los elementos.
     */
    public Solicitud buscarPorIdBinaria(Integer id) {
        List<Solicitud> ordenadas = ordenar("id");
        int inicio = 0;
        int fin = ordenadas.size() - 1;

        while (inicio <= fin) {
            int medio = (inicio + fin) / 2;
            Integer idMedio = ordenadas.get(medio).getId();

            if (idMedio.equals(id)) {
                return ordenadas.get(medio);
            } else if (idMedio < id) {
                inicio = medio + 1;   // buscar en la mitad derecha
            } else {
                fin = medio - 1;      // buscar en la mitad izquierda
            }
        }
        return null; // no se encontró
    }

    /**
     * Búsqueda lineal: recorre toda la lista buscando el texto en la descripción
     * o en el nombre del cliente (sin importar mayúsculas).
     */
    public List<Solicitud> buscarPorTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Debe indicar un texto para buscar");
        }

        String buscado = texto.trim().toLowerCase();
        List<Solicitud> encontradas = new ArrayList<>();

        for (Solicitud solicitud : solicitudes) {
            boolean coincideDescripcion = solicitud.getDescripcion().toLowerCase().contains(buscado);
            boolean coincideCliente = solicitud.getCliente() != null
                    && solicitud.getCliente().getNombre().toLowerCase().contains(buscado);

            if (coincideDescripcion || coincideCliente) {
                encontradas.add(solicitud);
            }
        }
        return encontradas;
    }

    // ======================= RESUMEN =======================

    public Map<String, Object> resumen() {
        int pendientes = 0;
        int atendidas = 0;
        int alta = 0;
        int media = 0;
        int baja = 0;

        for (Solicitud solicitud : solicitudes) {
            if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE) {
                pendientes++;
            } else {
                atendidas++;
            }

            switch (solicitud.getPrioridad()) {
                case ALTA -> alta++;
                case MEDIA -> media++;
                case BAJA -> baja++;
            }
        }

        Map<String, Object> porPrioridad = new LinkedHashMap<>();
        porPrioridad.put("ALTA", alta);
        porPrioridad.put("MEDIA", media);
        porPrioridad.put("BAJA", baja);

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("total", solicitudes.size());
        resumen.put("pendientes", pendientes);
        resumen.put("atendidas", atendidas);
        resumen.put("enCola", colaPendientes.size());
        resumen.put("enHistorial", historial.size());
        resumen.put("porPrioridad", porPrioridad);
        return resumen;
    }
}
