import client.StundenplanClient;
import database.Kurs;
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
        gui = new GUI();//Eine GUI wird erstellt
        GUIUtil.installApplicationIcons();
        GUIUtil.installBoundsPersistence(gui, "stundenplan", 500, 440);

        client = new StundenplanClient(SERVER_URL);//Erstellt einen neuen StundenplanClient

        String[] options = new String[]{"Anmelden", "Registrieren", "Exit"};//Auswahloptionen für den Dialog

        //Erster Dialog, der angezeigt wird: "Hast du bereits einen Account?" Mit drei Auswahlmöglichkeiten
        int n = JOptionPane.showOptionDialog(null, "Hast du bereits einen Account?", "Anmeldung",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if((n == JOptionPane.NO_OPTION)) { //"Registrieren" wurde gewählt
            registration();
        }
        else if(n == JOptionPane.YES_OPTION) { //"Anmelden" wurde gewählt
            while (!client.isLoggedIn()) { //while wird solange ausgeführt, bis der User angemeldet wurde (client.login())
                NeuerNutzer logData = gui.login();
                if (logData != null) { //true, wenn in der Methode login() in GUI kein null zurückgegeben wurde
                    /*
                    client.login() gibt einen boolean zurück, ob das einloggen geklappt hat,
                    somit wird in der If-Bedingung der Nutzer direkt eingeloggt und eine Warnung gegeben, wenn
                    das einloggen nicht geklappt hat
                    */
                    if (!client.login(logData.getBenutzername(), logData.getPasswort().toCharArray())) {
                        String[] warningOps = new String[]{"Zurück", "Exit"};
                        int warning = JOptionPane.showOptionDialog(null, "Falsche Anmeldedaten.",
                                "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, warningOps, warningOps[0]);
                        if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                            System.exit(0);
                        }
                    }
                    else { //bei erfolgreichem einloggen wird direkt ein Schüler des gerade eingeloggten Schülers aus der Datenbank gezogen
                        me = client.getSchuelerMitFaechern(logData.getBenutzername());
                    }
                }
                else { //bei falscher oder fehlerhaften Anmeldung, kann diese entweder Wiederholt oder Abgebrochen werden
                    String[] warningOps = new String[]{"Zurück", "Exit"};//Auswahloptionen für den Dialog
                    int warning = JOptionPane.showOptionDialog(null, "Username oder Passwort wurde nicht ausgefüllt.",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, warningOps, warningOps[0]);
                    if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                        System.exit(0);
                    }
                }
            }
        }
        //wenn bei dem ersten Dialogfenster "Exit" gedrückt oder das Fenster geschlossen wurde, wird die Anwendung beendet
        else if(n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        //wenn der Benutzer angemeldet wurde (siehe oben), werden die notwendigen Daten an die GUI übergeben und die GUI anschließend aufgebaut
        if(client.isLoggedIn()) {
            gui.setKurse(client.getKurse());
            gui.addKursListener(this::updateKurse);
            gui.setSchueler(me);
            gui.setup();
        }
        else {
            System.exit(0);
        }
    }

    public void updateKurse(Kurs[] kurse) {
        client.storeSchuelerKurse(me.getBenutzername(), kurse);
    }

    /**
     * Eine Methode, welche alle verfügbaren Stufen aus der Datenbank als String[] zurückgibt
     */
    public String[] getStufen() {
        //speichert alle Stufen aus der Datenbank in einer Liste<Stufe> der Klasse Stufe
        java.util.List<Stufe> stufen = Arrays.asList(client.getStufen());
        java.util.List<String> list = new ArrayList<>();//eine leere String Liste wird erstellt
        //es werden alle stufen, die nicht null sind, der Liste list hinzugefügt
        for (Stufe s : stufen) {
            String stufe = s.getStufe();
            if (stufe != null)
                list.add(stufe);
        }
        String[] array = list.toArray(new String[0]);//die Liste wird zum String Array konvertiert
        Arrays.sort(array);
        return array;//und zurückgegeben
    }

    /**
     * Methode, welche eine aus einem Dialogfenster ausgewählte Stufe zurückgibt
     */
    public String getStufe() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Und jetzt wähl bitte noch deine Stufe aus:");
        //eine ComboBox mit der Auswahl aller verfügbaren Stufen (erhalten von getStufen())
        JComboBox cbStufen = new JComboBox(getStufen());

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));//vertikale Anordnung der Elemente
        panel.add(label);
        panel.add(cbStufen);

        String[] options = new String[]{"OK", "Cancel"};//Auswahloptionen
        int op = JOptionPane.showOptionDialog(null, panel, "Deine Stufe",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, cbStufen);
        if(op == JOptionPane.OK_OPTION) {
            return  (String) cbStufen.getSelectedItem();//gibt die ausgewählte Stufe zurück
        }
        return "";
    }

    /**
     * Methode um einen neuen Nutzer zu registrieren
     */
    public void registration() {
        while (!client.isLoggedIn()) { //while wird solange ausgeführt, bis der User angemeldet wurde (client.login())
            NeuerNutzer logData = gui.register();
            if (logData != null) { //true, wenn in der Methode register() in GUI kein null zurückgegeben wurde
                //Gibt eine 'Response' zurück, je nach dem, ob der Server den neuen Nutzer 'NeuerNutzer' registrieren konnte, oder nicht
                Response response = client.registerUser(logData);
                if(response.getStatus() == 200) { //client.registerUser() war erfolgreich und der neue Nutzer wurde angelegt
                    //Nutzer wird eingeloggt (while wird false)
                    client.login(logData.getBenutzername(), logData.getPasswort().toCharArray());
                    //ein Schüler 'me' wird mit den registrierten Daten aus der Datenbank gezogen
                    me = client.getSchuelerMitFaechern(logData.getBenutzername());
                    //die Stufe des neuen Schülers wird mit getStufe() erfragt und sofort gesetzt
                    me.setStufe(new Stufe(getStufe()));
                    //und anschließend direkt in der Datenbank gespeichert
                    client.storeSchuelerdaten(logData.getBenutzername(), me);
                }
                else if(response.getStatus() == 420) { //client.registerUser() ist fehlgeschlagen mit der Response '420'
                    String[] opts = new String[]{"Zurück", "Exit"};//Auswahloptionen
                    //Dialog mit der entsprechenden Fehlermeldung und den Optionen "zurück" oder "exit" wird erstellt
                    int warning = JOptionPane.showOptionDialog(null, "Dein Username muss auf '@gao-online.de' enden!",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                    if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                        break;//while wird beendet wenn "Exit" gewählt oder das Fenster geschlossen wurde
                    }
                }
                else if(response.getStatus() == 422) { //client.registerUser() ist fehlgeschlagen mit der Response '422'
                    String[] opts = new String[]{"Zurück", "Exit"};//Auswahloptionen
                    //Dialog mit der entsprechenden Fehlermeldung und den Optionen "zurück" oder "exit" wird erstellt
                    int warning = JOptionPane.showOptionDialog(null, "Dein Username ist leider schon vergeben!",
                            "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                    if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                        break;//while wird beendet wenn "Exit" gewählt oder das Fenster geschlossen wurde
                    }
                }
            }
            else { //wenn gui.register() null zurückgegeben hat
                String[] opts = new String[]{"Zurück", "Exit"};//Auswahloptionen
                //Dialog mit der entsprechenden Fehlermeldung und den Optionen "zurück" oder "exit" wird erstellt
                int warning = JOptionPane.showOptionDialog(null, "Ein oder mehrere Felder wurden nicht ausgefüllt!",
                        "Warnung", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                if(warning == JOptionPane.NO_OPTION || warning == JOptionPane.CLOSED_OPTION) {
                    break;//while wird beendet wenn "Exit" gewählt oder das Fenster geschlossen wurde
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}