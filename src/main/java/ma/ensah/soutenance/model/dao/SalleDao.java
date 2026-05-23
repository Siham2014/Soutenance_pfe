package ma.ensah.soutenance.model.dao;

import ma.ensah.soutenance.model.entity.Salle;
import java.util.List;

public interface SalleDao {
    void save(Salle salle);
    List<Salle> findAll();
    void deleteAll();
}