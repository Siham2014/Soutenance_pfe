package ma.ensah.soutenance.model.dao;

import ma.ensah.soutenance.model.entity.Soutenance;
import java.util.Date;
import java.util.List;

public interface SoutenanceDao {
    void save(Soutenance soutenance);
    List<Soutenance> findAll();
    void deleteAll();
    boolean isSalleOccupee(Long salleId, Date date, String heureDebut);
    boolean isProfOccupe(Long profId, Date date, String heureDebut);
    List<Soutenance> findByProf(Long profId, Date date);
}