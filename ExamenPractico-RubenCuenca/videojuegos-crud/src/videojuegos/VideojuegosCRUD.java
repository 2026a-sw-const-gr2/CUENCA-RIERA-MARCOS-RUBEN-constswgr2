package videojuegos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio en memoria para el CRUD de Videojuegos.
 */
public class VideojuegosCRUD {

    private final Map<Integer, Videojuego> almacenamiento = new LinkedHashMap<>();
    private int contadorId = 1;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------
    public Videojuego crear(String titulo, String genero, String plataforma,
                            int anio, double precio) {
        Videojuego v = new Videojuego(contadorId++, titulo, genero, plataforma, anio, precio);
        almacenamiento.put(v.getId(), v);

        // Enviar evento al hub
        EventClient.enviar("CREATE", titulo,
                "Videojuego creado: " + titulo + " (" + plataforma + ")");

        return v;
    }

    // -------------------------------------------------------------------------
    // READ ALL
    // -------------------------------------------------------------------------
    public List<Videojuego> listarTodos() {
        List<Videojuego> lista = new ArrayList<>(almacenamiento.values());

        // Enviar evento QUERY al hub
        EventClient.enviar("QUERY", "listado-completo",
                "Consulta de todos los videojuegos (" + lista.size() + " registros)");

        return lista;
    }

    // -------------------------------------------------------------------------
    // READ ONE
    // -------------------------------------------------------------------------
    public Videojuego buscarPorId(int id) {
        Videojuego v = almacenamiento.get(id);
        if (v != null) {
            EventClient.enviar("QUERY", v.getTitulo(),
                    "Consulta del videojuego ID=" + id);
        }
        return v;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------
    public boolean actualizar(int id, String titulo, String genero,
                              String plataforma, int anio, double precio) {
        Videojuego v = almacenamiento.get(id);
        if (v == null) return false;

        v.setTitulo(titulo);
        v.setGenero(genero);
        v.setPlataforma(plataforma);
        v.setAnioLanzamiento(anio);
        v.setPrecio(precio);

        EventClient.enviar("UPDATE", titulo,
                "Videojuego actualizado ID=" + id);

        return true;
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------
    public boolean eliminar(int id) {
        Videojuego v = almacenamiento.remove(id);
        if (v == null) return false;

        EventClient.enviar("DELETE", v.getTitulo(),
                "Videojuego eliminado ID=" + id + ": " + v.getTitulo());

        return true;
    }

    public int totalRegistros() {
        return almacenamiento.size();
    }
}
