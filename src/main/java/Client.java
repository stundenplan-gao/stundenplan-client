import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.Fach;

import java.io.IOException;
import java.util.concurrent.*;

public class Client {
    public RestApiClient RAC;
    public GUI gui;

    public Client() throws IOException, InterruptedException {
        RAC = new RestApiClient();
        gui = new GUI();
        String logData = gui.register();
        String[] arr = logData.split(":");
        RAC.getToken(arr[0], arr[1]);

        try {
            gui.setFaecher(RAC.getResource("/schueler/faecherauswahl", Fach[].class));
        } catch (ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
    }
}