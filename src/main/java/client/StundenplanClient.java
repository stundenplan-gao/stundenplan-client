package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import database.Fach;
import database.Schueler;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class StundenplanClient implements StundenplanAPI {

    private class JWTFilter implements ClientRequestFilter, ClientResponseFilter {
        @Override
        public void filter(ClientRequestContext requestContext) {
            if (token != null) {
                requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, token);
            }
        }

        @Override
        public void filter(ClientRequestContext requestContext,
                ClientResponseContext responseContext) throws IOException {
            String body = new String(responseContext.getEntityStream().readAllBytes());

            if (!body.equals("") &&responseContext.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                Object jsonObject = mapper.readValue(body, Object.class);

                String prettyFormatted = mapper.writeValueAsString(jsonObject);
                System.err.println(prettyFormatted);
            } else {
                System.err.println(body);
            }

            responseContext.setEntityStream(new ByteArrayInputStream(body.getBytes()));
        }

    }

    public static void main(String... args) {
        StundenplanClient c = new StundenplanClient("ysprenger", "ysprenger".toCharArray());
        System.err.println(c.echo("Testmessage"));
        try {
            System.err.println(c.echoAuth("Testmessage"));
        } catch (NotAuthorizedException e) {
            e.printStackTrace();
        }
        Fach[] faecher = c.getFaecherList();
        for (Fach fach : faecher) {
            System.err.println(fach);
        }
        try {
            Schueler schueler = c.getSchuelerMitFaechern("ysprenger");
            System.err.println(schueler.toFullString());
        } catch (NotAuthorizedException e) {
            e.printStackTrace();
        }
    }

    private final ResteasyClient client;

    private final StundenplanAPI proxy;

    private final ResteasyWebTarget target;

    protected String token;

    public StundenplanClient(String username, char[] passwd) {
        client = (ResteasyClient) ClientBuilder.newBuilder().register(new JWTFilter()).build();
        target = client.target("http://localhost:8080/Stundenplan_Server/stundenplan");
        proxy = target.proxy(StundenplanAPI.class);
        token = proxy.authenticateUser(username, new String(passwd));
    }

    @Override
    public String authenticateUser(String username, String password) {
        return proxy.authenticateUser(username, password);
    }

    @Override
    public String echo(String message) {
        return proxy.echo(message);
    }

    @Override
    public String echoAuth(String message) {
        return proxy.echoAuth(message);
    }

    @Override
    public Fach[] getFaecherList() {
        return proxy.getFaecherList();
    }

    @Override
    public Schueler getSchuelerMitFaechern(String benutzername) {
        return proxy.getSchuelerMitFaechern(benutzername);
    }

    @Override
    public Response index() {
        return proxy.index();
    }
}
