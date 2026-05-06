package ma.ensah.soutenance.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "soutenances")
public class Soutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'étudiant qui soutient
    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    // Membre jury 1 : l'encadrant
    @ManyToOne
    @JoinColumn(name = "encadrant_id", nullable = false)
    private Professeur encadrant;

    // Membre jury 2 : prof Informatique
    @ManyToOne
    @JoinColumn(name = "membre_info_id", nullable = false)
    private Professeur membreInfo;

    // Membre jury 3 : prof Mathématiques
    @ManyToOne
    @JoinColumn(name = "membre_math_id", nullable = false)
    private Professeur membreMath;

    // La salle de soutenance
    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    // Date de soutenance
    @Temporal(TemporalType.DATE)
    @Column(name = "date_soutenance", nullable = false)
    private Date dateSoutenance;

    // Heure début ex: "09:00"
    @Column(name = "heure_debut", nullable = false)
    private String heureDebut;

    // Heure fin ex: "10:00"
    @Column(name = "heure_fin", nullable = false)
    private String heureFin;

    public Soutenance() {}

    // ── Getters & Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Etudiant getEtudiant() { return etudiant; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }

    public Professeur getEncadrant() { return encadrant; }
    public void setEncadrant(Professeur encadrant) { this.encadrant = encadrant; }

    public Professeur getMembreInfo() { return membreInfo; }
    public void setMembreInfo(Professeur membreInfo) { this.membreInfo = membreInfo; }

    public Professeur getMembreMath() { return membreMath; }
    public void setMembreMath(Professeur membreMath) { this.membreMath = membreMath; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public Date getDateSoutenance() { return dateSoutenance; }
    public void setDateSoutenance(Date dateSoutenance) { this.dateSoutenance = dateSoutenance; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
}