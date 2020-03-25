import client.StundenplanClient;
import database.NeuerNutzer;
import util.GUIUtil;

import javax.swing.*;
import javax.ws.rs.core.Response;

public class Client {
    private final static String SERVER_URL;
    static {
        SERVER_URL = System.getProperty("stundenplan.url", "http://localhost:8080/stundenplan_server/stundenplan");
    }
    public StundenplanClient client;
    public GUI gui;

    public Client() {
        gui = new GUI();
        GUIUtil.installApplicationIcons();
        GUIUtil.installBoundsPersistence(gui, "stundenplan", 600, 700);
        
        int n = JOptionPane.showConfirmDialog(null, "Hast du bereits einen Account?", "Anmeldung", JOptionPane.YES_NO_OPTION);
        client = new StundenplanClient(SERVER_URL);
        if((n == JOptionPane.NO_OPTION)) {
            registration();
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

    public void registration() {
        while (!client.isLoggedIn()) {
            NeuerNutzer logData = gui.register();
            if (logData != null) {
                Response response = client.registerUser(logData);
                if(response.getStatus() == 200) {
                    client.setToken(client.login(logData.getBenutzername(), logData.getPasswort().toCharArray()));
                }
                else if(response.getStatus() == 420) {
                    String[] opts = new String[]{"Try again", "Exit"};
                    int warning = JOptionPane.showOptionDialog(null, "Dein Username muss auf '@gao-online.de' enden!",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                    if(warning == JOptionPane.CANCEL_OPTION) {
                        break;
                    }
                }
                else if(response.getStatus() == 422) {
                    String[] opts = new String[]{"Try again", "Exit"};
                    int warning = JOptionPane.showOptionDialog(null, "Dein Username ist leider schon vergeben!",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                    if(warning == JOptionPane.CANCEL_OPTION) {
                        break;
                    }
                }
            }
            else {
                String[] opts = new String[]{"Try again", "Exit"};
                int warning = JOptionPane.showOptionDialog(null, "Ein oder mehrere Felder wurden nicht ausgefüllt!",
                        "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CANCEL_OPTION) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}