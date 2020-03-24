import javax.swing.*;

public class Client {
    public StundenplanClient client;
    public GUI gui;

    public Client() {
        gui = new GUI();
        int n = JOptionPane.showConfirmDialog(null, "Hast du bereits einen Account?", "Anmeldung", JOptionPane.YES_NO_OPTION);
        boolean isLoggedIn = false;
        if((n == JOptionPane.NO_OPTION)) {
            Credentials logData = gui.register();
            if(logData != null) {
                client = new StundenplanClient(logData.getUsername(), logData.getPassword());
                isLoggedIn = true;
            }
        }
        else {
            Credentials logData = gui.login();
            if(logData != null) {
                client = new StundenplanClient(logData.getUsername(), logData.getPassword());
                isLoggedIn = true;
            }
        }
        if(isLoggedIn) {
            gui.setFaecher(client.getFaecherList());
            gui.buildTimeTable();
        }
    }

    public static void main(String[] args) {
        Client obj = new Client();
    }
}