package ma.ensah.soutenance.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

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
        else if ("importerPlanning".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();

            if (groupes.isEmpty()) {
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

        // ── Export planning Excel ─────────────────────────────
        else if ("exportPlanning".equals(action)) {
            exportPlanningExcel(request, response);
            return;
        }

        // ── Accueil par défaut ────────────────────────────────
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

                PlanningService planningService = new PlanningServiceImpl();
                planningService.genererPlanning(creneaux, new ArrayList<>());
            }

            response.sendRedirect("app?action=voirPlanning");
            return;
        }

        doGet(request, response);
    }

    // =========================================================
    //  Méthode privée — Export Excel du planning
    // =========================================================
    private void exportPlanningExcel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PlanningService planningService = new PlanningServiceImpl();
        List<Soutenance> planning = planningService.getPlanning();

        if (planning.isEmpty()) {
            response.sendRedirect("app?action=voirPlanning");
            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Planning Soutenances");

        // ── Styles ────────────────────────────────────────────
        XSSFCellStyle styleTitre  = buildTitleStyle(workbook);
        XSSFCellStyle styleHeader = buildHeaderStyle(workbook);
        XSSFCellStyle styleData   = buildDataStyle(workbook);
        XSSFCellStyle styleTDIA   = buildFiliereStyle(workbook, styleData,
                new byte[]{(byte)244, (byte)177, (byte)131});
        XSSFCellStyle styleGI     = buildFiliereStyle(workbook, styleData,
                new byte[]{(byte)180, (byte)198, (byte)231});
        XSSFCellStyle styleID     = buildFiliereStyle(workbook, styleData,
                new byte[]{(byte)183, (byte)225, (byte)205});

        // ── Ligne titre ───────────────────────────────────────
        Row rowTitre = sheet.createRow(0);
        rowTitre.setHeightInPoints(28);
        Cell cellTitre = rowTitre.createCell(0);
        cellTitre.setCellValue("Planning des Soutenances PFE - ENSAH");
        cellTitre.setCellStyle(styleTitre);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        // ── En-têtes colonnes ─────────────────────────────────
        String[] headers = {
            "Date", "Heure Début", "Heure Fin",
            "Étudiant", "Filière", "Salle",
            "Encadrant (Jury)", "Jury Informatique", "Jury Mathématiques"
        };
        Row rowHeader = sheet.createRow(1);
        rowHeader.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styleHeader);
        }

        // ── Données ───────────────────────────────────────────
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        int rowNum = 2;

        for (Soutenance s : planning) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);

            String filiere = s.getEtudiant().getFiliere();
            XSSFCellStyle styleFiliere = "TDIA".equals(filiere) ? styleTDIA
                                       : "GI".equals(filiere)   ? styleGI
                                       : "ID".equals(filiere)   ? styleID
                                       : styleData;

            setCell(row, 0, s.getDateSoutenance() != null
                    ? sdf.format(s.getDateSoutenance()) : "", styleData);
            setCell(row, 1, s.getHeureDebut(),  styleData);
            setCell(row, 2, s.getHeureFin(),    styleData);
            setCell(row, 3, s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom(), styleFiliere);
            setCell(row, 4, filiere,             styleFiliere);
            setCell(row, 5, s.getSalle().getNom(), styleData);
            setCell(row, 6, s.getEncadrant().getNom()  + " " + s.getEncadrant().getPrenom(),  styleData);
            setCell(row, 7, s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom(), styleData);
            setCell(row, 8, s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom(), styleData);
        }

        // ── Mise en forme finale ──────────────────────────────
        int[] colWidths = {3200, 3000, 3000, 6000, 2500, 3000, 6000, 6000, 6000};
        for (int i = 0; i < colWidths.length; i++) {
            sheet.setColumnWidth(i, colWidths[i]);
        }
        sheet.createFreezePane(0, 2);
        sheet.setAutoFilter(new CellRangeAddress(1, rowNum - 1, 0, 8));

        // ── Réponse HTTP ──────────────────────────────────────
        response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"planning_soutenances.xlsx\"");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ── Helpers cellule ───────────────────────────────────────

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    // ── Helpers styles ────────────────────────────────────────

    private XSSFCellStyle buildTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)31,(byte)111,(byte)191}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private XSSFCellStyle buildHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)31,(byte)111,(byte)191}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorders(s);
        return s;
    }

    private XSSFCellStyle buildDataStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        applyBorders(s);
        return s;
    }

    private XSSFCellStyle buildFiliereStyle(XSSFWorkbook wb, XSSFCellStyle base, byte[] rgb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.cloneStyleFrom(base);
        s.setFillForegroundColor(new XSSFColor(rgb, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private void applyBorders(XSSFCellStyle s) {
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }
}

