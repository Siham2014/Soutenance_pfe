package ma.ensah.soutenance.service.impl;

import java.util.Date;
import java.util.List;

import ma.ensah.soutenance.algorithm.RepartitionStrategy;
import ma.ensah.soutenance.model.dao.*;
import ma.ensah.soutenance.model.entity.*;
import ma.ensah.soutenance.service.RepartitionEncadrantsService;

public class RepartitionEncadrantsServiceImpl implements RepartitionEncadrantsService {

    private ProfesseurDao professeurDao;
    private EtudiantDao etudiantDao;
    private GroupPfeDao groupPfeDao;
    private VersionRepartitionDao versionDao;

    private RepartitionStrategy strategy;

    public RepartitionEncadrantsServiceImpl(
            ProfesseurDao professeurDao,
            EtudiantDao etudiantDao,
            GroupPfeDao groupPfeDao,
            VersionRepartitionDao versionDao,
            RepartitionStrategy strategy) {

        this.professeurDao = professeurDao;
        this.etudiantDao = etudiantDao;
        this.groupPfeDao = groupPfeDao;
        this.versionDao = versionDao;
        this.strategy = strategy;
    }

    @Override
    public void repartirEncadrants() {

        List<Professeur> professeurs = professeurDao.findAll();
        List<Etudiant> etudiants = etudiantDao.findAll();

        if (professeurs.isEmpty() || etudiants.isEmpty()) {
            System.out.println("Aucun professeur ou étudiant trouvé.");
            return;
        }

        VersionRepartition version = new VersionRepartition();
        version.setNom("Version " + System.currentTimeMillis());
        version.setDateCreation(new Date());

        versionDao.save(version);

        List<GroupPfe> groupes = strategy.repartir(professeurs, etudiants);

        for (GroupPfe groupe : groupes) {
            groupe.setVersion(version);
            groupPfeDao.save(groupe);
        }

        System.out.println("Répartition terminée !");
    }
}