package ma.ensah.soutenance.model.entity;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "groupes_pfe")
public class GroupPfe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "encadrant_id")
    private Professeur encadrant;

    @ManyToOne
    @JoinColumn(name = "version_id")
    private VersionRepartition version;

    @OneToMany
    @JoinTable(
        name = "groupe_etudiants",
        joinColumns = @JoinColumn(name = "groupe_id"),
        inverseJoinColumns = @JoinColumn(name = "etudiant_id")
    )
    private List<Etudiant> etudiants;

    public GroupPfe() {
    }

    public GroupPfe(List<Etudiant> etudiants) {
        this.etudiants = etudiants;
    }

    public GroupPfe(Professeur encadrant, List<Etudiant> etudiants) {
        this.encadrant = encadrant;
        this.etudiants = etudiants;
    }

    public Long getId() {
        return id;
    }

    public Professeur getEncadrant() {
        return encadrant;
    }

    public VersionRepartition getVersion() {
        return version;
    }

    public List<Etudiant> getEtudiants() {
        return etudiants;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEncadrant(Professeur encadrant) {
        this.encadrant = encadrant;
    }

    public void setVersion(VersionRepartition version) {
        this.version = version;
    }

    public void setEtudiants(List<Etudiant> etudiants) {
        this.etudiants = etudiants;
    }
}