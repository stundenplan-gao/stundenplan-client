package database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import util.Strings;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "kurs")
public class Kurs {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String bezeichnung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fachId")
    private Fach fach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stufenId")
    private Stufe stufe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lehrerId")
    private Lehrer lehrer;

    @ManyToMany(mappedBy = "kurse", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Schueler> schueler;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "kurs_stunde",
            joinColumns = @JoinColumn(name = "kursId"),
            inverseJoinColumns = @JoinColumn(name = "stundenId")
    )
    private Set<Stunde> stunden;

    public Kurs() {
    }

    public Kurs(String bezeichnung, Fach fach, Stufe stufe, Lehrer lehrer) {
        this.bezeichnung = bezeichnung;
        this.fach = fach;
        this.stufe = stufe;
        this.lehrer = lehrer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String kursbezeichnung) {
        this.bezeichnung = kursbezeichnung;
    }

    public Fach getFach() {
        return fach;
    }

    public void setFach(Fach fach) {
        this.fach = fach;
    }

    public Stufe getStufe() {
        return stufe;
    }

    public void setStufe(Stufe stufe) {
        this.stufe = stufe;
    }

    public void setLehrer(Lehrer lehrer) {
        this.lehrer = lehrer;
    }

    public Set<Schueler> getSchueler() {
        return schueler;
    }

    public void setSchueler(Set<Schueler> schueler) {
        this.schueler = schueler;
    }

    public Set<Stunde> getStunden() {
        return stunden;
    }

    public void setStunden(Set<Stunde> stunden) {
        this.stunden = stunden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kurs kurs = (Kurs) o;
        return fach.equals(kurs.fach) &&
                stufe.equals(stufe) &&
                lehrer.equals(lehrer) &&
                Objects.equals(id, kurs.id) &&
                Objects.equals(bezeichnung, kurs.bezeichnung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bezeichnung, fach, stufe, lehrer);
    }

    @Override
    public String toString() {
        return "Kurs [" + id + ":" + bezeichnung + "]\n" + //
                " Fach    : " + fach.getKuerzel() + " (" + fach.getFach() + ")\n" + //
                " Stufe   : " + stufe.getStufe() + "\n" + //
                " Lehrer  : " + lehrer + "\n" + //
                " Schüler : \n   - " + Strings.join(schueler, "\n   - ") + "\n" + //
                " Stunden : \n   - " + Strings.join(stunden, "\n   - ");
    }
}
