package client;

import database.Fach;
import database.Schueler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/schueler")
public interface StundenplanAPI {

    @GET
    @Path("/")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response index();

    // Test method
    @GET
    @Path("/echo")
    @Produces({ MediaType.TEXT_PLAIN })
    public String echo(@QueryParam("message") String message);

    // Test method
    @GET
    @Path("/echo_auth")
    @Produces({ MediaType.TEXT_PLAIN })
    public String echoAuth(@QueryParam("message") String message);

    @POST
    @Path("/login")
    @Produces({ MediaType.TEXT_PLAIN })
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON })
    public String authenticateUser(@QueryParam("username") String username,
                                   @QueryParam("password") String password);

    @GET
    @Path("/faecherauswahl")
    @Produces({ MediaType.APPLICATION_JSON })
    public Fach[] getFaecherList();

    @GET
    @Path("/schueler-mit-faechern/${benutzername}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Schueler getSchuelerMitFaechern(@PathParam("benutzername") String benutzername);
}
