import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RestApiClient {
    private static final String ServerURL = "http://localhost:8080/Stundenplan_Server/stundenplan";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public RestApiClient() {

    }

    private String sendGet(String path) throws InterruptedException, ExecutionException, TimeoutException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(ServerURL + path))
                .setHeader("User-Agent", "Stundenplan Client")
                .build();

        CompletableFuture<HttpResponse<String>> response =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
    }

    public <T> T getResource(String pResourcePath, Class<T> returnType) throws InterruptedException, ExecutionException, TimeoutException, JsonProcessingException {
        String json = sendGet(pResourcePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, returnType);
    }

    public String getToken(String username, String password) throws IOException, InterruptedException {
        Map<Object, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(ofFormData(data))
                .uri(URI.create(ServerURL + "/schueler/login"))
                .setHeader("User-Agent", "Stundenplan Client")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private void sendPostJSON() throws IOException, InterruptedException {
        String json = new StringBuilder()
                .append("{")
                .append("\"name\":\"mkyong\",")
                .append("\"notes\":\"hello\"")
                .append("}").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create("https://httpbin.org/post"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}