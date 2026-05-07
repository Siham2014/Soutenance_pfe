package ma.ensah.soutenance.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
        else if ("exportRepartitionExcel".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();

            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();

            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();

                if (!repartition.containsKey(prof)) {
                    repartition.put(prof, new ArrayList<Etudiant>());
                }

                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.xlsx");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Répartition");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Encadrant Nom");
            header.createCell(1).setCellValue("Encadrant Prénom");

            header.createCell(2).setCellValue("Etudiant 1 Nom");
            header.createCell(3).setCellValue("Etudiant 1 Prénom");

            header.createCell(4).setCellValue("Etudiant 2 Nom");
            header.createCell(5).setCellValue("Etudiant 2 Prénom");

            header.createCell(6).setCellValue("Etudiant 3 Nom");
            header.createCell(7).setCellValue("Etudiant 3 Prénom");

            header.createCell(8).setCellValue("Etudiant 4 Nom");
            header.createCell(9).setCellValue("Etudiant 4 Prénom");

            CellStyle styleTDIA = workbook.createCellStyle();
            styleTDIA.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            styleTDIA.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle styleGI = workbook.createCellStyle();
            styleGI.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            styleGI.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle styleID = workbook.createCellStyle();
            styleID.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            styleID.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIndex = 1;

            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {

                Row row = sheet.createRow(rowIndex++);

                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();

                row.createCell(0).setCellValue(prof.getNom());
                row.createCell(1).setCellValue(prof.getPrenom());

                int col = 2;

                for (int i = 0; i < 4; i++) {

                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);

                        Cell cellNom = row.createCell(col);
                        Cell cellPrenom = row.createCell(col + 1);

                        cellNom.setCellValue(e.getNom());
                        cellPrenom.setCellValue(e.getPrenom());

                        CellStyle style = null;

                        if ("TDIA".equalsIgnoreCase(e.getFiliere())) {
                            style = styleTDIA;
                        } else if ("GI".equalsIgnoreCase(e.getFiliere())) {
                            style = styleGI;
                        } else if ("ID".equalsIgnoreCase(e.getFiliere())) {
                            style = styleID;
                        }

                        if (style != null) {
                            cellNom.setCellStyle(style);
                            cellPrenom.setCellStyle(style);
                        }

                    } else {
                        row.createCell(col).setCellValue("");
                        row.createCell(col + 1).setCellValue("");
                    }

                    col += 2;
                }
            }

            for (int i = 0; i <= 9; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            workbook.close();
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