package ma.ensah.soutenance.model.entity;

import javax.persistence.*;

@Entity
@Table(name="etudiants")

public class Etudiant {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    
    private String prenom;
    @Column(nullable = false)
    private String filiere;

    public Etudiant() {
    }

    public Etudiant(String nom, String prenom, String filiere) {
        this.nom = nom;
        this.prenom = prenom;
        this.filiere = filiere;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }
}