import database.*;
import util.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;

public class GUI extends JFrame implements ActionListener {
    private List<Data> timetable;
    private Schueler schueler;

    private JFrame frame;

    private JMenuBar mb;
    private JMenuItem m11;
    private JMenuItem m12;

    private JTabbedPane tabbedPane;

    private JButton bVertretungsplan;

    private Kurs[] alleKurse;
    private KursListener kursListener;

    public GUI() {
        timetable = new List<>();
    }

    /**
     * Methode zur Aufnahme und Rückgabe (als 'NeuerNutzer') der Daten eines neuen Nutzers per Dialogfenster
     */
    public NeuerNutzer register() {
        JPanel panel = new JPanel();
        JLabel lfn = new JLabel("Gib hier deinen Vornamen ein:");
        JTextField tfn = new JTextField();//Textfeld zum eingeben des Vornamens

        JLabel lln = new JLabel("Gib hier deinen Nachnamen ein:");
        JTextField tln = new JTextField();//Textfeld zum eingeben des Nachnamens

        JLabel lun = new JLabel("Gib hier einen Usernamen ein:");
        JTextField tun = new JTextField();//Textfeld zum eingeben des Usernames

        JLabel lpw = new JLabel("Gib hier ein Passwort ein:");
        JPasswordField passF = new JPasswordField(20);//Passwort Feld zum eingeben des Passworts

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));//Die einzelnen Labels und Felder werden untereinander angeordenet
        panel.add(lfn);//alle Elemente werden dem Panel hinzugefügt
        panel.add(tfn);
        panel.add(lln);
        panel.add(tln);
        panel.add(lun);
        panel.add(tun);
        panel.add(lpw);
        panel.add(passF);

        String[] options = new String[]{"OK", "Cancel"};//Die Optionen für die Buttons werden gesetzt

        //Es wird ein Dialog mit dem obigem Panel erstellt
        int op = JOptionPane.showOptionDialog(null, panel, "Registrierung",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, tfn);
        //Es wird geprüft, ob alle Felder einen Wert haben und wenn nicht, wird null zurückgegeben
        if (!tfn.getText().isEmpty() || !tln.getText().isEmpty() || !tun.getText().isEmpty() || passF.getPassword().length != 0) {
            //Anschließend wird überprüft, ob OK gedrückt worden ist, falls nicht, wird null returnt
            if (op == JOptionPane.OK_OPTION) {
                //Die Werte werden gesetzt und als "NeuerNutzer" zurückgegeben
                String firstname = tfn.getText();
                String lastname = tln.getText();
                String username = tun.getText();
                char[] password = passF.getPassword();
                return new NeuerNutzer(firstname, lastname, username, new String(password));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Diese Methode nimmt die Anmeldedaten eines bereits bekannten Users per Dialog auf und gibt sie als 'NeuerNutzer' zurück.
     */
    public NeuerNutzer login() {
        JPanel panel = new JPanel();
        JLabel lun = new JLabel("Username:");
        JTextField tun = new JTextField("");//Ein Textfeld für den Usernamen

        JLabel lpw = new JLabel("Passwort:");
        JPasswordField passF = new JPasswordField(20);//Ein Passwort Feld für das Passwort

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));//Vertikale Anordnung der Labels und Textfelder
        panel.add(lun);
        panel.add(tun);
        panel.add(lpw);
        panel.add(passF);

        String[] options = new String[]{"OK", "Cancel"};//Auswahloptionen

        //Dialog mit dem Panel als Inhalt
        int op = JOptionPane.showOptionDialog(null, panel, "Anmeldung",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, tun);
        if (!tun.getText().isEmpty() || passF.getPassword().length != 0) {//Fortfahren nur wenn die Felder einen Wert haben und
            if (op == JOptionPane.OK_OPTION) {//die "OK" Option ausgewählt wurde
                //Werte werden gesetzt und als "NeuerNutzer" (nur mit Benutzernamen und Passwort) zurückgegeben
                String username = tun.getText();
                char[] password = passF.getPassword();
                return new NeuerNutzer("", "", username, new String(password));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Aufbau der Menuleiste
     */
    public void setup() {
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
        //Anschließend der Aufbau der restlichen GUI
        buildTimeTable();
    }

    /**
     * Aufbau der GUI
     */
    public void buildTimeTable() {
        frame = new JFrame("Stundenplan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 440);
        GUIUtil.installBoundsPersistence(frame, "stundenplan", 500, 440);

        tabbedPane = new JTabbedPane();//TabbedPane für verschiedene Tabs (Stundenplan und Vertretungsplan)

        JPanel topPanel = new JPanel();//Ein 'übergeordnetes' Panel

        //Der 'Kopf' der GUI mit Wochentag und Uhrzeit
        JPanel pHead = new JPanel();
        JLabel lDay = new JLabel("<html>Heute ist <font color='#008B8B'><b>" + getDayOfWeek() + "</b></font></html>");//Färbung des Tages und der Uhrzeit
        JLabel lTime = new JLabel("<html>und es ist <font color='#008B8B'><b>" + getTime() + "</b></font> Uhr</html>");// mit HTML Befehlen
        bVertretungsplan = new JButton("<html><i>zum <u>V</u>ertretungsplan</i></html>");
        bVertretungsplan.addActionListener(this);

        pHead.add(lDay);
        pHead.add(lTime);
        pHead.add(bVertretungsplan);

        //'TimeTable' Panel mit 5x10 Stunden, also 50 Wochenstunden
        JPanel pTT = new JPanel(new GridLayout(10, 5, 10, 10));//integriert in ein GridLayout

        //50 Label für 50 Wochenstunden
        JLabel[] jLabels = new JLabel[50];
        Set<Kurs> kurse = schueler.getKurse();
        /*
        Abfrage ob der angemeldete Schüler schon einen Stundenplan bzw. gesetzte Kurse hat mit
        anschließendem Auslegen der Stunden in die Label
         */
        if (!kurse.isEmpty()) {
            for (Kurs k : kurse) {
                String fach = k.getFach().getFach();
                Set<Stunde> stunden = k.getStunden();
                for (Stunde s : stunden) {
                    jLabels[(s.getStunde() - 1) * 5 + (s.getTag() - 1)] = new JLabel(fach);
                }
            }
        }
        for (int i = 0; i < 50; i++) {
            //Es werden nur dann 'leere' Label erstellt, wenn der Schüler noch keinen Stundenplan bzw. Fächer gesetzt hat
            if (jLabels[i] == null) {
                jLabels[i] = new JLabel("Frei");
                jLabels[i].setForeground(Color.gray);
            }
            jLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            pTT.add(jLabels[i]);//alle Labels werden dem 'TimeTable' Panel hinzugefügt
        }
        for (int j = 0; j < jLabels.length; j++) {
            final short tag = (short) ((j % 5) + 1);
            final short stunde = (short) (j / 5 + 1);
            final Stunde index = new Stunde(tag, stunde);//die 'Koordinaten' des ausgewählten Labels werden als 'Stunde' gespeichert
            jLabels[j].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ArrayList<Kurs> kurse = new ArrayList<>(Arrays.asList(alleKurse));
                    //Es werden alle Kurse aussortiert, welche nicht an diesem Tag, zu dieser Stunde, in dieser Stufe stattfinden
                    kurse.removeIf(k ->
                            !k.getStufe().equals(schueler.getStufe()) ||
                                    !k.getStunden().contains(index)
                    );
                    ArrayList<Fach> fachArrayList = new ArrayList<>();
                    for (Kurs k : kurse) {
                        if (!fachArrayList.contains(k.getFach())) {
                            fachArrayList.add(k.getFach());
                        }
                    }
                    //Dialog in welchem man sein Fach auswählt
                    Object fach = JOptionPane.showInputDialog(frame,
                            "Welches Fach hast du da?",
                            "Fachauswahl",
                            JOptionPane.PLAIN_MESSAGE,
                            null, fachArrayList.toArray(),
                            null);

                    if (fach != null) {
                        //Es werden alle Kurse aussortiert, welche nicht dem zuvor ausgewähltem Fach entsprechen
                        kurse.removeIf(k -> !k.getFach().equals(fach));
                        ArrayList<Lehrer> lehrerArrayList = new ArrayList<>();
                        for (Kurs k : kurse) {
                            if (!lehrerArrayList.contains(k.getLehrer())) {
                                lehrerArrayList.add(k.getLehrer());
                            }
                        }
                        //Dialog, in dem man den (meist eizigen) Lehrer des Faches auswählt
                        Lehrer lehrer = (Lehrer) JOptionPane.showInputDialog(frame,
                                "Welchen Lehrer hast du da?",
                                "Lehrerauswahl",
                                JOptionPane.PLAIN_MESSAGE,
                                null, lehrerArrayList.toArray(),
                                null);

                        if (lehrer != null) {
                            //wenn das Fach und der Lehrer gesetzt wurden, wird das Label schwarz und die genaue Kursbezeichnung wird gespeichert

                            kurse.removeIf(k -> !k.getLehrer().equals(lehrer));
                            Kurs k = kurse.get(0);
                            for(Stunde s : k.getStunden()) {
                                jLabels[(s.getStunde() - 1) * 5 + (s.getTag() - 1)].setText(""+fach);
                                jLabels[(s.getStunde() - 1) * 5 + (s.getTag() - 1)].setForeground(Color.black);
                            }
                            ArrayList<Kurs> alleKurseArrayList = new ArrayList<>(Arrays.asList(alleKurse));
                            alleKurseArrayList.add(k);
                            storeKurse(alleKurseArrayList.toArray(new Kurs[0]));
                        }
                        else {
                            //Fehlermeldung wenn der Lehrer nicht gesetzt wurde
                            JOptionPane.showMessageDialog(null, "Du musst noch einen Lehrer für das Fach festlegen.",
                                    "Warnung", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            });
        }
        topPanel.add(pHead);//Dem übergeordnetem Panel 'topPanel' werden die anderen Panel 'Head' und
        topPanel.add(pTT);// 'TimeTable' hinzugefügt
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));//vertikale Anordnung

        JPanel pRP = new JPanel();//Ein Panel für den Vertretungsplan 'RepresentationPlan' wird erstellt
        JLabel lRP = new JLabel("hier kommt der Vertretungsplan hin");
        pRP.add(lRP);

        JScrollPane scrollPane = new JScrollPane(topPanel);//'topPanel' wird einem scrollPane hinzugefügt

        tabbedPane.addTab("Stundenplan", null, scrollPane);//Es werden zwei Tabs für den Stundenplan und
        tabbedPane.addTab("Vertretungsplan", null, pRP);//   den Vertretungsplan erstellt

        frame.getContentPane().add(BorderLayout.NORTH, mb);//Die Menuleiste erhält ihren Platz ganz oben im Frame
        frame.getContentPane().add(BorderLayout.CENTER, tabbedPane);//und die Tabs in der Mitte
        frame.setVisible(true);
    }

    /**
     * Alte Methode zum Hinzufügen von Elementen der Klasse 'Data' zu einer Liste (Node List)
     */
    public void add(Data d) {
        if (d != null) {
            if (d != searchFile(d)) {
                timetable.append(d);
            } else {
                timetable.setContent(d);
            }
        }
    }

    /**
     * Alte Methode, um die Liste nach einem schon vorhandenem Element zu durchsuchen
     */
    public Data searchFile(Data d) {
        if (!timetable.isEmpty()) {
            timetable.toFirst();
            while (timetable.hasAccess() && d.getID() != timetable.getContent().getID()) {
                timetable.next();
            }
            if (!timetable.hasAccess()) {
                return (null);
            }
            return (timetable.getContent());
        } else {
            return (null);
        }
    }

    /**
     * Eine Methode, welche den aktuellen Wochentag als String zurückgibt
     */
    public String getDayOfWeek() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());//erstellt einen Calender mit der default TimeZone
        cal.setTimeZone(TimeZone.getTimeZone("MET"));//stellt die Zeitzone auf Mittel Europäische Zeit
        int DoW = cal.get(Calendar.DAY_OF_WEEK);
        switch (DoW) { //switch-case zu den Wochentagen
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

    /**
     * Eine Methode, welche die aktuelle Uhrzeit formatiert und als String zurückgibt
     */
    public String getTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTimeZone(TimeZone.getTimeZone("MET"));
        Date date = cal.getTime();//holt sich die Uhrzeit aus dem Calender
        SimpleDateFormat f = new SimpleDateFormat("HH:mm");//formatiert die Uhrzeit
        return f.format(date);
    }

    public void addKursListener(KursListener kursListener) {
        this.kursListener = kursListener;
    }

    public void storeKurse(Kurs[] pKurse) {
        kursListener.onKursChange(pKurse);
    }

    /**
     * Setter Methode für den Schüler
     */
    public void setSchueler(Schueler schueler) {
        this.schueler = schueler;
    }

    /**
     * Setter Methode für die Kurse, bzw. ein Array der Klasse Kurs
     */
    public void setKurse(Kurs[] kurse) {
        //setzt die Kurse, wenn es der GUI am besten passt
        SwingUtilities.invokeLater(() -> this.alleKurse = kurse);
    }

    /**
     * Die Methode zum ActionListener, welche die verschiedenen Aktionen ausführt
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == m11) { //reset
            frame.dispose();//löscht das frame
            buildTimeTable();//und baut alles neu auf
        } else if (src == m12) { //close
            System.exit(0);//schließt die Anwendung
        } else if (src == bVertretungsplan) {
            tabbedPane.setSelectedIndex(1);
        }
    }
}