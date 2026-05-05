package ma.ensah.soutenance.model.entity;

import javax.persistence.*;

@Entity
@Table(name="professeurs")

public class Professeur {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	private Long id;
	
	@Column(name="nom" , nullable = false)
	private String nom;
	
	@Column(name="prenom" , nullable = false)
	private String prenom;
	
	@Column(name="spacialite", nullable= false)
	private String specialite;
	
	public Professeur() {
		
	}
	
	public Professeur(String nom , String prenom, String specialite) {
		this.nom=nom;
		this.prenom=prenom;
		this.specialite=specialite;
		
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

	public String getSpecialite() {
		return specialite;
	}

	public void setSpecialite(String specialite) {
		this.specialite = specialite;
	}

	

	
}
