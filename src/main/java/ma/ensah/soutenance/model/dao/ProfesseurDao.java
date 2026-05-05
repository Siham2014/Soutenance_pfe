package ma.ensah.soutenance.model.dao;

import java.util.List;

import ma.ensah.soutenance.model.entity.Professeur;

public interface ProfesseurDao {
    void save(Professeur professeur);
    List<Professeur> findAll();
}