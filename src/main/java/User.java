import javax.swing.*;
public class User {
    private String Username;
    private String Password;

    public User(String pUsername, String pPassword) {
        Username = pUsername;
        Password = pPassword;
    }

    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }
}