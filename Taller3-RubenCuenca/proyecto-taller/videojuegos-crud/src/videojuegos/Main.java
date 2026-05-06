package videojuegos;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Sistema de Gestión de Videojuegos
 * CRUD de consola en Java puro – integrado con EPN Event Manager
 */
public class Main {

    private static final VideojuegosCRUD crud = new VideojuegosCRUD();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        cargarDatosDemostracion();

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║    SISTEMA DE GESTIÓN DE VIDEOJUEGOS  🎮          ║");
        System.out.println("║    Integrado con EPN Event Manager               ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1 -> listarVideojuegos();
                case 2 -> buscarVideojuego();
                case 3 -> crearVideojuego();
                case 4 -> actualizarVideojuego();
                case 5 -> eliminarVideojuego();
                case 0 -> salir = true;
                default -> System.out.println("  ⚠  Opción inválida. Intente de nuevo.\n");
            }
        }

        System.out.println("\n¡Hasta luego! 👾");
        sc.close();
    }

    // =========================================================================
    // MENÚ
    // =========================================================================
    private static void mostrarMenu() {
        System.out.println("\n┌─────────────────────────────────┐");
        System.out.println("│           MENÚ PRINCIPAL        │");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│  1. Listar todos los juegos     │");
        System.out.println("│  2. Buscar juego por ID         │");
        System.out.println("│  3. Agregar nuevo juego         │");
        System.out.println("│  4. Actualizar juego            │");
        System.out.println("│  5. Eliminar juego              │");
        System.out.println("│  0. Salir                       │");
        System.out.println("└─────────────────────────────────┘");
    }

    // =========================================================================
    // OPERACIONES
    // =========================================================================

    private static void listarVideojuegos() {
        System.out.println("\n  📋 LISTADO DE VIDEOJUEGOS");
        List<Videojuego> lista = crud.listarTodos();

        if (lista.isEmpty()) {
            System.out.println("  (No hay videojuegos registrados)");
            return;
        }

        imprimirCabecera();
        for (Videojuego v : lista) {
            System.out.println(v);
        }
        imprimirPie();
        System.out.println("  Total: " + lista.size() + " videojuego(s).");
    }

    private static void buscarVideojuego() {
        System.out.println("\n  🔍 BUSCAR VIDEOJUEGO");
        int id = leerEntero("  ID del juego a buscar: ");
        Videojuego v = crud.buscarPorId(id);
        if (v == null) {
            System.out.println("  ✖ No se encontró ningún videojuego con ID=" + id);
        } else {
            imprimirCabecera();
            System.out.println(v);
            imprimirPie();
        }
    }

    private static void crearVideojuego() {
        System.out.println("\n  ➕ AGREGAR VIDEOJUEGO");
        String titulo    = leerTexto("  Título       : ");
        String genero    = leerTexto("  Género        : ");
        String plataforma = leerTexto("  Plataforma    : ");
        int anio         = leerEntero("  Año           : ");
        double precio    = leerDecimal("  Precio (USD)  : ");

        Videojuego v = crud.crear(titulo, genero, plataforma, anio, precio);
        System.out.println("  ✔ Videojuego creado con ID=" + v.getId());
    }

    private static void actualizarVideojuego() {
        System.out.println("\n  ✏  ACTUALIZAR VIDEOJUEGO");
        int id = leerEntero("  ID del juego a actualizar: ");

        Videojuego existente = crud.buscarPorId(id);
        if (existente == null) {
            System.out.println("  ✖ No existe videojuego con ID=" + id);
            return;
        }

        System.out.println("  Datos actuales: " + existente.getTitulo());
        System.out.println("  (Deje en blanco para conservar el valor actual)\n");

        String titulo     = leerTextoOpcional("  Nuevo título       [" + existente.getTitulo() + "]: ",
                                              existente.getTitulo());
        String genero     = leerTextoOpcional("  Nuevo género       [" + existente.getGenero() + "]: ",
                                              existente.getGenero());
        String plataforma = leerTextoOpcional("  Nueva plataforma   [" + existente.getPlataforma() + "]: ",
                                              existente.getPlataforma());
        int anio          = leerEnteroOpcional("  Nuevo año          [" + existente.getAnioLanzamiento() + "]: ",
                                              existente.getAnioLanzamiento());
        double precio     = leerDecimalOpcional("  Nuevo precio       [" + existente.getPrecio() + "]: ",
                                              existente.getPrecio());

        boolean ok = crud.actualizar(id, titulo, genero, plataforma, anio, precio);
        System.out.println(ok ? "  ✔ Videojuego actualizado." : "  ✖ No se pudo actualizar.");
    }

    private static void eliminarVideojuego() {
        System.out.println("\n  🗑  ELIMINAR VIDEOJUEGO");
        int id = leerEntero("  ID del juego a eliminar: ");
        boolean ok = crud.eliminar(id);
        System.out.println(ok
                ? "  ✔ Videojuego eliminado correctamente."
                : "  ✖ No existe videojuego con ID=" + id);
    }

    // =========================================================================
    // UTILIDADES DE CONSOLA
    // =========================================================================

    private static void imprimirCabecera() {
        System.out.println("+------+--------------------------------+-----------------+--------------+------+----------+");
        System.out.println("| ID   | Título                         | Género          | Plataforma   | Año  | Precio   |");
        System.out.println("+------+--------------------------------+-----------------+--------------+------+----------+");
    }

    private static void imprimirPie() {
        System.out.println("+------+--------------------------------+-----------------+--------------+------+----------+");
    }

    private static String leerTexto(String prompt) {
        String valor = "";
        while (valor.trim().isEmpty()) {
            System.out.print(prompt);
            valor = sc.nextLine();
        }
        return valor.trim();
    }

    private static String leerTextoOpcional(String prompt, String defecto) {
        System.out.print(prompt);
        String valor = sc.nextLine().trim();
        return valor.isEmpty() ? defecto : valor;
    }

    private static int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Por favor ingrese un número entero válido.");
            }
        }
    }

    private static int leerEnteroOpcional(String prompt, int defecto) {
        System.out.print(prompt);
        String linea = sc.nextLine().trim();
        if (linea.isEmpty()) return defecto;
        try {
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            System.out.println("  ⚠  Valor inválido, se conserva el anterior.");
            return defecto;
        }
    }

    private static double leerDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Por favor ingrese un número decimal válido (use punto: 59.99).");
            }
        }
    }

    private static double leerDecimalOpcional(String prompt, double defecto) {
        System.out.print(prompt);
        String linea = sc.nextLine().trim();
        if (linea.isEmpty()) return defecto;
        try {
            return Double.parseDouble(linea);
        } catch (NumberFormatException e) {
            System.out.println("  ⚠  Valor inválido, se conserva el anterior.");
            return defecto;
        }
    }

    // =========================================================================
    // DATOS DE DEMOSTRACIÓN
    // =========================================================================
    private static void cargarDatosDemostracion() {
        // Se cargan silenciosamente (sin imprimir mensajes del hub)
        System.out.println("  Cargando datos de demostración...");
        crud.crear("The Legend of Zelda: TOTK", "Aventura",  "Nintendo Switch", 2023, 69.99);
        crud.crear("Red Dead Redemption 2",      "Acción",    "PS5",             2018, 39.99);
        crud.crear("Elden Ring",                 "RPG",       "PC",              2022, 59.99);
        crud.crear("FIFA 25",                    "Deportes",  "PS5",             2024, 69.99);
        crud.crear("Minecraft",                  "Sandbox",   "PC",              2011, 26.95);
        System.out.println("  ✔ " + crud.totalRegistros() + " juegos cargados.\n");
    }
}
