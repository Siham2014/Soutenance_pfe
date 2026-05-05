package ma.ensah.soutenance.model.entity;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "version_repartition")
public class VersionRepartition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_creation", nullable = false)
    private Date dateCreation;

    public VersionRepartition() {
    }

    public VersionRepartition(String nom, Date dateCreation) {
        this.nom = nom;
        this.dateCreation = dateCreation;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
}