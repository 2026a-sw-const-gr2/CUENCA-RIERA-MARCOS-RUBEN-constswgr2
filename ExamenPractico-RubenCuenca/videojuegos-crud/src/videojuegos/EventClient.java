package videojuegos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Cliente HTTP que envía eventos al EPN Event Manager.
 * Endpoint: POST http://localhost:3000/events
 */
public class EventClient {

    private static final String URL = "http://localhost:3000/events";
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Envía un evento al hub. Si el hub no está disponible, lo notifica
     * sin detener la ejecución del CRUD.
     *
     * @param action  CREATE | UPDATE | DELETE | QUERY
     * @param titulo  Título del videojuego involucrado
     * @param detalle Descripción adicional del evento
     */
    public static void enviar(String action, String titulo, String detalle) {
        try {
            String payload = buildJson(action, titulo, detalle);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("  [Hub] Evento enviado correctamente ✔");
            } else {
                System.out.println("  [Hub] Advertencia: respuesta " + response.statusCode());
            }

        } catch (Exception e) {
            // Si el hub no está corriendo, el CRUD sigue funcionando normalmente
            System.out.println("  [Hub] No disponible (¿está corriendo el Event Manager?): "
                    + e.getMessage());
        }
    }

    private static String buildJson(String action, String titulo, String detalle) {
        // Escapa caracteres especiales básicos para JSON
        titulo = titulo.replace("\"", "\\\"");
        detalle = detalle.replace("\"", "\\\"");

        return "{"
            + "\"source\":\"videojuegos-crud\","
            + "\"entity\":\"Videojuego\","
            + "\"action\":\"" + action + "\","
            + "\"title\":\"" + titulo + "\","
            + "\"description\":\"" + detalle + "\","
            + "\"payload\":{\"app\":\"Sistema de Gestión de Videojuegos\"}"
            + "}";
    }
}
