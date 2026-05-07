package ma.ensah.soutenance.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import ma.ensah.soutenance.model.dao.*;
import ma.ensah.soutenance.model.dao.impl.*;
import ma.ensah.soutenance.model.entity.*;
import ma.ensah.soutenance.service.*;
import ma.ensah.soutenance.service.impl.*;
import ma.ensah.soutenance.algorithm.*;

@MultipartConfig
@WebServlet("/app")
public class MainControllerServlet extends HttpServlet {

    // =========================================================
    //  GET
    // =========================================================
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        ProfesseurDao professeurDao = new ProfesseurDaoImpl();
        EtudiantDao   etudiantDao   = new EtudiantDaoImpl();
        GroupPfeDao   groupPfeDao   = new GroupPfeDaoImpl();

        // ── Répartition encadrants ────────────────────────────
        if ("repartition".equals(action)) {

            long seed = System.currentTimeMillis();
            RepartitionStrategy strategy = new SeededBalancedBlockFiliereStrategy(seed);
            VersionRepartitionDao versionDao = new VersionRepartitionDaoImpl();

            RepartitionEncadrantsService repartitionService =
                    new RepartitionEncadrantsServiceImpl(
                            professeurDao, etudiantDao, groupPfeDao, versionDao, strategy);

            repartitionService.repartirEncadrants();

            response.sendRedirect("app?action=voirRepartition");
            return;
        }

        // ── Voir répartition ──────────────────────────────────
        else if ("voirRepartition".equals(action)) {

            String versionIdParam = request.getParameter("versionId");
            List<GroupPfe> groupes;

            if (versionIdParam != null && !versionIdParam.isEmpty()) {
                Long versionId = Long.parseLong(versionIdParam);
                groupes = groupPfeDao.findAllWithDetailsByVersion(versionId);
            } else {
                groupes = groupPfeDao.findAllWithDetails();
            }

            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();
            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();
                if (!repartition.containsKey(prof)) {
                    repartition.put(prof, new ArrayList<Etudiant>());
                }
                repartition.get(prof).addAll(g.getEtudiants());
            }

            request.setAttribute("repartition", repartition);
            request.getRequestDispatcher("/WEB-INF/views/repartition.jsp")
                   .forward(request, response);
            return;
        }

        // ── Page import encadrants ────────────────────────────
        else if ("pageImportEncadrants".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/views/import-encadrants.jsp")
                   .forward(request, response);
            return;
        }

        // ── Page import planning ──────────────────────────────
        // ✅ Bloqué si la répartition n'a pas encore été faite
        else if ("importerPlanning".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();

            if (groupes.isEmpty()) {
                // Pas de répartition → retour accueil avec message erreur
                List<GroupPfe> groupesVide = groupPfeDao.findAllWithDetails();
                request.setAttribute("repartitionFaite", false);
                request.setAttribute("message_erreur",
                    "⚠️ Vous devez d'abord effectuer la répartition des encadrants avant de générer le planning !");
                request.getRequestDispatcher("/WEB-INF/views/accueil.jsp")
                       .forward(request, response);
                return;
            }

            request.getRequestDispatcher("/WEB-INF/views/importerPlanning.jsp")
                   .forward(request, response);
            return;
        }

        // ── Voir planning généré ──────────────────────────────
        else if ("voirPlanning".equals(action)) {
            PlanningService planningService = new PlanningServiceImpl();
            List<Soutenance> planning = planningService.getPlanning();
            request.setAttribute("planning", planning);
            request.getRequestDispatcher("/WEB-INF/views/planning.jsp")
                   .forward(request, response);
            return;
        }

        // ── Accueil par défaut ────────────────────────────────
        // Vérifier si répartition existe pour activer/désactiver bouton planning
        List<GroupPfe> groupesExistants = groupPfeDao.findAllWithDetails();
        request.setAttribute("repartitionFaite", !groupesExistants.isEmpty());

        request.getRequestDispatcher("/WEB-INF/views/accueil.jsp")
               .forward(request, response);
    }

    // =========================================================
    //  POST
    // =========================================================
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // ── Import Excel encadrants + répartition ─────────────
        if ("importerEtRepartir".equals(action)) {

            GroupPfeDao groupPfeDao = new GroupPfeDaoImpl();
            groupPfeDao.resetDatabase();

            Part filePart = request.getPart("fichierExcel");

            try (InputStream input = filePart.getInputStream();
                 Workbook workbook = WorkbookFactory.create(input)) {

                EtudiantDao   etudiantDao   = new EtudiantDaoImpl();
                ProfesseurDao professeurDao = new ProfesseurDaoImpl();

                // Lire feuille Etudiant
                Sheet sheetEtudiants = workbook.getSheet("Etudiant");
                for (int i = 1; i <= sheetEtudiants.getLastRowNum(); i++) {
                    Row row = sheetEtudiants.getRow(i);
                    if (row != null && row.getCell(0) != null) {
                        String nom     = row.getCell(0).getStringCellValue().trim();
                        String prenom  = row.getCell(1).getStringCellValue().trim();
                        String filiere = row.getCell(2).getStringCellValue().trim();
                        if (!nom.isEmpty()) {
                            etudiantDao.save(new Etudiant(nom, prenom, filiere));
                        }
                    }
                }

                // Lire feuille Professeur
                Sheet sheetProfesseurs = workbook.getSheet("Professeur");
                for (int i = 1; i <= sheetProfesseurs.getLastRowNum(); i++) {
                    Row row = sheetProfesseurs.getRow(i);
                    if (row != null && row.getCell(0) != null) {
                        String nom        = row.getCell(0).getStringCellValue().trim();
                        String prenom     = row.getCell(1).getStringCellValue().trim();
                        String specialite = row.getCell(2).getStringCellValue().trim();
                        if (!nom.isEmpty()) {
                            professeurDao.save(new Professeur(nom, prenom, specialite));
                        }
                    }
                }
            }

            // Lancer répartition
            long seed = System.currentTimeMillis();
            RepartitionStrategy strategy = new SeededBalancedBlockFiliereStrategy(seed);
            VersionRepartitionDao versionDao = new VersionRepartitionDaoImpl();

            RepartitionEncadrantsService repartitionService =
                    new RepartitionEncadrantsServiceImpl(
                            new ProfesseurDaoImpl(),
                            new EtudiantDaoImpl(),
                            new GroupPfeDaoImpl(),
                            versionDao,
                            strategy);

            repartitionService.repartirEncadrants();

            response.sendRedirect("app?action=voirRepartition");
            return;
        }

        // ── Import Excel planning + génération ────────────────
        else if ("genererPlanning".equals(action)) {

            // ✅ Vérifier une dernière fois que la répartition existe
            GroupPfeDao groupPfeDao = new GroupPfeDaoImpl();
            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();
            if (groupes.isEmpty()) {
                request.setAttribute("repartitionFaite", false);
                request.setAttribute("message_erreur",
                    "⚠️ Répartition introuvable. Veuillez d'abord importer et répartir les encadrants.");
                request.getRequestDispatcher("/WEB-INF/views/accueil.jsp")
                       .forward(request, response);
                return;
            }

            Part filePart = request.getPart("fichierExcel");

            try (InputStream input = filePart.getInputStream();
                 Workbook workbook = WorkbookFactory.create(input)) {

                // Vider salles et soutenances avant chaque génération
                SoutenanceDao soutenanceDao = new SoutenanceDaoImpl();
                soutenanceDao.deleteAll();

                SalleDao salleDao = new SalleDaoImpl();
                salleDao.deleteAll();

                // Lire feuille Salle
                Sheet sheetSalles = workbook.getSheet("Salle");
                if (sheetSalles != null) {
                    for (int i = 1; i <= sheetSalles.getLastRowNum(); i++) {
                        Row row = sheetSalles.getRow(i);
                        if (row != null && row.getCell(0) != null) {
                            String nom = row.getCell(0).getStringCellValue().trim();
                            int cap    = (int) row.getCell(1).getNumericCellValue();
                            if (!nom.isEmpty()) {
                                salleDao.save(new Salle(nom, cap));
                            }
                        }
                    }
                }

                // Lire feuille Creneau
                Sheet sheetCreneaux = workbook.getSheet("Creneau");
                List<String[]> creneaux = new ArrayList<>();
                if (sheetCreneaux != null) {
                    for (int i = 1; i <= sheetCreneaux.getLastRowNum(); i++) {
                        Row row = sheetCreneaux.getRow(i);
                        if (row != null && row.getCell(0) != null) {
                            String date = row.getCell(0).getStringCellValue().trim();
                            String hdeb = row.getCell(1).getStringCellValue().trim();
                            String hfin = row.getCell(2).getStringCellValue().trim();
                            if (!date.isEmpty()) {
                                creneaux.add(new String[]{date, hdeb, hfin});
                            }
                        }
                    }
                }

                // Lancer algorithme planning
                PlanningService planningService = new PlanningServiceImpl();
                planningService.genererPlanning(creneaux, new ArrayList<>());
            }

            response.sendRedirect("app?action=voirPlanning");
            return;
        }

        doGet(request, response);
    }
}
