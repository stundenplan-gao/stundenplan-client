import client.StundenplanClient;
import database.NeuerNutzer;
import database.Schueler;
import database.Stufe;
import util.GUIUtil;

import javax.swing.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    private final static String SERVER_URL;
    static {
        SERVER_URL = System.getProperty("stundenplan.url", "http://localhost:8080/stundenplan_server/stundenplan");
    }
    public StundenplanClient client;
    public GUI gui;
    private Schueler me;

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
                    if (!client.login(logData.getBenutzername(), logData.getPasswort().toCharArray())) {
                        String[] warningOps = new String[]{"Zurück", "Exit"};
                        int warning = JOptionPane.showOptionDialog(null, "Falsche Anmeldedaten.",
                                "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, warningOps, warningOps[0]);
                        if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                            System.exit(0);
                        }
                    }
                    me = client.getSchuelerMitFaechern(logData.getBenutzername());
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
            gui.setKurse(client.getKurse());
            gui.setSchueler(me);
            gui.setup();
        }
        else {
            System.exit(0);
        }
    }

    public String[] getStufen() {
        java.util.List<Stufe> stufen = Arrays.asList(client.getStufen());
        java.util.List<String> list = new ArrayList<>();
        for (Stufe s : stufen) {
            String stufe = s.getStufe();
            if (stufe != null)
                list.add(stufe);
        }
        String[] array = list.toArray(new String[0]);
        Arrays.sort(array);
        return array;
        //gui.setStufen(array);
    }

    public String getStufe() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Und jetzt wähl bitte noch deine Stufe aus:");
        JComboBox cbStufen = new JComboBox(getStufen());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(label);
        panel.add(cbStufen);
        String[] options = new String[]{"OK", "Cancel"};
        int op = JOptionPane.showOptionDialog(null, panel, "Deine Stufe",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, cbStufen);
        if(op == JOptionPane.OK_OPTION) {
            return  (String) cbStufen.getSelectedItem();
        }
        return "";
    }

    public void registration() {
        while (!client.isLoggedIn()) {
            NeuerNutzer logData = gui.register();
            if (logData != null) {
                Response response = client.registerUser(logData);
                if(response.getStatus() == 200) {
                    client.login(logData.getBenutzername(), logData.getPasswort().toCharArray());
                    me = client.getSchuelerMitFaechern(logData.getBenutzername());
                    me.setStufe(new Stufe(getStufe()));
                    client.storeSchuelerdaten(logData.getBenutzername(), me);
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