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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ma.ensah.soutenance.service.impl.genererPv;

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
        }else if ("exportRepartitionExcel".equals(action)) {

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
        else if ("exportRepartitionPdf".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();
            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();

            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();
                if (!repartition.containsKey(prof)) {
                    repartition.put(prof, new ArrayList<Etudiant>());
                }
                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.pdf");

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, response.getOutputStream());

            document.open();
            document.add(new Paragraph("Répartition des encadrants"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);

            table.addCell("Encadrant Nom");
            table.addCell("Encadrant Prénom");
            table.addCell("Etudiant 1 Nom");
            table.addCell("Etudiant 1 Prénom");
            table.addCell("Etudiant 2 Nom");
            table.addCell("Etudiant 2 Prénom");
            table.addCell("Etudiant 3 Nom");
            table.addCell("Etudiant 3 Prénom");
            table.addCell("Etudiant 4 Nom");
            table.addCell("Etudiant 4 Prénom");

            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {
                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();

                table.addCell(prof.getNom());
                table.addCell(prof.getPrenom());

                for (int i = 0; i < 4; i++) {
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);

                        PdfPCell cellNom = new PdfPCell(new Phrase(e.getNom()));
                        PdfPCell cellPrenom = new PdfPCell(new Phrase(e.getPrenom()));

                        if ("GI".equalsIgnoreCase(e.getFiliere())) {
                            cellNom.setBackgroundColor(new java.awt.Color(180,198,231));
                            cellPrenom.setBackgroundColor(new java.awt.Color(180,198,231));
                        } else if ("ID".equalsIgnoreCase(e.getFiliere())) {
                            cellNom.setBackgroundColor(new java.awt.Color(183,225,205));
                            cellPrenom.setBackgroundColor(new java.awt.Color(183,225,205));
                        } else if ("TDIA".equalsIgnoreCase(e.getFiliere())) {
                            cellNom.setBackgroundColor(new java.awt.Color(244,177,131));
                            cellPrenom.setBackgroundColor(new java.awt.Color(244,177,131));
                        }

                        table.addCell(cellNom);
                        table.addCell(cellPrenom);
                    } else {
                        table.addCell("");
                        table.addCell("");
                    }
                }
            }

            document.add(table);
            document.close();
            return;
        } else if ("exportRepartitionWord".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();
            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();

            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();
                if (!repartition.containsKey(prof)) {
                    repartition.put(prof, new ArrayList<Etudiant>());
                }
                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.docx");

            XWPFDocument document = new XWPFDocument();

            XWPFParagraph title = document.createParagraph();
            XWPFRun run = title.createRun();
            run.setText("Répartition des encadrants");
            run.setBold(true);
            run.setFontSize(16);

            XWPFTable table = document.createTable();

            XWPFTableRow header = table.getRow(0);
            header.getCell(0).setText("Encadrant Nom");
            header.addNewTableCell().setText("Encadrant Prénom");
            header.addNewTableCell().setText("Etudiant 1 Nom");
            header.addNewTableCell().setText("Etudiant 1 Prénom");
            header.addNewTableCell().setText("Etudiant 2 Nom");
            header.addNewTableCell().setText("Etudiant 2 Prénom");
            header.addNewTableCell().setText("Etudiant 3 Nom");
            header.addNewTableCell().setText("Etudiant 3 Prénom");
            header.addNewTableCell().setText("Etudiant 4 Nom");
            header.addNewTableCell().setText("Etudiant 4 Prénom");

            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {
                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();

                XWPFTableRow row = table.createRow();

                row.getCell(0).setText(prof.getNom());
                row.getCell(1).setText(prof.getPrenom());

                int col = 2;

                for (int i = 0; i < 4; i++) {
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);

                        XWPFTableCell cellNom = row.getCell(col);
                        XWPFTableCell cellPrenom = row.getCell(col + 1);

                        cellNom.setText(e.getNom());
                        cellPrenom.setText(e.getPrenom());

                        if ("GI".equalsIgnoreCase(e.getFiliere())) {
                            cellNom.setColor("B4C6E7");
                            cellPrenom.setColor("B4C6E7");
                        } else if ("ID".equalsIgnoreCase(e.getFiliere())) {
                            cellNom.setColor("B7E1CD");
                            cellPrenom.setColor("B7E1CD");
                        } else if ("TDIA".equalsIgnoreCase(e.getFiliere())) {
                            cellNom.setColor("F4B183");
                            cellPrenom.setColor("F4B183");
                        }
                    } else {
                        row.getCell(col).setText("");
                        row.getCell(col + 1).setText("");
                    }

                    col += 2;
                }
            }

            document.write(response.getOutputStream());
            document.close();
            return;
        } else if ("genererPv".equals(action)) {

            String idParam = request.getParameter("id");

            if (idParam == null || idParam.trim().isEmpty()) {

                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID soutenance manquant"
                );

                return;
            }

            Long id = Long.parseLong(idParam);
            SoutenanceDao soutenanceDao = new SoutenanceDaoImpl();
            Soutenance s = soutenanceDao.findById(id);

           

            if (s == null) {

                response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Soutenance introuvable"
                );

                return;
            }

            genererPv pv = new genererPv();

            pv.genererPv(response, s);
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
