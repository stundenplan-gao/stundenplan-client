import client.StundenplanClient;
import database.NeuerNutzer;

import javax.swing.*;
import javax.ws.rs.core.Response;

public class Client {
    public StundenplanClient client;
    public GUI gui;

    public Client() {
        gui = new GUI();
        int n = JOptionPane.showConfirmDialog(null, "Hast du bereits einen Account?", "Anmeldung", JOptionPane.YES_NO_OPTION);
        client = new StundenplanClient("http://localhost:8080/stundenplan_server/stundenplan");
        if((n == JOptionPane.NO_OPTION)) {
            while (!client.isLoggedIn()) {
                NeuerNutzer logData = gui.register();
                if (logData != null) {
                    Response response = client.registerUser(logData);
                    if(response.getStatus() < 400) { //TODO muss zu '== 200' geändert werden
                        client.setToken(client.login(logData.getBenutzername(), logData.getPasswort().toCharArray()));
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Username oder Passwort falsch oder schon vergeben!",
                                "Warnung", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
        else {
            while (!client.isLoggedIn()) {
                NeuerNutzer logData = gui.login();
                if (logData != null) {
                    String token = client.login(logData.getBenutzername(), logData.getPasswort().toCharArray());
                    if (!token.isEmpty()) {
                        client.setToken(token);
                    } else {
                        JOptionPane.showMessageDialog(null, "Falsche Anmeldedaten.",
                                "Warnung", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
        if(client.isLoggedIn()) {
            gui.setFaecher(client.getFaecherList());
            gui.buildTimeTable();
        }
    }

    public static void main(String[] args) {
        Client obj = new Client();
    }
}