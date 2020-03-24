import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class GUI extends JFrame implements ActionListener{
    private List<Data> timetable;

    private JFrame frameTT;
    private JFrame frameRP;

    private JMenuBar mb;
    private JMenuItem m11;
    private JMenuItem m12;

    private JLabel lDay;
    private JLabel lTime;

    private JButton bVertretungsplan;
    private JButton bStundenplan;

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

        //buildTimeTable();
    }

    public Credentials register() {
        final String username = JOptionPane.showInputDialog(null, "Gib einen Usernamen ein:", "Registrierung", JOptionPane.QUESTION_MESSAGE);
        if(username != null) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Gib ein Passwort ein: ");
            JPasswordField pass = new JPasswordField(20);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(label);
            panel.add(pass);

            String[] options = new String[]{"OK", "Cancel"};

            int option = JOptionPane.showOptionDialog(null, panel, "Registrierung",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, pass);

            if (option == JOptionPane.OK_OPTION) {
                char[] pw = pass.getPassword();
                return new Credentials(username, pw);
            }
            else {
                JOptionPane.showMessageDialog(null, "Du musst ein Passwort eingeben,\n" + "um dich zu regestrieren.",
                        "Warnung", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Du musst einen Usernamen eingeben,\n" + "um dich zu regestrieren.",
                    "Warnung", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    public Credentials login() {
        final String username = JOptionPane.showInputDialog(null, "Username:", "Anmeldung", JOptionPane.QUESTION_MESSAGE);
        if(username != null) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Passwort: ");
            JPasswordField pass = new JPasswordField(20);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(label);
            panel.add(pass);

            String[] options = new String[]{"OK", "Cancel"};

            int option = JOptionPane.showOptionDialog(null, panel, "Anmeldung",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, pass);

            if (option == JOptionPane.OK_OPTION) {
                char[] pw = pass.getPassword();
                return new Credentials(username, pw);
            }
            else {
                JOptionPane.showMessageDialog(null, "Du musst ein Passwort eingeben,\n" + "um dich anzumelden.",
                        "Warnung", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Du musst einen Usernamen eingeben,\n" + "um dich anzumelden.",
                    "Warnung", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    public void buildTimeTable() {
        frameTT = new JFrame("Stundenplan");
        frameTT.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameTT.setSize(600, 440);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel topPanel = new JPanel();
        JPanel pHead1 = new JPanel();
        lDay = new JLabel("Heute ist " + getDayOfWeek());
        lTime = new JLabel("und es ist " + getTime() + " Uhr");
        bVertretungsplan = new JButton("zum Vertretungsplan");
        bVertretungsplan.addActionListener(this);
        pHead1.add(lDay);
        pHead1.add(lTime);
        pHead1.add(bVertretungsplan);

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
                        JOptionPane.showMessageDialog(frameTT, "Warten auf Server Antwort.", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        Object s = JOptionPane.showInputDialog(frameTT,
                                "Welches Fach hast du da?",
                                "Fachauswahl",
                                JOptionPane.PLAIN_MESSAGE,
                                null, faecher,
                                null);
                        if (s != null) {
                            String t = (String) JOptionPane.showInputDialog(frameTT,
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
        topPanel.add(pHead1);
        topPanel.add(pTT);

        tabbedPane.addTab("Stundenplan", null, topPanel);

        //frameTT.getContentPane().add(BorderLayout.CENTER, pHead1);
        //frameTT.getContentPane().add(BorderLayout.WEST, pTT);
        frameTT.getContentPane().add(BorderLayout.NORTH, mb);
        frameTT.getContentPane().add(BorderLayout.CENTER, tabbedPane);
        frameTT.setVisible(true);
    }

    public void buildRepresentationPlan() {
        frameRP = new JFrame("Vertretungsplan");
        frameRP.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameRP.setSize(500, 400);

        JPanel pHead2 = new JPanel();
        lDay = new JLabel("Heute ist " + getDayOfWeek());
        lTime = new JLabel("und es ist " + getTime() + " Uhr");
        bStundenplan = new JButton("zum Stundenplan");
        bStundenplan.addActionListener(this);
        pHead2.add(lDay);
        pHead2.add(lTime);
        pHead2.add(bStundenplan);

        JPanel pRP = new JPanel();
        JLabel lRP = new JLabel("hier kommt der Vertretungsplan hin");
        pRP.add(lRP);

        frameRP.getContentPane().add(BorderLayout.CENTER, pHead2);
        frameRP.getContentPane().add(BorderLayout.WEST, pRP);
        frameRP.getContentPane().add(BorderLayout.NORTH, mb);
        frameRP.setVisible(true);
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
        if(src == bVertretungsplan) {
            frameTT.setVisible(false);
            buildRepresentationPlan();
        }
        else if(src == bStundenplan) {
            frameRP.setVisible(false);
            buildTimeTable();
        }
        else if(src == m11) { //reset
            System.exit(0);
            //buildTimeTable();
        }
        else if(src == m12) { // close
            System.exit(0);
        }
    }
}