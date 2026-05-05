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

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        ProfesseurDao professeurDao = new ProfesseurDaoImpl();
        EtudiantDao etudiantDao = new EtudiantDaoImpl();
        GroupPfeDao groupPfeDao = new GroupPfeDaoImpl();

        if ("repartition".equals(action)) {

            long seed = System.currentTimeMillis();
            RepartitionStrategy strategy = new SeededBalancedBlockFiliereStrategy(seed);

            VersionRepartitionDao versionDao = new VersionRepartitionDaoImpl();

            RepartitionEncadrantsService repartitionService =
                    new RepartitionEncadrantsServiceImpl(
                            professeurDao,
                            etudiantDao,
                            groupPfeDao,
                            versionDao,
                            strategy
                    );

            repartitionService.repartirEncadrants();

            request.setAttribute("message", "Répartition effectuée !");
            request.getRequestDispatcher("/WEB-INF/views/accueil.jsp")
                   .forward(request, response);
            return;
        }

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

        else if ("pageImportEncadrants".equals(action)) {

            request.getRequestDispatcher("/WEB-INF/views/import-encadrants.jsp")
                   .forward(request, response);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/accueil.jsp")
               .forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if ("importerEtRepartir".equals(action)) {

        	 GroupPfeDao groupPfeDao = new GroupPfeDaoImpl();
        	    groupPfeDao.resetDatabase(); 
            Part filePart = request.getPart("fichierExcel");

            try (InputStream input = filePart.getInputStream();
                 Workbook workbook = WorkbookFactory.create(input)) {

         
                EtudiantDao etudiantDao = new EtudiantDaoImpl();
                ProfesseurDao professeurDao = new ProfesseurDaoImpl();

         
                Sheet sheetEtudiants = workbook.getSheet("Etudiant");

                for (int i = 1; i <= sheetEtudiants.getLastRowNum(); i++) {
                    Row row = sheetEtudiants.getRow(i);

                    if (row != null) {
                        String nom = row.getCell(0).getStringCellValue();
                        String prenom = row.getCell(1).getStringCellValue();
                        String filiere = row.getCell(2).getStringCellValue();

                        Etudiant etudiant = new Etudiant(nom, prenom, filiere);
                        etudiantDao.save(etudiant);
                    }
                }

            
                Sheet sheetProfesseurs = workbook.getSheet("Professeur");

                for (int i = 1; i <= sheetProfesseurs.getLastRowNum(); i++) {
                    Row row = sheetProfesseurs.getRow(i);

                    if (row != null) {
                        String nom = row.getCell(0).getStringCellValue();
                        String prenom = row.getCell(1).getStringCellValue();
                        String specialite = row.getCell(2).getStringCellValue();

                        Professeur professeur = new Professeur(nom, prenom, specialite);
                        professeurDao.save(professeur);
                    }
                }
            }
        

         long seed = System.currentTimeMillis();
         RepartitionStrategy strategy = new SeededBalancedBlockFiliereStrategy(seed);

         VersionRepartitionDao versionDao = new VersionRepartitionDaoImpl();

         RepartitionEncadrantsService repartitionService =
                 new RepartitionEncadrantsServiceImpl(
                         new ProfesseurDaoImpl(),
                         new EtudiantDaoImpl(),
                         new GroupPfeDaoImpl(),
                         versionDao,
                         strategy
                 );

         repartitionService.repartirEncadrants();

            request.setAttribute("message", "Import terminé");
            response.sendRedirect("app?action=voirRepartition");
            return;
        }

        doGet(request, response);
    }
}