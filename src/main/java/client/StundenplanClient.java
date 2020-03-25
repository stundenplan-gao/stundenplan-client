package client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import database.*;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
            /*String body = new String(responseContext.getEntityStream().readAllBytes());

            if (!body.equals("") && responseContext.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                Object jsonObject = mapper.readValue(body, Object.class);

                String prettyFormatted = mapper.writeValueAsString(jsonObject);
                System.err.println(prettyFormatted);
            } else {
                System.err.println(body);
            }

            responseContext.setEntityStream(new ByteArrayInputStream(body.getBytes()));*/
        }

    }

    public static void main(String... args) {
        StundenplanClient c = new StundenplanClient("ysprenger", "ysprenger".toCharArray(), "http://localhost:8080/stundenplan_srver/stundenplan");
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

    public StundenplanClient(String username, char[] passwd, String url) {
        client = (ResteasyClient) ClientBuilder.newBuilder().register(new JWTFilter()).build();
        target = client.target(url);
        proxy = target.proxy(StundenplanAPI.class);
        token = proxy.authenticateUser(username, new String(passwd));
    }

    public StundenplanClient(String url) {
        client = (ResteasyClient) ClientBuilder.newBuilder().register(new JWTFilter()).build();
        target = client.target(url);
        proxy = target.proxy(StundenplanAPI.class);
        token = "";
    }

    public boolean isLoggedIn() {
        return token != null && !token.equals("");
    }

    public boolean checkConnection() {
        String testMsg = "Testing connection...";
        return testMsg.equals(echo(testMsg));
    }

    public boolean checkAuth() {
        String testMsg = "Testing connection...";
        return testMsg.equals(echoAuth(testMsg));
    }

    public boolean login(String username, char[] password) {
        String token = proxy.authenticateUser(username, new String(password));
        setToken(token);
        return token.isEmpty();
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String authenticateUser(String username, String password) {
        return proxy.authenticateUser(username, password);
    }

    @Override
    public Response registerUser(NeuerNutzer nutzer) {
        return proxy.registerUser(nutzer);
    }

    @Override
    public Response deleteUser(String username) {
        return proxy.deleteUser(username);
    }

    @Override
    public String echo(String message) {
        return proxy.echo(message);
    }

    @Override
    public String echoAuth(String message) {
        try {
            return proxy.echoAuth(message);
        } catch (NotAuthorizedException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public Fach[] getFaecherList() {
        try {
            return proxy.getFaecherList();
        } catch (NotAuthorizedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Schueler getSchuelerMitFaechern(String benutzername) {
        try {
            return proxy.getSchuelerMitFaechern(benutzername);
        } catch (NotAuthorizedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Response index() {
        return proxy.index();
    }

    @GET
    @Path("/kurse")
    @Produces({MediaType.APPLICATION_JSON})
    public Kurs[] getKurse() {
        return proxy.getKurse();
    }

    @Override
    public Response storeSchuelerdaten(String benutzername, Kurs[] kurse) {
        return proxy.storeSchuelerdaten(benutzername, kurse);
    }

    @Override
    public Entfall[] getEntfaelle() {
        return proxy.getEntfaelle();
    }

    public void close() {
        client.close();
    }
}
