package videojuegos;

/**
 * Modelo de datos para un Videojuego.
 */
public class Videojuego {
    private int id;
    private String titulo;
    private String genero;
    private String plataforma;
    private int anioLanzamiento;
    private double precio;

    public Videojuego(int id, String titulo, String genero, String plataforma,
                      int anioLanzamiento, double precio) {
        this.id = id;
        this.titulo = titulo;
        this.genero = genero;
        this.plataforma = plataforma;
        this.anioLanzamiento = anioLanzamiento;
        this.precio = precio;
    }

    public int getId()               { return id; }
    public String getTitulo()        { return titulo; }
    public String getGenero()        { return genero; }
    public String getPlataforma()    { return plataforma; }
    public int getAnioLanzamiento()  { return anioLanzamiento; }
    public double getPrecio()        { return precio; }

    public void setTitulo(String titulo)               { this.titulo = titulo; }
    public void setGenero(String genero)               { this.genero = genero; }
    public void setPlataforma(String plataforma)       { this.plataforma = plataforma; }
    public void setAnioLanzamiento(int anio)           { this.anioLanzamiento = anio; }
    public void setPrecio(double precio)               { this.precio = precio; }

    @Override
    public String toString() {
        return String.format("| %-4d | %-30s | %-15s | %-12s | %-4d | $%-8.2f |",
                id, titulo, genero, plataforma, anioLanzamiento, precio);
    }
}
