import client.StundenplanClient;
import database.NeuerNutzer;
import database.Schueler;
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
    private Schueler my;

    public Client() {
        gui = new GUI();
        GUIUtil.installApplicationIcons();
        GUIUtil.installBoundsPersistence(gui, "stundenplan", 500, 440);

        client = new StundenplanClient(SERVER_URL);
        String[] options = new String[]{"Anmelden", "Registrieren", "Exit"};
        int n = JOptionPane.showOptionDialog(null, "Hast du bereits einen Account?", "Anmeldung",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if((n == JOptionPane.NO_OPTION)) {
            registration();
        }
        else if(n == JOptionPane.YES_OPTION) {
            while (!client.isLoggedIn()) {
                NeuerNutzer logData = gui.login();
                if (logData != null) {
                    //String token = client.login(logData.getBenutzername(), logData.getPasswort().toCharArray());
                    if (!client.login(logData.getBenutzername(), logData.getPasswort().toCharArray())) {
                        String[] warningOps = new String[]{"Zurück", "Exit"};
                        int warning = JOptionPane.showOptionDialog(null, "Falsche Anmeldedaten.",
                                "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, warningOps, warningOps[0]);
                        if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                            System.exit(0);
                        }
                    }
                    my = client.getSchuelerMitFaechern(logData.getBenutzername());
                }
                else {
                    String[] warningOps = new String[]{"Zurück", "Exit"};
                    int warning = JOptionPane.showOptionDialog(null, "Username oder Passwort wurde nicht ausgefüllt.",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, warningOps, warningOps[0]);
                    if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                        System.exit(0);
                    }
                }
            }
        }
        else if(n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        if(client.isLoggedIn()) {
            gui.setFaecher(client.getFaecherList());
            gui.setup();
        }
        else {
            System.exit(0);
        }
    }

    public void registration() {
        while (!client.isLoggedIn()) {
            NeuerNutzer logData = gui.register();
            if (logData != null) {
                Response response = client.registerUser(logData);
                if(response.getStatus() == 200) {
                    client.login(logData.getBenutzername(), logData.getPasswort().toCharArray());
                    my = client.getSchuelerMitFaechern(logData.getBenutzername());
                }
                else if(response.getStatus() == 420) {
                    String[] opts = new String[]{"Zurück", "Exit"};
                    int warning = JOptionPane.showOptionDialog(null, "Dein Username muss auf '@gao-online.de' enden!",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                    if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                        break;
                    }
                }
                else if(response.getStatus() == 422) {
                    String[] opts = new String[]{"Zurück", "Exit"};
                    int warning = JOptionPane.showOptionDialog(null, "Dein Username ist leider schon vergeben!",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                    if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                        break;
                    }
                }
            }
            else {
                String[] opts = new String[]{"Zurück", "Exit"};
                int warning = JOptionPane.showOptionDialog(null, "Ein oder mehrere Felder wurden nicht ausgefüllt!",
                        "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}