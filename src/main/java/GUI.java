import database.NeuerNutzer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class GUI extends JFrame implements ActionListener{
    private List<Data> timetable;

    private JFrame frame;

    private JMenuBar mb;
    private JMenuItem m11;
    private JMenuItem m12;

    private Object[] faecher;

    public GUI() {
        timetable = new List<>();

        mb = new JMenuBar();
        JMenu m1 = new JMenu("File");
        mb.add(m1);
        m11 = new JMenuItem("Reset");
        m12 = new JMenuItem("Close");
        m1.add(m11);
        m11.addActionListener(this);
        m1.add(m12);
        m12.addActionListener(this);
        JMenu m2 = new JMenu("Help");
        mb.add(m2);
        JMenuItem m21 = new JMenuItem("placeholder1");
        JMenuItem m22 = new JMenuItem("placeholder2");
        m2.add(m21);
        m21.addActionListener(this);
        m2.add(m22);
        m22.addActionListener(this);
    }

    public NeuerNutzer register() {
        JPanel panel = new JPanel();
        JLabel lfn = new JLabel("Gib hier deinen Vornamen ein:");
        JTextField tfn = new JTextField();
        JLabel lln = new JLabel("Gib hier deinen Nachnamen ein:");
        JTextField tln = new JTextField();
        JLabel lun = new JLabel("Gib hier einen Usernamen ein:");
        JTextField tun = new JTextField();
        JLabel lpw = new JLabel("Gib hier ein Passwort ein:");
        JPasswordField passF = new JPasswordField(20);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(lfn);
        panel.add(tfn);
        panel.add(lln);
        panel.add(tln);
        panel.add(lun);
        panel.add(tun);
        panel.add(lpw);
        panel.add(passF);

        String[] options = new String[]{"OK", "Cancel"};

        int op = JOptionPane.showOptionDialog(null, panel, "Registrierung",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, tfn);
        if(!tfn.getText().isEmpty() || !tln.getText().isEmpty() || !tun.getText().isEmpty() || passF.getPassword().length != 0) {
            if (op == JOptionPane.OK_OPTION) {
                String firstname = tfn.getText();
                String lastname = tln.getText();
                String username = tun.getText();
                char[] password = passF.getPassword();
                return new NeuerNutzer(firstname, lastname, username, new String(password));
            }
            else {
                return null;
            }
        }
        else {
            //JOptionPane.showMessageDialog(null, "Ein oder mehrere Felder wurden nicht ausgefüllt!", "Warnung", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    public NeuerNutzer login(){
        JPanel panel = new JPanel();
        JLabel lun = new JLabel("Username:");
        JTextField tun = new JTextField("ysprenger");
        JLabel lpw = new JLabel("Passwort:");
        JPasswordField passF = new JPasswordField("ysprenger", 20);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(lun);
        panel.add(tun);
        panel.add(lpw);
        panel.add(passF);

        String[] options = new String[]{"OK", "Cancel"};

        int op = JOptionPane.showOptionDialog(null, panel, "Anmeldung",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, tun);
        if(!tun.getText().isEmpty() || passF.getPassword().length != 0) {
            if (op == JOptionPane.OK_OPTION) {
                String username = tun.getText();
                char[] password = passF.getPassword();
                return new NeuerNutzer("", "", username, new String(password));
            }
            else {
                return null;
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Username oder Passwort wurden nicht ausgefüllt!",
                    "Warnung", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    public void buildTimeTable() {
        frame = new JFrame("Stundenplan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 440);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel topPanel = new JPanel();
        JPanel pHead = new JPanel();
        JLabel lDay = new JLabel("<html>Heute ist <font color='#008B8B'>" + getDayOfWeek() + "</font></html>");
        JLabel lTime = new JLabel("<html>und es ist <font color='#008B8B'>" + getTime() + "</font> Uhr</html>");
        JButton bVertretungsplan = new JButton("zum Vertretungsplan");
        bVertretungsplan.addActionListener(this);
        pHead.add(lDay);
        pHead.add(lTime);
        pHead.add(bVertretungsplan);

        JPanel pTT = new JPanel(new GridLayout(10, 5, 10, 10));
        JLabel[] jLabels = new JLabel[50];
        for(int i = 0; i < 50; i++) {
            jLabels[i] = new JLabel("| Dein Fach ");
            jLabels[i].setForeground(Color.gray);
            jLabels[i].setHorizontalTextPosition(JLabel.LEFT);
            pTT.add(jLabels[i]);
        }
        for(int j = 0; j < jLabels.length; j++) {
            int finalJ = j;
            jLabels[j].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(faecher == null) {
                        JOptionPane.showMessageDialog(frame, "Warten auf Server Antwort.", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        Object s = JOptionPane.showInputDialog(frame,
                                "Welches Fach hast du da?",
                                "Fachauswahl",
                                JOptionPane.PLAIN_MESSAGE,
                                null, faecher,
                                null);
                        if (s != null) {
                            String t = (String) JOptionPane.showInputDialog(frame,
                                    "Welchen Lehrer hast du da?\n" +
                                    "(Gib den Lehrerkürzel ein)",
                                    "Lehrerauswahl",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    null);
                            jLabels[finalJ].setText("| " + s);
                            if(t != null && !t.equals("")) {
                                jLabels[finalJ].setForeground(Color.black);
                                Data temp = new Data(s.toString(), t, finalJ);
                                add(temp);
                            }
                            else {
                                jLabels[finalJ].setForeground(Color.red);
                                JOptionPane.showMessageDialog(null, "Du musst noch einen Lehrer für das Fach festlegen.",
                                        "Warnung", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            });
        }

        topPanel.add(pHead);
        topPanel.add(pTT);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel pRP = new JPanel();
        JLabel lRP = new JLabel("hier kommt der Vertretungsplan hin");
        pRP.add(lRP);

        tabbedPane.addTab("Stundenplan", null, topPanel);
        tabbedPane.addTab("Vertretungsplan", null, pRP);

        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, tabbedPane);
        frame.setVisible(true);
    }

    public void add(Data d) {
        if(d != null) {
            if(d != searchFile(d)) {
                timetable.append(d);
            }
            else {
                timetable.setContent(d);
            }
        }
    }

    public Data searchFile(Data d) {
        if(!timetable.isEmpty()) {
            timetable.toFirst();
            while(timetable.hasAccess() && d.getID() != timetable.getContent().getID()) {
                timetable.next();
            }
            if(!timetable.hasAccess()) {
                return(null);
            }
            return(timetable.getContent());
        }
        else {
            return(null);
        }
    }

    public String getDayOfWeek() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTimeZone(TimeZone.getTimeZone("MET"));
        int DoW = cal.get(Calendar.DAY_OF_WEEK);
        switch (DoW) {
            case 2:
                return "Montag";
            case 3:
                return "Dienstag";
            case 4:
                return "Mittwoch";
            case 5:
                return "Donnerstag";
            case 6:
                return "Freitag";
            case 7:
                return "Samstag";
            case 1:
                return "Sonntag";
            default:
                return null;
        }
    }

    public String getTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTimeZone(TimeZone.getTimeZone("MET"));
        Date date = cal.getTime();
        SimpleDateFormat f = new SimpleDateFormat("HH:mm");
        return f.format(date);
    }

    public void setFaecher(Object[] faecher) {
        SwingUtilities.invokeLater(() -> this.faecher = faecher);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == m11) { //reset
            buildTimeTable();
        }
        else if(src == m12) { // close
            System.exit(0);
        }
    }
}