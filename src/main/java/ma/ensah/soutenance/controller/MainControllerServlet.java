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
import ma.ensah.soutenance.model.dto.DashboardStats;
import ma.ensah.soutenance.service.*;
import ma.ensah.soutenance.service.impl.*;
import ma.ensah.soutenance.algorithm.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

        // ── Dashboard statistiques ────────────────────────────
        if ("dashboard".equals(action)) {
            DashboardService dashboardService = new DashboardServiceImpl();
            DashboardStats stats = dashboardService.getStats();

            // Objet global (pour les KPI)
            request.setAttribute("statsGlobales", stats);

            // Attributs séparés pour les graphiques dans la JSP
            request.setAttribute("nbEtudiantsParEncadrant", stats.getEtudiantsParProf());
            request.setAttribute("nbSoutenancesParFiliere",  stats.getSoutenancesParFiliere());
            request.setAttribute("nbSoutenancesParProf",     stats.getSoutenancesParProf());
            request.setAttribute("nbSoutenancesParJour",     stats.getSoutenancesParDate());
            request.setAttribute("nbEtudiantsParFiliere",    stats.getEtudiantsParFiliere());
            request.setAttribute("specialitesProf",          stats.getProfsParSpecialite());
            request.setAttribute("chargeJuryParProf",        stats.getChargeJuryParProf());
            request.setAttribute("nbSoutenancesParSalle",    stats.getSoutenancesParSalle());

            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
                   .forward(request, response);
            return;
        }

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
        
  

        // ── Page import planning PV ───────────────────────────
        else if ("importerPlanningPv".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/views/importerPlanningPv.jsp")
                   .forward(request, response);
            return;
        }
        else if ("voirPlanning".equals(action)) {
            PlanningService planningService = new PlanningServiceImpl();
            List<Soutenance> planning = planningService.getPlanning();
            request.setAttribute("planning", planning);
            request.getRequestDispatcher("/WEB-INF/views/planning.jsp")
                   .forward(request, response);
            return;
        }
        
     // ── Export Excel Planning ─────────────────────────────
        else if ("exportExcel".equals(action) && "planning".equals(request.getParameter("type"))) {
            PlanningService planningService = new PlanningServiceImpl();
            List<Soutenance> planning = planningService.getPlanning();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=planning_soutenances.xlsx");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Planning Soutenances");

            // ── Police de base ───────────────────────────────────
            org.apache.poi.ss.usermodel.Font baseFont = workbook.createFont();
            baseFont.setFontName("Calibri");
            baseFont.setFontHeightInPoints((short) 10);

            // ── Style titre ──────────────────────────────────────
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            ((org.apache.poi.xssf.usermodel.XSSFFont) titleFont).setColor(
            	    new org.apache.poi.xssf.usermodel.XSSFColor(
            	        new byte[]{(byte)31, (byte)111, (byte)191}, null));

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            // Ligne titre
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Planning des Soutenances PFE");
            titleCell.setCellStyle(titleStyle);

            // Ligne sous-titre
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 8));
            Row subRow = sheet.createRow(1);
            Cell subCell = subRow.createCell(0);
            subCell.setCellValue("Total : " + planning.size() + " soutenance(s) planifiée(s)");
            CellStyle subStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font subFont2 = workbook.createFont();
            subFont2.setFontName("Calibri");
            subFont2.setItalic(true);
            subFont2.setColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT.getIndex());
            subStyle.setFont(subFont2);
            subCell.setCellStyle(subStyle);

            // Ligne vide
            sheet.createRow(2);

            // ── Style en-tête ────────────────────────────────────
            org.apache.poi.ss.usermodel.Font headerFont2 = workbook.createFont();
            headerFont2.setFontName("Calibri");
            headerFont2.setBold(true);
            headerFont2.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerFont2.setFontHeightInPoints((short) 10);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont2);
            headerStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(
                new byte[]{(byte)31, (byte)111, (byte)191}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(22);
            String[] cols2 = {"Date", "Heure Début", "Heure Fin", "Étudiant", 
                              "Filière", "Salle", "Encadrant", "Jury Info", "Jury Math"};
            for (int i = 0; i < cols2.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(cols2[i]);
                c.setCellStyle(headerStyle);
            }

            // ── Styles filières ──────────────────────────────────
            CellStyle styleGI2   = createColorStyle(workbook, (byte)180, (byte)198, (byte)231, baseFont);
            CellStyle styleID2   = createColorStyle(workbook, (byte)183, (byte)225, (byte)205, baseFont);
            CellStyle styleTDIA2 = createColorStyle(workbook, (byte)244, (byte)177, (byte)131, baseFont);
            CellStyle styleEnc   = createColorStyle(workbook, (byte)198, (byte)239, (byte)206, baseFont); // vert
            CellStyle styleInfo2 = createColorStyle(workbook, (byte)189, (byte)215, (byte)238, baseFont); // bleu
            CellStyle styleMath2 = createColorStyle(workbook, (byte)226, (byte)206, (byte)255, baseFont); // violet

            CellStyle styleWhite = createColorStyle(workbook, (byte)255, (byte)255, (byte)255, baseFont);
            CellStyle styleGray  = createColorStyle(workbook, (byte)245, (byte)245, (byte)245, baseFont);

            // ── Lignes de données ────────────────────────────────
            int rowIdx = 4;
            for (Soutenance s : planning) {
                Row row = sheet.createRow(rowIdx);
                row.setHeightInPoints(18);

                String filiere2 = s.getEtudiant().getFiliere();
                CellStyle filiereStyle = "GI".equalsIgnoreCase(filiere2)   ? styleGI2 :
                                         "ID".equalsIgnoreCase(filiere2)   ? styleID2 :
                                         "TDIA".equalsIgnoreCase(filiere2) ? styleTDIA2 :
                                         (rowIdx % 2 == 0) ? styleWhite : styleGray;
                CellStyle rowStyle = (rowIdx % 2 == 0) ? styleWhite : styleGray;

                row.createCell(0).setCellValue(
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(s.getDateSoutenance()));
                row.getCell(0).setCellStyle(rowStyle);

                row.createCell(1).setCellValue(s.getHeureDebut());
                row.getCell(1).setCellStyle(rowStyle);

                row.createCell(2).setCellValue(s.getHeureFin());
                row.getCell(2).setCellStyle(rowStyle);

                row.createCell(3).setCellValue(s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom());
                row.getCell(3).setCellStyle(filiereStyle);

                row.createCell(4).setCellValue(filiere2);
                row.getCell(4).setCellStyle(filiereStyle);

                row.createCell(5).setCellValue(s.getSalle().getNom());
                row.getCell(5).setCellStyle(rowStyle);

                row.createCell(6).setCellValue(s.getEncadrant().getNom() + " " + s.getEncadrant().getPrenom());
                row.getCell(6).setCellStyle(styleEnc);

                row.createCell(7).setCellValue(s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom());
                row.getCell(7).setCellStyle(styleInfo2);

                row.createCell(8).setCellValue(s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom());
                row.getCell(8).setCellStyle(styleMath2);

                rowIdx++;
            }

            // Auto-size colonnes
            for (int i = 0; i <= 8; i++) sheet.autoSizeColumn(i);

            workbook.write(response.getOutputStream());
            workbook.close();
            return;
        }
     // ── Export Word Planning ──────────────────────────────
        else if ("exportWord".equals(action) && "planning".equals(request.getParameter("type"))) {
            PlanningService planningService = new PlanningServiceImpl();
            List<Soutenance> planning = planningService.getPlanning();

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=planning_soutenances.docx");

            XWPFDocument document = new XWPFDocument();

            // ── Titre ────────────────────────────────────────────
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("Planning des Soutenances PFE");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setColor("1F6FBF");
            titleRun.setFontFamily("Calibri");

            // ── Sous-titre ───────────────────────────────────────
            XWPFParagraph subPara = document.createParagraph();
            subPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun subRun = subPara.createRun();
            subRun.setText("Total : " + planning.size() + " soutenance(s) planifiée(s)");
            subRun.setFontSize(10);
            subRun.setColor("888888");
            subRun.setFontFamily("Calibri");

            // Espace
            document.createParagraph();

            // ── Tableau ──────────────────────────────────────────
            XWPFTable table = document.createTable(1, 10);
            table.setWidth("100%");

            // En-tête
            XWPFTableRow headerRow = table.getRow(0);
            String[] headers = {"Date", "Heure Début", "Heure Fin", "Étudiant", 
                                "Filière", "Salle", "Encadrant", "Jury Info", "Jury Math"};

            // Supprimer la cellule en trop (createTable(1,10) crée 10 mais on en veut 9... on repart de 0)
            // Recréer proprement
            table = document.createTable();
            XWPFTableRow hRow = table.getRow(0);

            String[] cols = {"Date", "Heure Début", "Heure Fin", "Étudiant",
                             "Filière", "Salle", "Encadrant", "Jury Info", "Jury Math"};

            // Cellule 0 déjà créée
            styleHeaderCell(hRow.getCell(0), cols[0]);
            for (int idx = 1; idx < cols.length; idx++) {
                styleHeaderCell(hRow.addNewTableCell(), cols[idx]);
            }

            // Lignes de données
            int rowNum = 0;
            for (Soutenance s : planning) {
                XWPFTableRow row = table.createRow();
                String filiere = s.getEtudiant().getFiliere();

                // Couleur alternée
                String bgAlt = (rowNum % 2 == 0) ? "FFFFFF" : "F5F5F5";

                styleDataCell(row.getCell(0),
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(s.getDateSoutenance()), bgAlt, false);
                styleDataCell(row.getCell(1), s.getHeureDebut(), bgAlt, true);
                styleDataCell(row.getCell(2), s.getHeureFin(), bgAlt, false);

                // Étudiant + couleur filière
                String filiereColor = "GI".equalsIgnoreCase(filiere) ? "B4C6E7" :
                                      "ID".equalsIgnoreCase(filiere) ? "B7E1CD" :
                                      "TDIA".equalsIgnoreCase(filiere) ? "F4B183" : bgAlt;
                styleDataCell(row.getCell(3),
                    s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom(), filiereColor, true);
                styleDataCell(row.getCell(4), filiere, filiereColor, true);
                styleDataCell(row.getCell(5), s.getSalle().getNom(), bgAlt, false);

                // Encadrant — vert
                styleDataCell(row.getCell(6),
                    s.getEncadrant().getNom() + " " + s.getEncadrant().getPrenom(), "C6EFCE", false);

                // Jury Info — bleu cyan
                styleDataCell(row.getCell(7),
                    s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom(), "BDD7EE", false);

                // Jury Math — violet clair
                styleDataCell(row.getCell(8),
                    s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom(), "E2CEFF", false);

                rowNum++;
            }

            document.write(response.getOutputStream());
            document.close();
            return;
        }
     // ── Export PDF Planning ───────────────────────────────
        else if ("exportPdf".equals(action) && "planning".equals(request.getParameter("type"))) {
            PlanningService planningService = new PlanningServiceImpl();
            List<Soutenance> planning = planningService.getPlanning();

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=planning_soutenances.pdf");

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Titre
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD,
                new java.awt.Color(31, 111, 191));
            Paragraph title = new Paragraph("📅 Planning des Soutenances PFE", titleFont);
            title.setSpacingAfter(10);
            document.add(title);

            com.lowagie.text.Font subFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL,
                java.awt.Color.GRAY);
            document.add(new Paragraph("Total : " + planning.size() + " soutenance(s) planifiée(s)", subFont));
            document.add(new Paragraph(" "));

            // Tableau 10 colonnes
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1.5f, 1.5f, 2f, 2f, 1.2f, 1.5f, 2.5f, 2.5f, 2.5f});

            // Style entête
            java.awt.Color headerColor = new java.awt.Color(31, 111, 191);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.BOLD, java.awt.Color.WHITE);

            String[] headers = {"Date", "Heure Début", "Heure Fin", "Nom", "Prénom", 
                                "Filière", "Salle", "Encadrant", "Jury Info", "Jury Math"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Couleurs filières
            java.awt.Color colorGI   = new java.awt.Color(180, 198, 231);
            java.awt.Color colorID   = new java.awt.Color(183, 225, 205);
            java.awt.Color colorTDIA = new java.awt.Color(244, 177, 131);
            java.awt.Color colorRow  = new java.awt.Color(245, 245, 245);

            com.lowagie.text.Font cellFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8);
            com.lowagie.text.Font boldFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD);

            int i = 0;
            for (Soutenance s : planning) {
                java.awt.Color bg = (i % 2 == 0) ? java.awt.Color.WHITE : colorRow;
                String filiere = s.getEtudiant().getFiliere();
                java.awt.Color filiereColor = "GI".equalsIgnoreCase(filiere) ? colorGI :
                                              "ID".equalsIgnoreCase(filiere) ? colorID :
                                              "TDIA".equalsIgnoreCase(filiere) ? colorTDIA : bg;

                // Cellule simple
                java.util.function.Function<String, PdfPCell> cell = txt -> {
                    PdfPCell c = new PdfPCell(new Phrase(txt, cellFont));
                    c.setBackgroundColor(bg);
                    c.setPadding(4);
                    c.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    return c;
                };

                table.addCell(cell.apply(new java.text.SimpleDateFormat("dd/MM/yyyy").format(s.getDateSoutenance())));
                table.addCell(cell.apply(s.getHeureDebut()));
                table.addCell(cell.apply(s.getHeureFin()));

                // Nom en gras + couleur filière
                PdfPCell nomCell = new PdfPCell(new Phrase(s.getEtudiant().getNom(), boldFont));
                nomCell.setBackgroundColor(filiereColor); nomCell.setPadding(4);
                nomCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(nomCell);

                PdfPCell prenomCell = new PdfPCell(new Phrase(s.getEtudiant().getPrenom(), cellFont));
                prenomCell.setBackgroundColor(filiereColor); prenomCell.setPadding(4);
                prenomCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(prenomCell);

                PdfPCell filiereCell = new PdfPCell(new Phrase(filiere, boldFont));
                filiereCell.setBackgroundColor(filiereColor); filiereCell.setPadding(4);
                filiereCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(filiereCell);

                table.addCell(cell.apply(s.getSalle().getNom()));

                // Encadrant — vert
                PdfPCell encCell = new PdfPCell(new Phrase(
                    s.getEncadrant().getNom() + " " + s.getEncadrant().getPrenom(), cellFont));
                encCell.setBackgroundColor(new java.awt.Color(40, 167, 69)); encCell.setPadding(4);
                encCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(encCell);

                // Jury Info — bleu cyan
                PdfPCell infoCell = new PdfPCell(new Phrase(
                    s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom(), cellFont));
                infoCell.setBackgroundColor(new java.awt.Color(23, 162, 184)); infoCell.setPadding(4);
                infoCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(infoCell);

                // Jury Math — violet
                PdfPCell mathCell = new PdfPCell(new Phrase(
                    s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom(), cellFont));
                mathCell.setBackgroundColor(new java.awt.Color(111, 66, 193)); mathCell.setPadding(4);
                mathCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(mathCell);

                i++;
            }

            document.add(table);
            document.close();
            return;
        }
     // ── Export Excel ──────────────────────────────────────
        else if ("exportRepartitionExcel".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();
            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();
            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();
                if (!repartition.containsKey(prof)) repartition.put(prof, new ArrayList<Etudiant>());
                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.xlsx");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Répartition Encadrants");

            // ── Police de base ───────────────────────────────────
            org.apache.poi.ss.usermodel.Font baseFont = workbook.createFont();
            baseFont.setFontName("Calibri");
            baseFont.setFontHeightInPoints((short) 10);

            // ── Titre ────────────────────────────────────────────
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            ((org.apache.poi.xssf.usermodel.XSSFFont) titleFont).setColor(
                new org.apache.poi.xssf.usermodel.XSSFColor(
                    new byte[]{(byte)31, (byte)111, (byte)191}, null));

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Répartition des Encadrants PFE");
            titleCell.setCellStyle(titleStyle);

            // ── Sous-titre ───────────────────────────────────────
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 9));
            Row subRow = sheet.createRow(1);
            Cell subCell = subRow.createCell(0);
            subCell.setCellValue("Total : " + repartition.size() + " encadrant(s)");
            CellStyle subStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font subFont = workbook.createFont();
            subFont.setFontName("Calibri");
            subFont.setItalic(true);
            subFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT.getIndex());
            subStyle.setFont(subFont);
            subCell.setCellStyle(subStyle);

            sheet.createRow(2); // ligne vide

            // ── En-tête ──────────────────────────────────────────
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            ((org.apache.poi.xssf.usermodel.XSSFCellStyle) headerStyle).setFillForegroundColor(
                new org.apache.poi.xssf.usermodel.XSSFColor(
                    new byte[]{(byte)31, (byte)111, (byte)191}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(22);
            String[] cols = {"Encadrant Nom", "Encadrant Prénom",
                             "Etudiant 1 Nom", "Etudiant 1 Prénom",
                             "Etudiant 2 Nom", "Etudiant 2 Prénom",
                             "Etudiant 3 Nom", "Etudiant 3 Prénom",
                             "Etudiant 4 Nom", "Etudiant 4 Prénom"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            // ── Styles filières ──────────────────────────────────
            CellStyle styleGI   = createColorStyle(workbook, (byte)180, (byte)198, (byte)231, baseFont);
            CellStyle styleID   = createColorStyle(workbook, (byte)183, (byte)225, (byte)205, baseFont);
            CellStyle styleTDIA = createColorStyle(workbook, (byte)244, (byte)177, (byte)131, baseFont);
            CellStyle styleWhite = createColorStyle(workbook, (byte)255, (byte)255, (byte)255, baseFont);
            CellStyle styleGray  = createColorStyle(workbook, (byte)245, (byte)245, (byte)245, baseFont);

            // Style encadrant — bleu foncé
            org.apache.poi.ss.usermodel.Font encFont = workbook.createFont();
            encFont.setFontName("Calibri");
            encFont.setBold(true);
            encFont.setFontHeightInPoints((short) 10);
            CellStyle styleEnc = createColorStyle(workbook, (byte)189, (byte)215, (byte)238, encFont);

            // ── Lignes de données ────────────────────────────────
            int rowIdx = 4;
            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {
                Row row = sheet.createRow(rowIdx);
                row.setHeightInPoints(18);

                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();

                CellStyle rowBg = (rowIdx % 2 == 0) ? styleWhite : styleGray;

                row.createCell(0).setCellValue(prof.getNom());
                row.getCell(0).setCellStyle(styleEnc);
                row.createCell(1).setCellValue(prof.getPrenom());
                row.getCell(1).setCellStyle(styleEnc);

                int col = 2;
                for (int i = 0; i < 4; i++) {
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);
                        String filiere = e.getFiliere();
                        CellStyle cs = "GI".equalsIgnoreCase(filiere)   ? styleGI :
                                       "ID".equalsIgnoreCase(filiere)   ? styleID :
                                       "TDIA".equalsIgnoreCase(filiere) ? styleTDIA : rowBg;

                        row.createCell(col).setCellValue(e.getNom());
                        row.getCell(col).setCellStyle(cs);
                        row.createCell(col + 1).setCellValue(e.getPrenom());
                        row.getCell(col + 1).setCellStyle(cs);
                    } else {
                        row.createCell(col).setCellValue("");
                        row.getCell(col).setCellStyle(rowBg);
                        row.createCell(col + 1).setCellValue("");
                        row.getCell(col + 1).setCellStyle(rowBg);
                    }
                    col += 2;
                }
                rowIdx++;
            }

            for (int i = 0; i <= 9; i++) sheet.autoSizeColumn(i);

            workbook.write(response.getOutputStream());
            workbook.close();
            return;
        }
        else if ("exportRepartitionPdf".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();
            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();
            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();
                if (!repartition.containsKey(prof)) repartition.put(prof, new ArrayList<Etudiant>());
                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.pdf");

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // ── Titre ────────────────────────────────────────────
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD,
                new java.awt.Color(31, 111, 191));
            Paragraph title = new Paragraph("Répartition des Encadrants PFE", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(6);
            document.add(title);

            com.lowagie.text.Font subFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.ITALIC,
                java.awt.Color.GRAY);
            Paragraph sub = new Paragraph("Total : " + repartition.size() + " encadrant(s)", subFont);
            sub.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            sub.setSpacingAfter(12);
            document.add(sub);

            // ── Tableau ──────────────────────────────────────────
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f});

            // En-tête
            java.awt.Color headerColor = new java.awt.Color(31, 111, 191);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD, java.awt.Color.WHITE);

            String[] headers = {"Encadrant Nom", "Encadrant Prénom",
                                "Etudiant 1 Nom", "Etudiant 1 Prénom",
                                "Etudiant 2 Nom", "Etudiant 2 Prénom",
                                "Etudiant 3 Nom", "Etudiant 3 Prénom",
                                "Etudiant 4 Nom", "Etudiant 4 Prénom"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Couleurs
            java.awt.Color colorGI   = new java.awt.Color(180, 198, 231);
            java.awt.Color colorID   = new java.awt.Color(183, 225, 205);
            java.awt.Color colorTDIA = new java.awt.Color(244, 177, 131);
            java.awt.Color colorEnc  = new java.awt.Color(189, 215, 238);
            java.awt.Color colorRow  = new java.awt.Color(245, 245, 245);

            com.lowagie.text.Font cellFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8);
            com.lowagie.text.Font boldFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD);

            int rowNum = 0;
            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {
                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();
                java.awt.Color bg = (rowNum % 2 == 0) ? java.awt.Color.WHITE : colorRow;

                // Encadrant — bleu
                PdfPCell cNom = new PdfPCell(new Phrase(prof.getNom(), boldFont));
                cNom.setBackgroundColor(colorEnc); cNom.setPadding(4);
                cNom.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(cNom);

                PdfPCell cPrenom = new PdfPCell(new Phrase(prof.getPrenom(), boldFont));
                cPrenom.setBackgroundColor(colorEnc); cPrenom.setPadding(4);
                cPrenom.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(cPrenom);

                // Étudiants
                for (int i = 0; i < 4; i++) {
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);
                        java.awt.Color fc = "GI".equalsIgnoreCase(e.getFiliere())   ? colorGI :
                                            "ID".equalsIgnoreCase(e.getFiliere())   ? colorID :
                                            "TDIA".equalsIgnoreCase(e.getFiliere()) ? colorTDIA : bg;

                        PdfPCell cn = new PdfPCell(new Phrase(e.getNom(), cellFont));
                        cn.setBackgroundColor(fc); cn.setPadding(4);
                        cn.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                        table.addCell(cn);

                        PdfPCell cp = new PdfPCell(new Phrase(e.getPrenom(), cellFont));
                        cp.setBackgroundColor(fc); cp.setPadding(4);
                        cp.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                        table.addCell(cp);
                    } else {
                        PdfPCell empty = new PdfPCell(new Phrase(""));
                        empty.setBackgroundColor(bg); empty.setPadding(4);
                        table.addCell(empty);
                        table.addCell(empty);
                    }
                }
                rowNum++;
            }

            document.add(table);
            document.close();
            return;
        }
        else if ("exportRepartitionWord".equals(action)) {

            List<GroupPfe> groupes = groupPfeDao.findAllWithDetails();
            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();
            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();
                if (!repartition.containsKey(prof)) repartition.put(prof, new ArrayList<Etudiant>());
                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.docx");

            XWPFDocument document = new XWPFDocument();

            // ── Titre ────────────────────────────────────────────
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("Répartition des Encadrants PFE");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setColor("1F6FBF");
            titleRun.setFontFamily("Calibri");

            // ── Sous-titre ───────────────────────────────────────
            XWPFParagraph subPara = document.createParagraph();
            subPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun subRun = subPara.createRun();
            subRun.setText("Total : " + repartition.size() + " encadrant(s)");
            subRun.setFontSize(10);
            subRun.setColor("888888");
            subRun.setFontFamily("Calibri");

            document.createParagraph(); // espace

            // ── Tableau ──────────────────────────────────────────
            XWPFTable table = document.createTable();

            // En-tête
            XWPFTableRow hRow = table.getRow(0);
            String[] hCols = {"Encadrant Nom", "Encadrant Prénom",
                              "Etudiant 1 Nom", "Etudiant 1 Prénom",
                              "Etudiant 2 Nom", "Etudiant 2 Prénom",
                              "Etudiant 3 Nom", "Etudiant 3 Prénom",
                              "Etudiant 4 Nom", "Etudiant 4 Prénom"};

            styleHeaderCell(hRow.getCell(0), hCols[0]);
            for (int i = 1; i < hCols.length; i++) {
                styleHeaderCell(hRow.addNewTableCell(), hCols[i]);
            }

            // Lignes de données
            int rowNum = 0;
            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {
                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();

                XWPFTableRow row = table.createRow();
                String bgAlt = (rowNum % 2 == 0) ? "FFFFFF" : "F5F5F5";

                // Encadrant — bleu
                styleDataCell(row.getCell(0), prof.getNom(),    "BDD7EE", true);
                styleDataCell(row.getCell(1), prof.getPrenom(), "BDD7EE", true);

                int col = 2;
                for (int i = 0; i < 4; i++) {
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);
                        String fc = "GI".equalsIgnoreCase(e.getFiliere())   ? "B4C6E7" :
                                    "ID".equalsIgnoreCase(e.getFiliere())   ? "B7E1CD" :
                                    "TDIA".equalsIgnoreCase(e.getFiliere()) ? "F4B183" : bgAlt;

                        styleDataCell(row.getCell(col),     e.getNom(),    fc, false);
                        styleDataCell(row.getCell(col + 1), e.getPrenom(), fc, false);
                    } else {
                        styleDataCell(row.getCell(col),     "", bgAlt, false);
                        styleDataCell(row.getCell(col + 1), "", bgAlt, false);
                    }
                    col += 2;
                }
                rowNum++;
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

        pv.genererPv(response,s);
    }
    
    else if ("listePv".equals(action)) {

        SoutenanceDao soutenanceDao = new SoutenanceDaoImpl();

        List<Soutenance> soutenances = soutenanceDao.findAll();

        Map<Professeur, List<Soutenance>> pvParProf = new LinkedHashMap<>();

        for (Soutenance s : soutenances) {
            Professeur encadrant = s.getEncadrant();

            if (!pvParProf.containsKey(encadrant)) {
                pvParProf.put(encadrant, new ArrayList<Soutenance>());
            }

            pvParProf.get(encadrant).add(s);
        }

        String profIdParam = request.getParameter("profId");

        if (profIdParam != null && !profIdParam.trim().isEmpty()) {
            Long profId = Long.parseLong(profIdParam);
            request.setAttribute("profIdSelectionne", profId);
        }

        request.setAttribute("pvParProf", pvParProf);

        request.getRequestDispatcher("/WEB-INF/views/pvpage.jsp")
               .forward(request, response);

        return;
    }
    
    if ("importPv".equals(action)) {

        Part fichier = request.getPart("fichierPv");

        // ici tu lis le fichier Excel
        // puis tu récupères les soutenances depuis la base

        SoutenanceDao soutenanceDao = new SoutenanceDaoImpl();

        List<Soutenance> soutenances = soutenanceDao.findAll();

        Map<Professeur, List<Soutenance>> pvParProf = new LinkedHashMap<>();

        for (Soutenance s : soutenances) {

            Professeur encadrant = s.getEncadrant();

            if (!pvParProf.containsKey(encadrant)) {
                pvParProf.put(encadrant, new ArrayList<Soutenance>());
            }

            pvParProf.get(encadrant).add(s);
        }

        request.setAttribute("pvParProf", pvParProf);

        request.getRequestDispatcher("/WEB-INF/views/pvpage.jsp")
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

        // ── Import planning Excel exporté → PV ───────────────
        else if ("importerPlanningEtGenererPv".equals(action)) {

            Part filePart = request.getPart("fichierPlanningExcel");

            if (filePart == null || filePart.getSize() == 0) {
                request.setAttribute("erreur", "Veuillez sélectionner un fichier Excel valide.");
                request.getRequestDispatcher("/WEB-INF/views/importerPlanningPv.jsp")
                       .forward(request, response);
                return;
            }

            try (InputStream input = filePart.getInputStream();
                 Workbook workbook = WorkbookFactory.create(input)) {

                Sheet sheet = workbook.getSheet("Planning Soutenances");

                if (sheet == null) {
                    request.setAttribute("erreur",
                        "Feuille 'Planning Soutenances' introuvable. Vérifiez que le fichier est bien un planning exporté.");
                    request.getRequestDispatcher("/WEB-INF/views/importerPlanningPv.jsp")
                           .forward(request, response);
                    return;
                }

                // Charger toutes les soutenances en base
                SoutenanceDao soutenanceDao = new SoutenanceDaoImpl();
                List<Soutenance> toutesLesSoutenances = soutenanceDao.findAll();

                // Indexer par nom étudiant (nom+prenom) en minuscules pour recherche rapide
                Map<String, Soutenance> indexEtudiant = new java.util.HashMap<>();
                for (Soutenance s : toutesLesSoutenances) {
                    String cle = (s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom()).toLowerCase().trim();
                    indexEtudiant.put(cle, s);
                    // Aussi indexer nom seul au cas où
                    indexEtudiant.put(s.getEtudiant().getNom().toLowerCase().trim(), s);
                }

                // Lire le fichier Excel exporté.
                // Ligne 0 = titre, ligne 1 = sous-titre, ligne 2 = en-tête colonnes, lignes 3+ = données
                // Colonnes : 0=Date, 1=Heure Début, 2=Heure Fin, 3=Étudiant (Nom Prénom),
                //            4=Filière, 5=Salle, 6=Encadrant, 7=Jury Info, 8=Jury Math
                List<Soutenance> soutenancesFromPlanning = new ArrayList<>();

                // Trouver la ligne d'en-tête (chercher la cellule "Date")
                int dataStartRow = 3; // Par défaut après titre, sous-titre, en-tête
                for (int r = 0; r <= Math.min(5, sheet.getLastRowNum()); r++) {
                    Row row = sheet.getRow(r);
                    if (row != null) {
                        Cell cell0 = row.getCell(0);
                        if (cell0 != null) {
                            String val = "";
                            if (cell0.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                val = cell0.getStringCellValue().trim();
                            }
                            if ("Date".equalsIgnoreCase(val)) {
                                dataStartRow = r + 1;
                                break;
                            }
                        }
                    }
                }

                for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Cell cellEtudiant = row.getCell(3);
                    if (cellEtudiant == null) continue;

                    String nomEtudiantCell = "";
                    if (cellEtudiant.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                        nomEtudiantCell = cellEtudiant.getStringCellValue().trim();
                    }
                    if (nomEtudiantCell.isEmpty()) continue;

                    // Chercher la soutenance correspondante
                    String cleLookup = nomEtudiantCell.toLowerCase().trim();
                    Soutenance s = indexEtudiant.get(cleLookup);

                    // Si pas trouvé, essayer avec juste le premier mot (nom)
                    if (s == null) {
                        String[] parts = cleLookup.split("\\s+");
                        if (parts.length > 0) {
                            s = indexEtudiant.get(parts[0]);
                        }
                    }

                    if (s != null && !soutenancesFromPlanning.contains(s)) {
                        soutenancesFromPlanning.add(s);
                    }
                }

                if (soutenancesFromPlanning.isEmpty()) {
                    // Aucune correspondance : utiliser toutes les soutenances en base
                    soutenancesFromPlanning = toutesLesSoutenances;
                }

                // Construire la map pvParProf à partir des soutenances trouvées
                Map<Professeur, List<Soutenance>> pvParProf = new java.util.LinkedHashMap<>();
                for (Soutenance s : soutenancesFromPlanning) {
                    Professeur encadrant = s.getEncadrant();
                    if (!pvParProf.containsKey(encadrant)) {
                        pvParProf.put(encadrant, new ArrayList<Soutenance>());
                    }
                    pvParProf.get(encadrant).add(s);
                }

                request.setAttribute("pvParProf", pvParProf);
                request.setAttribute("sourceImport", true);
                request.getRequestDispatcher("/WEB-INF/views/pvpage.jsp")
                       .forward(request, response);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("erreur",
                    "Erreur lors de la lecture du fichier : " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/importerPlanningPv.jsp")
                       .forward(request, response);
                return;
            }
        }
    }
 // ── Méthode helper : cellule d'en-tête ───────────────
    private void styleHeaderCell(XWPFTableCell cell, String text) {
        cell.setColor("1F6FBF");
        org.apache.poi.xwpf.usermodel.XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
        run.setColor("FFFFFF");
        run.setFontSize(9);
        run.setFontFamily("Calibri");
    }

    // ── Méthode helper : cellule de données ──────────────
    private void styleDataCell(XWPFTableCell cell, String text, String bgColor, boolean bold) {
        cell.setColor(bgColor);
        org.apache.poi.xwpf.usermodel.XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(8);
        run.setFontFamily("Calibri");
    }
    private CellStyle createColorStyle(Workbook workbook, byte r, byte g, byte b,
            org.apache.poi.ss.usermodel.Font font) {
CellStyle style = workbook.createCellStyle();
style.setFont(font);
style.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(
new byte[]{r, g, b}, null));
style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
return style;
}
}