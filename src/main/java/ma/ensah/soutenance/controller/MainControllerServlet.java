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
         
            // ── Palette couleurs professeurs (RGB bytes) ─────────────────────
            byte[][] profPalette = {
                {(byte)255,(byte)153,(byte)153}, {(byte)255,(byte)204,(byte)102},
                {(byte)153,(byte)204,(byte)255}, {(byte)153,(byte)255,(byte)153},
                {(byte)204,(byte)153,(byte)255}, {(byte)255,(byte)153,(byte)204},
                {(byte)102,(byte)204,(byte)204}, {(byte)255,(byte)178,(byte)102},
                {(byte)178,(byte)255,(byte)102}, {(byte)102,(byte)178,(byte)255},
                {(byte)255,(byte)102,(byte)178}, {(byte)178,(byte)102,(byte)255},
                {(byte)102,(byte)255,(byte)178}, {(byte)255,(byte)230,(byte)102},
                {(byte)255,(byte)102,(byte)102}, {(byte)102,(byte)255,(byte)230},
            };
            Map<String, byte[]> profColorMap = new java.util.LinkedHashMap<>();
            int ci = 0;
            for (Soutenance s : planning) {
                String kE = s.getEncadrant().getNom()  + " " + s.getEncadrant().getPrenom();
                String k1 = s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom();
                String k2 = s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom();
                if (!profColorMap.containsKey(kE)) profColorMap.put(kE, profPalette[ci++ % profPalette.length]);
                if (!profColorMap.containsKey(k1)) profColorMap.put(k1, profPalette[ci++ % profPalette.length]);
                if (!profColorMap.containsKey(k2)) profColorMap.put(k2, profPalette[ci++ % profPalette.length]);
            }
         
            // ── Palette couleurs heures ──────────────────────────────────────
            byte[][] heurePalette = {
                {(byte)255,(byte)255,(byte)153}, // 09h - jaune
                {(byte)198,(byte)239,(byte)206}, // 10h - vert clair
                {(byte)189,(byte)215,(byte)238}, // 11h - bleu clair
                {(byte)255,(byte)204,(byte)153}, // 14h - orange clair
                {(byte)255,(byte)182,(byte)193}, // 15h - rose clair
                {(byte)216,(byte)191,(byte)216}, // 16h - violet clair
                {(byte)173,(byte)216,(byte)230}, // 17h - bleu pâle
                {(byte)152,(byte)251,(byte)152}, // 18h - vert pâle
            };
            Map<String, byte[]> heureColorMap = new java.util.LinkedHashMap<>();
            List<String> heuresVues = new java.util.ArrayList<>();
            for (Soutenance s : planning) {
                if (!heuresVues.contains(s.getHeureDebut())) heuresVues.add(s.getHeureDebut());
            }
            for (int hi = 0; hi < heuresVues.size(); hi++) {
                heureColorMap.put(heuresVues.get(hi), heurePalette[hi % heurePalette.length]);
            }
         
            // ── Palette couleurs dates ───────────────────────────────────────
            byte[][] datePalette = {
                {(byte)235,(byte)241,(byte)255},
                {(byte)235,(byte)255,(byte)241},
                {(byte)255,(byte)241,(byte)235},
                {(byte)241,(byte)235,(byte)255},
            };
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            Map<String, byte[]> dateColorMap = new java.util.LinkedHashMap<>();
            List<String> datesVues = new java.util.ArrayList<>();
            for (Soutenance s : planning) {
                String d = sdf.format(s.getDateSoutenance());
                if (!datesVues.contains(d)) datesVues.add(d);
            }
            for (int di = 0; di < datesVues.size(); di++) {
                dateColorMap.put(datesVues.get(di), datePalette[di % datePalette.length]);
            }
         
            // ── Cache de CellStyles ──────────────────────────────────────────
            Map<String, CellStyle> styleCache = new java.util.HashMap<>();
         
            // Polices
            org.apache.poi.ss.usermodel.Font baseFont = workbook.createFont();
            baseFont.setFontName("Calibri"); baseFont.setFontHeightInPoints((short)9);
         
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setFontName("Calibri"); boldFont.setBold(true); boldFont.setFontHeightInPoints((short)9);
         
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setFontName("Calibri"); titleFont.setBold(true); titleFont.setFontHeightInPoints((short)14);
         
            org.apache.poi.ss.usermodel.Font subFont = workbook.createFont();
            subFont.setFontName("Calibri"); subFont.setBold(true); subFont.setFontHeightInPoints((short)11);
         
            org.apache.poi.ss.usermodel.Font normFont = workbook.createFont();
            normFont.setFontName("Calibri"); normFont.setFontHeightInPoints((short)10);
         
            org.apache.poi.ss.usermodel.Font italFont = workbook.createFont();
            italFont.setFontName("Calibri"); italFont.setItalic(true); italFont.setFontHeightInPoints((short)10);
         
            // Helper createStyle
            java.util.function.BiFunction<byte[], org.apache.poi.ss.usermodel.Font, CellStyle> mkStyle = (rgb, font) -> {
                String key = (font.getBold() ? "B" : "N") + "_" + (rgb[0]&0xFF) + "_" + (rgb[1]&0xFF) + "_" + (rgb[2]&0xFF);
                if (styleCache.containsKey(key)) return styleCache.get(key);
                CellStyle st = workbook.createCellStyle();
                st.setFont(font);
                st.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(rgb, null));
                st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                st.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
                st.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
                st.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                st.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                st.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                st.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                styleCache.put(key, st); return st;
            };
         
            byte[] WHITE = {(byte)255,(byte)255,(byte)255};
            byte[] GRAY  = {(byte)245,(byte)245,(byte)245};
            byte[] GI    = {(byte)180,(byte)198,(byte)231};
            byte[] ID    = {(byte)183,(byte)225,(byte)205};
            byte[] TDIA  = {(byte)244,(byte)177,(byte)131};
         
            // ── Styles en-tête institutionnel ────────────────────────────────
            CellStyle stTitle = workbook.createCellStyle();
            stTitle.setFont(titleFont); stTitle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
         
            CellStyle stSub = workbook.createCellStyle();
            stSub.setFont(subFont); stSub.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
         
            CellStyle stNorm = workbook.createCellStyle();
            stNorm.setFont(normFont); stNorm.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
         
            CellStyle stItal = workbook.createCellStyle();
            stItal.setFont(italFont); stItal.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
         
            int NCOLS = 10;
         
            // ── Lignes titre (0-4) ───────────────────────────────────────────
            String[][] titres = {
                {"Ecole Nationale des Sciences Appliquées - Al Hoceima", "T"},
                {"Département Mathématiques et Informatique", "S"},
                {"Planning des soutenances des Projets de Fin d'Etude", "N"},
                {"(Première Session)", "I"},
                {"Année Universitaire 2024/2025", "N"},
            };
            int rowN = 0;
            for (String[] t : titres) {
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowN, rowN, 0, NCOLS-1));
                Row r = sheet.createRow(rowN++);
                r.setHeightInPoints(t[1].equals("T") ? 22 : 18);
                Cell c = r.createCell(0);
                c.setCellValue(t[0]);
                c.setCellStyle(t[1].equals("T") ? stTitle : t[1].equals("S") ? stSub : t[1].equals("I") ? stItal : stNorm);
            }
         
            // ── Ligne vide (5) ───────────────────────────────────────────────
            sheet.createRow(rowN++);
         
            // ── Légende filières (6-8) centrée sur colonnes 3-6 ─────────────
            // GI
            Row rLeg1 = sheet.createRow(rowN);
            Cell lgGIbox = rLeg1.createCell(3); lgGIbox.setCellValue(""); lgGIbox.setCellStyle(mkStyle.apply(GI, baseFont));
            Cell lgGItxt = rLeg1.createCell(4); lgGItxt.setCellValue("Filière GI");
            rowN++;
         
            // ID
            Row rLeg2 = sheet.createRow(rowN);
            Cell lgIDbox = rLeg2.createCell(3); lgIDbox.setCellValue(""); lgIDbox.setCellStyle(mkStyle.apply(ID, baseFont));
            Cell lgIDtxt = rLeg2.createCell(4); lgIDtxt.setCellValue("Filière ID");
            rowN++;
         
            // TDIA
            Row rLeg3 = sheet.createRow(rowN);
            Cell lgTDbox = rLeg3.createCell(3); lgTDbox.setCellValue(""); lgTDbox.setCellStyle(mkStyle.apply(TDIA, baseFont));
            Cell lgTDtxt = rLeg3.createCell(4); lgTDtxt.setCellValue("Filière TDIA");
            rowN++;
         
            // ── Ligne vide ───────────────────────────────────────────────────
            sheet.createRow(rowN++);
         
            // ── En-tête tableau ──────────────────────────────────────────────
            org.apache.poi.ss.usermodel.Font hdrFont = workbook.createFont();
            hdrFont.setFontName("Calibri"); hdrFont.setBold(true);
            hdrFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            hdrFont.setFontHeightInPoints((short)9);
         
            CellStyle hdrStyle = workbook.createCellStyle();
            hdrStyle.setFont(hdrFont);
            hdrStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)50,(byte)50,(byte)50}, null));
            hdrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hdrStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            hdrStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            hdrStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            hdrStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            hdrStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            hdrStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            hdrStyle.setWrapText(true);
         
            Row headerRow = sheet.createRow(rowN++);
            headerRow.setHeightInPoints(28);
            String[] cols = {"ID","Encadrant","Membre de jury 1","Membre de jury 2","Date","Heure","Salle","Nom d'étudiant","Prénom d'étudiant","Filière"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hdrStyle);
            }
         
            // ── Lignes données ───────────────────────────────────────────────
            int idRow = 1;
            for (Soutenance s : planning) {
                Row row = sheet.createRow(rowN++);
                row.setHeightInPoints(18);
         
                String kE  = s.getEncadrant().getNom()  + " " + s.getEncadrant().getPrenom();
                String k1  = s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom();
                String k2  = s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom();
                String fil = s.getEtudiant().getFiliere();
                String dateStr = sdf.format(s.getDateSoutenance());
         
                byte[] rgbEnc  = profColorMap.get(kE);
                byte[] rgbJ1   = profColorMap.get(k1);
                byte[] rgbJ2   = profColorMap.get(k2);
                byte[] rgbFil  = "GI".equalsIgnoreCase(fil) ? GI : "ID".equalsIgnoreCase(fil) ? ID : "TDIA".equalsIgnoreCase(fil) ? TDIA : WHITE;
                byte[] rgbDate = dateColorMap.getOrDefault(dateStr, WHITE);
                byte[] rgbHeur = heureColorMap.getOrDefault(s.getHeureDebut(), WHITE);
         
                // Col 0 : ID
                Cell c0 = row.createCell(0); c0.setCellValue(idRow); c0.setCellStyle(mkStyle.apply(WHITE, baseFont));
                // Col 1 : Encadrant (gras)
                Cell c1 = row.createCell(1); c1.setCellValue(kE);  c1.setCellStyle(mkStyle.apply(rgbEnc, boldFont));
                // Col 2 : Jury 1
                Cell c2 = row.createCell(2); c2.setCellValue(k1);  c2.setCellStyle(mkStyle.apply(rgbJ1, baseFont));
                // Col 3 : Jury 2
                Cell c3 = row.createCell(3); c3.setCellValue(k2);  c3.setCellStyle(mkStyle.apply(rgbJ2, baseFont));
                // Col 4 : Date (couleur jour)
                Cell c4 = row.createCell(4); c4.setCellValue(dateStr); c4.setCellStyle(mkStyle.apply(rgbDate, baseFont));
                // Col 5 : Heure (couleur créneau)
                Cell c5 = row.createCell(5); c5.setCellValue(s.getHeureDebut()); c5.setCellStyle(mkStyle.apply(rgbHeur, baseFont));
                // Col 6 : Salle
                Cell c6 = row.createCell(6); c6.setCellValue(s.getSalle().getNom()); c6.setCellStyle(mkStyle.apply(WHITE, baseFont));
                // Col 7 : Nom étudiant
                Cell c7 = row.createCell(7); c7.setCellValue(s.getEtudiant().getNom()); c7.setCellStyle(mkStyle.apply(rgbFil, baseFont));
                // Col 8 : Prénom étudiant
                Cell c8 = row.createCell(8); c8.setCellValue(s.getEtudiant().getPrenom()); c8.setCellStyle(mkStyle.apply(rgbFil, baseFont));
                // Col 9 : Filière (gras)
                Cell c9 = row.createCell(9); c9.setCellValue(fil); c9.setCellStyle(mkStyle.apply(rgbFil, boldFont));
         
                idRow++;
            }
         
            // ── Auto-size ────────────────────────────────────────────────────
            for (int i = 0; i < NCOLS; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }
         
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
         
            // ── Palette couleurs professeurs (hex) ───────────────────────────
            String[] profPaletteHex = {
                "FF9999","FFCC66","99CCFF","99FF99","CC99FF","FF99CC",
                "66CCCC","FFB266","B2FF66","66B2FF","FF66B2","B266FF",
                "66FFB2","FFE666","FF6666","66FFE6",
            };
            Map<String, String> profColorMap = new java.util.LinkedHashMap<>();
            int ci = 0;
            for (Soutenance s : planning) {
                String kE = s.getEncadrant().getNom()  + " " + s.getEncadrant().getPrenom();
                String k1 = s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom();
                String k2 = s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom();
                if (!profColorMap.containsKey(kE)) profColorMap.put(kE, profPaletteHex[ci++ % profPaletteHex.length]);
                if (!profColorMap.containsKey(k1)) profColorMap.put(k1, profPaletteHex[ci++ % profPaletteHex.length]);
                if (!profColorMap.containsKey(k2)) profColorMap.put(k2, profPaletteHex[ci++ % profPaletteHex.length]);
            }
         
            // ── Palette couleurs heures (hex) ────────────────────────────────
            String[] heurePaletteHex = {
                "FFFF99", // 09h - jaune
                "C6EFCE", // 10h - vert clair
                "BDD7EE", // 11h - bleu clair
                "FFCC99", // 14h - orange clair
                "FFB6C1", // 15h - rose clair
                "D8BFD8", // 16h - violet clair
                "ADD8E6", // 17h - bleu pâle
                "98FB98", // 18h - vert pâle
            };
            Map<String, String> heureColorMap = new java.util.LinkedHashMap<>();
            List<String> heuresVues = new java.util.ArrayList<>();
            for (Soutenance s : planning) {
                if (!heuresVues.contains(s.getHeureDebut())) heuresVues.add(s.getHeureDebut());
            }
            for (int hi = 0; hi < heuresVues.size(); hi++) {
                heureColorMap.put(heuresVues.get(hi), heurePaletteHex[hi % heurePaletteHex.length]);
            }
         
            // ── Palette couleurs dates (hex) ─────────────────────────────────
            String[] datePaletteHex = {"EBF1FF","EBFFF1","FFF1EB","F1EBFF"};
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            Map<String, String> dateColorMap = new java.util.LinkedHashMap<>();
            List<String> datesVues = new java.util.ArrayList<>();
            for (Soutenance s : planning) {
                String d = sdf.format(s.getDateSoutenance());
                if (!datesVues.contains(d)) datesVues.add(d);
            }
            for (int di = 0; di < datesVues.size(); di++) {
                dateColorMap.put(datesVues.get(di), datePaletteHex[di % datePaletteHex.length]);
            }
         
            // ── Couleurs filières ────────────────────────────────────────────
            String hexGI = "B4C6E7", hexID = "B7E1CD", hexTDIA = "F4B183";
         
            // ── Helper : créer un paragraphe centré ──────────────────────────
            java.util.function.Consumer<Object[]> addPara = args -> {
                // args : [text, bold, italic, size]
                XWPFParagraph pr = document.createParagraph();
                pr.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
                XWPFRun run = pr.createRun();
                run.setText((String) args[0]);
                run.setBold((Boolean) args[1]);
                run.setItalic((Boolean) args[2]);
                run.setFontSize((Integer) args[3]);
                run.setFontFamily("Calibri");
            };
         
            // ── En-tête institutionnel ───────────────────────────────────────
            addPara.accept(new Object[]{"Ecole Nationale des Sciences Appliquées - Al Hoceima", true,  false, 14});
            addPara.accept(new Object[]{"Département Mathématiques et Informatique",             true,  false, 11});
            addPara.accept(new Object[]{"Planning des soutenances des Projets de Fin d'Etude",  false, false, 10});
            addPara.accept(new Object[]{"(Première Session)",                                    false, true,  10});
            addPara.accept(new Object[]{"Année Universitaire 2024/2025",                        false, false, 10});
         
            document.createParagraph(); // ligne vide
         
            // ── Légende filières (tableau 3x2) ───────────────────────────────
            XWPFTable leg = document.createTable(3, 2);
            leg.setWidth("35%");
            styleLegendCell(leg.getRow(0).getCell(0), " ",            hexGI);
            styleLegendCell(leg.getRow(0).getCell(1), "Filière GI",   "FFFFFF");
            styleLegendCell(leg.getRow(1).getCell(0), " ",            hexID);
            styleLegendCell(leg.getRow(1).getCell(1), "Filière ID",   "FFFFFF");
            styleLegendCell(leg.getRow(2).getCell(0), " ",            hexTDIA);
            styleLegendCell(leg.getRow(2).getCell(1), "Filière TDIA", "FFFFFF");
         
            document.createParagraph(); // ligne vide
         
            // ── Tableau principal (10 colonnes) ─────────────────────────────
            XWPFTable table = document.createTable();
            table.setWidth("100%");
         
            // En-tête
            XWPFTableRow hRow = table.getRow(0);
            String[] hCols = {"ID","Encadrant","Membre de jury 1","Membre de jury 2",
                              "Date","Heure","Salle","Nom d'étudiant","Prénom d'étudiant","Filière"};
            styleHeaderCell(hRow.getCell(0), hCols[0]);
            for (int i = 1; i < hCols.length; i++) styleHeaderCell(hRow.addNewTableCell(), hCols[i]);
         
            // ── Helper styleDataCell étendu avec contrôle bold ───────────────
            // (utilise la méthode styleDataCell existante dans votre servlet)
         
            // Lignes de données
            int idRow = 1;
            for (Soutenance s : planning) {
                XWPFTableRow row = table.createRow();
                while (row.getTableCells().size() < 10) row.addNewTableCell();
         
                String kE  = s.getEncadrant().getNom()  + " " + s.getEncadrant().getPrenom();
                String k1  = s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom();
                String k2  = s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom();
                String fil = s.getEtudiant().getFiliere();
                String dateStr = sdf.format(s.getDateSoutenance());
         
                String cEnc  = profColorMap.get(kE);
                String cJ1   = profColorMap.get(k1);
                String cJ2   = profColorMap.get(k2);
                String cFil  = "GI".equalsIgnoreCase(fil) ? hexGI : "ID".equalsIgnoreCase(fil) ? hexID : "TDIA".equalsIgnoreCase(fil) ? hexTDIA : "FFFFFF";
                String cDate = dateColorMap.getOrDefault(dateStr, "FFFFFF");
                String cHeur = heureColorMap.getOrDefault(s.getHeureDebut(), "FFFFFF");
         
                styleDataCell(row.getCell(0), String.valueOf(idRow), "FFFFFF", false);
                styleDataCell(row.getCell(1), kE,                    cEnc,     true);
                styleDataCell(row.getCell(2), k1,                    cJ1,      false);
                styleDataCell(row.getCell(3), k2,                    cJ2,      false);
                styleDataCell(row.getCell(4), dateStr,               cDate,    false);
                styleDataCell(row.getCell(5), s.getHeureDebut(),     cHeur,    false);
                styleDataCell(row.getCell(6), s.getSalle().getNom(), "FFFFFF", false);
                styleDataCell(row.getCell(7), s.getEtudiant().getNom(),    cFil, false);
                styleDataCell(row.getCell(8), s.getEtudiant().getPrenom(), cFil, false);
                styleDataCell(row.getCell(9), fil,                   cFil,     true);
         
                idRow++;
            }
         
            // ── Orientation paysage A4 ───────────────────────────────────────
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody body =
                document.getDocument().getBody();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr =
                body.isSetSectPr() ? body.getSectPr() : body.addNewSectPr();
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz pgSz =
                sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
            pgSz.setW(java.math.BigInteger.valueOf(16838));
            pgSz.setH(java.math.BigInteger.valueOf(11906));
            pgSz.setOrient(org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation.LANDSCAPE);
         
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
         
            // ── Polices ──────────────────────────────────────────────────────
            com.lowagie.text.Font fTitle   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font fBold11  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font fNorm10  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10);
            com.lowagie.text.Font fItal10  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.ITALIC);
            com.lowagie.text.Font fHdr     = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  8, com.lowagie.text.Font.BOLD, java.awt.Color.WHITE);
            com.lowagie.text.Font fCell    = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  7);
            com.lowagie.text.Font fCellB   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  7, com.lowagie.text.Font.BOLD);
         
            // ── En-tête institutionnel ───────────────────────────────────────
            Paragraph p;
            p = new Paragraph("Ecole Nationale des Sciences Appliquées - Al Hoceima", fTitle);
            p.setAlignment(com.lowagie.text.Element.ALIGN_CENTER); document.add(p);
         
            p = new Paragraph("Département Mathématiques et Informatique", fBold11);
            p.setAlignment(com.lowagie.text.Element.ALIGN_CENTER); document.add(p);
         
            p = new Paragraph("Planning des soutenances des Projets de Fin d'Etude", fNorm10);
            p.setAlignment(com.lowagie.text.Element.ALIGN_CENTER); document.add(p);
         
            p = new Paragraph("(Première Session)", fItal10);
            p.setAlignment(com.lowagie.text.Element.ALIGN_CENTER); document.add(p);
         
            p = new Paragraph("Année Universitaire 2024/2025", fNorm10);
            p.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            p.setSpacingAfter(10); document.add(p);
         
            // ── Couleurs filières ────────────────────────────────────────────
            java.awt.Color cGI   = new java.awt.Color(180, 198, 231);
            java.awt.Color cID   = new java.awt.Color(183, 225, 205);
            java.awt.Color cTDIA = new java.awt.Color(244, 177, 131);
         
            // ── Légende filières (centrée) ───────────────────────────────────
            PdfPTable legend = new PdfPTable(6);
            legend.setWidthPercentage(55);
            legend.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            legend.setWidths(new float[]{0.6f, 2f, 0.6f, 1.6f, 0.6f, 2f});
            legend.setSpacingAfter(8);
         
            java.util.function.BiConsumer<java.awt.Color, String> addLeg = (col, txt) -> {
                PdfPCell box = new PdfPCell(new Phrase(" "));
                box.setBackgroundColor(col);
                box.setBorder(com.lowagie.text.Rectangle.BOX);
                box.setPadding(5);
                legend.addCell(box);
                PdfPCell lbl = new PdfPCell(new Phrase(txt, fCell));
                lbl.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                lbl.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                legend.addCell(lbl);
            };
            addLeg.accept(cGI,   "Filière GI");
            addLeg.accept(cID,   "Filière ID");
            addLeg.accept(cTDIA, "Filière TDIA");
            document.add(legend);
         
            // ── Palette couleurs professeurs ─────────────────────────────────
            java.awt.Color[] profPalette = {
                new java.awt.Color(255,153,153), new java.awt.Color(255,204,102),
                new java.awt.Color(153,204,255), new java.awt.Color(153,255,153),
                new java.awt.Color(204,153,255), new java.awt.Color(255,153,204),
                new java.awt.Color(102,204,204), new java.awt.Color(255,178,102),
                new java.awt.Color(178,255,102), new java.awt.Color(102,178,255),
                new java.awt.Color(255,102,178), new java.awt.Color(178,102,255),
                new java.awt.Color(102,255,178), new java.awt.Color(255,230,102),
                new java.awt.Color(255,102,102), new java.awt.Color(102,255,230),
            };
            Map<String, java.awt.Color> profColorMap = new java.util.LinkedHashMap<>();
            int ci = 0;
            for (Soutenance s : planning) {
                String kE = s.getEncadrant().getNom()  + " " + s.getEncadrant().getPrenom();
                String k1 = s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom();
                String k2 = s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom();
                if (!profColorMap.containsKey(kE)) profColorMap.put(kE, profPalette[ci++ % profPalette.length]);
                if (!profColorMap.containsKey(k1)) profColorMap.put(k1, profPalette[ci++ % profPalette.length]);
                if (!profColorMap.containsKey(k2)) profColorMap.put(k2, profPalette[ci++ % profPalette.length]);
            }
         
            // ── Couleurs créneaux horaires ───────────────────────────────────
            Map<String, java.awt.Color> heureColorMap = new java.util.LinkedHashMap<>();
            java.awt.Color[] heurePalette = {
                new java.awt.Color(255,255,153), // 09h - jaune
                new java.awt.Color(198,239,206), // 10h - vert clair
                new java.awt.Color(189,215,238), // 11h - bleu clair
                new java.awt.Color(255,204,153), // 14h - orange clair
                new java.awt.Color(255,182,193), // 15h - rose clair
                new java.awt.Color(216,191,216), // 16h - violet clair
                new java.awt.Color(173,216,230), // 17h - bleu pâle
                new java.awt.Color(152,251,152), // 18h - vert pâle
            };
            // Collecte des heures uniques dans l'ordre d'apparition
            List<String> heuresVues = new java.util.ArrayList<>();
            for (Soutenance s : planning) {
                if (!heuresVues.contains(s.getHeureDebut())) heuresVues.add(s.getHeureDebut());
            }
            for (int hi = 0; hi < heuresVues.size(); hi++) {
                heureColorMap.put(heuresVues.get(hi), heurePalette[hi % heurePalette.length]);
            }
         
            // ── Couleurs dates ───────────────────────────────────────────────
            Map<String, java.awt.Color> dateColorMap = new java.util.LinkedHashMap<>();
            java.awt.Color[] datePalette = {
                new java.awt.Color(235,241,255), // jour 1
                new java.awt.Color(235,255,241), // jour 2
                new java.awt.Color(255,241,235), // jour 3
                new java.awt.Color(241,235,255), // jour 4
            };
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            List<String> datesVues = new java.util.ArrayList<>();
            for (Soutenance s : planning) {
                String d = sdf.format(s.getDateSoutenance());
                if (!datesVues.contains(d)) datesVues.add(d);
            }
            for (int di = 0; di < datesVues.size(); di++) {
                dateColorMap.put(datesVues.get(di), datePalette[di % datePalette.length]);
            }
         
            // ── Tableau : 10 colonnes ────────────────────────────────────────
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.2f, 2.3f, 2.3f, 1.5f, 0.9f, 1f, 1.8f, 1.8f, 0.9f});
         
            java.awt.Color headerBg = new java.awt.Color(50, 50, 50);
            String[] hdrs = {"ID","Encadrant","Membre de jury 1","Membre de jury 2","Date","Heure","Salle","Nom d'étudiant","Prénom d'étudiant","Filière"};
            for (String h : hdrs) {
                PdfPCell c = new PdfPCell(new Phrase(h, fHdr));
                c.setBackgroundColor(headerBg);
                c.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                c.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                c.setPadding(4); table.addCell(c);
            }
         
            // Helper cellule
            java.util.function.Function<java.awt.Color, java.util.function.BiFunction<String, Boolean, PdfPCell>> mkCell =
                bg -> (txt, bold) -> {
                    PdfPCell c = new PdfPCell(new Phrase(txt, bold ? fCellB : fCell));
                    if (bg != null) c.setBackgroundColor(bg);
                    c.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    c.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    c.setPadding(3); return c;
                };
         
            int idRow = 1;
            for (Soutenance s : planning) {
                String kE  = s.getEncadrant().getNom()  + " " + s.getEncadrant().getPrenom();
                String k1  = s.getMembreInfo().getNom() + " " + s.getMembreInfo().getPrenom();
                String k2  = s.getMembreMath().getNom() + " " + s.getMembreMath().getPrenom();
                String fil = s.getEtudiant().getFiliere();
                String dateStr = sdf.format(s.getDateSoutenance());
         
                java.awt.Color cEnc  = profColorMap.get(kE);
                java.awt.Color cJ1   = profColorMap.get(k1);
                java.awt.Color cJ2   = profColorMap.get(k2);
                java.awt.Color cFil  = "GI".equalsIgnoreCase(fil) ? cGI : "ID".equalsIgnoreCase(fil) ? cID : "TDIA".equalsIgnoreCase(fil) ? cTDIA : java.awt.Color.WHITE;
                java.awt.Color cDate = dateColorMap.getOrDefault(dateStr, java.awt.Color.WHITE);
                java.awt.Color cHeur = heureColorMap.getOrDefault(s.getHeureDebut(), java.awt.Color.WHITE);
         
                table.addCell(mkCell.apply(java.awt.Color.WHITE).apply(String.valueOf(idRow), false));
                table.addCell(mkCell.apply(cEnc).apply(kE,  true));
                table.addCell(mkCell.apply(cJ1) .apply(k1,  false));
                table.addCell(mkCell.apply(cJ2) .apply(k2,  false));
                table.addCell(mkCell.apply(cDate).apply(dateStr, false));
                table.addCell(mkCell.apply(cHeur).apply(s.getHeureDebut(), false));
                table.addCell(mkCell.apply(java.awt.Color.WHITE).apply(s.getSalle().getNom(), false));
                table.addCell(mkCell.apply(cFil).apply(s.getEtudiant().getNom(), false));
                table.addCell(mkCell.apply(cFil).apply(s.getEtudiant().getPrenom(), false));
                table.addCell(mkCell.apply(cFil).apply(fil, true));
                idRow++;
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
                if (!repartition.containsKey(prof)) {
                    repartition.put(prof, new ArrayList<Etudiant>());
                }
                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.xlsx");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Répartition Encadrants");

            org.apache.poi.ss.usermodel.Font baseFont = workbook.createFont();
            baseFont.setFontName("Calibri");
            baseFont.setFontHeightInPoints((short) 10);

            CellStyle styleGI = createColorStyle(workbook, (byte)180, (byte)198, (byte)231, baseFont);
            CellStyle styleID = createColorStyle(workbook, (byte)183, (byte)225, (byte)205, baseFont);
            CellStyle styleTDIA = createColorStyle(workbook, (byte)244, (byte)177, (byte)131, baseFont);
            CellStyle styleWhite = createColorStyle(workbook, (byte)255, (byte)255, (byte)255, baseFont);
            CellStyle styleGray = createColorStyle(workbook, (byte)245, (byte)245, (byte)245, baseFont);

            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setFontName("Calibri");
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 12);

            CellStyle centerBold = workbook.createCellStyle();
            centerBold.setFont(boldFont);
            centerBold.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));
            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("Ecole Nationale des Sciences Appliquées - Al Hoceima");
            c0.setCellStyle(centerBold);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 9));
            Row r1 = sheet.createRow(1);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("Département Mathématiques et Informatique");
            c1.setCellStyle(centerBold);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, 9));
            Row r2 = sheet.createRow(2);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("Affectation des encadrants de Projet de Fin d'Etude");
            c2.setCellStyle(centerBold);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 3, 0, 9));
            Row r3 = sheet.createRow(3);
            Cell c3 = r3.createCell(0);
            c3.setCellValue("Année Universitaire 2025/2026");
            c3.setCellStyle(centerBold);

            Row l1 = sheet.createRow(5);
            l1.createCell(4).setCellValue("");
            l1.getCell(4).setCellStyle(styleTDIA);
            l1.createCell(5).setCellValue("Filière TDIA");

            Row l2 = sheet.createRow(6);
            l2.createCell(4).setCellValue("");
            l2.getCell(4).setCellStyle(styleID);
            l2.createCell(5).setCellValue("Filière ID");

            Row l3 = sheet.createRow(7);
            l3.createCell(4).setCellValue("");
            l3.getCell(4).setCellStyle(styleGI);
            l3.createCell(5).setCellValue("Filière GI");

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());

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

            Row headerRow = sheet.createRow(9);
            String[] cols = {
                "Encadrant Nom", "Encadrant Prénom",
                "Etudiant 1 Nom", "Etudiant 1 Prénom",
                "Etudiant 2 Nom", "Etudiant 2 Prénom",
                "Etudiant 3 Nom", "Etudiant 3 Prénom",
                "Etudiant 4 Nom", "Etudiant 4 Prénom"
            };

            for (int i = 0; i < cols.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            org.apache.poi.ss.usermodel.Font encFont = workbook.createFont();
            encFont.setFontName("Calibri");
            encFont.setBold(true);
            CellStyle styleEnc = createColorStyle(workbook, (byte)189, (byte)215, (byte)238, encFont);

            int rowIdx = 10;

            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {
                Row row = sheet.createRow(rowIdx);
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

                        CellStyle cs =
                            "GI".equalsIgnoreCase(e.getFiliere()) ? styleGI :
                            "ID".equalsIgnoreCase(e.getFiliere()) ? styleID :
                            "TDIA".equalsIgnoreCase(e.getFiliere()) ? styleTDIA :
                            rowBg;

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

            java.awt.Color colorGI = new java.awt.Color(180, 198, 231);
            java.awt.Color colorID = new java.awt.Color(183, 225, 205);
            java.awt.Color colorTDIA = new java.awt.Color(244, 177, 131);
            java.awt.Color colorEnc = new java.awt.Color(189, 215, 238);
            java.awt.Color colorRow = new java.awt.Color(245, 245, 245);

            PdfPTable headerBox = new PdfPTable(1);
            headerBox.setWidthPercentage(70);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setPadding(8);

            com.lowagie.text.Font headerBold =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);

            com.lowagie.text.Font headerNormal =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10);

            Paragraph h = new Paragraph();
            h.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            h.add(new Phrase("Ecole Nationale des Sciences Appliquées - Al Hoceima\n", headerBold));
            h.add(new Phrase("Département Mathématiques et Informatique\n", headerNormal));
            h.add(new Phrase("Affectation des encadrants de Projet de Fin d'Etude\n", headerNormal));
            h.add(new Phrase("Année Universitaire 2025/2026", headerNormal));

            headerCell.addElement(h);
            headerBox.addCell(headerCell);
            document.add(headerBox);
            document.add(new Paragraph(" "));

            PdfPTable legend = new PdfPTable(2);
            legend.setWidthPercentage(25);
            legend.setWidths(new float[]{40, 60});

            legend.addCell(getLegendPdfCell("", colorTDIA));
            legend.addCell(getLegendPdfCell("Filière TDIA", java.awt.Color.WHITE));

            legend.addCell(getLegendPdfCell("", colorID));
            legend.addCell(getLegendPdfCell("Filière ID", java.awt.Color.WHITE));

            legend.addCell(getLegendPdfCell("", colorGI));
            legend.addCell(getLegendPdfCell("Filière GI", java.awt.Color.WHITE));

            document.add(legend);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f});

            java.awt.Color headerColor = new java.awt.Color(31, 111, 191);

            com.lowagie.text.Font headerFont =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD, java.awt.Color.WHITE);

            String[] headers = {
                "Encadrant Nom", "Encadrant Prénom",
                "Etudiant 1 Nom", "Etudiant 1 Prénom",
                "Etudiant 2 Nom", "Etudiant 2 Prénom",
                "Etudiant 3 Nom", "Etudiant 3 Prénom",
                "Etudiant 4 Nom", "Etudiant 4 Prénom"
            };

            for (String txt : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(txt, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            com.lowagie.text.Font cellFont =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8);

            com.lowagie.text.Font boldFont =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD);

            int rowNum = 0;

            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {

                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();

                java.awt.Color bg = (rowNum % 2 == 0) ? java.awt.Color.WHITE : colorRow;

                PdfPCell cNom = new PdfPCell(new Phrase(prof.getNom(), boldFont));
                cNom.setBackgroundColor(colorEnc);
                cNom.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                cNom.setPadding(4);
                table.addCell(cNom);

                PdfPCell cPrenom = new PdfPCell(new Phrase(prof.getPrenom(), boldFont));
                cPrenom.setBackgroundColor(colorEnc);
                cPrenom.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                cPrenom.setPadding(4);
                table.addCell(cPrenom);

                for (int i = 0; i < 4; i++) {
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);

                        java.awt.Color fc =
                            "GI".equalsIgnoreCase(e.getFiliere()) ? colorGI :
                            "ID".equalsIgnoreCase(e.getFiliere()) ? colorID :
                            "TDIA".equalsIgnoreCase(e.getFiliere()) ? colorTDIA :
                            bg;

                        PdfPCell cn = new PdfPCell(new Phrase(e.getNom(), cellFont));
                        cn.setBackgroundColor(fc);
                        cn.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                        cn.setPadding(4);
                        table.addCell(cn);

                        PdfPCell cp = new PdfPCell(new Phrase(e.getPrenom(), cellFont));
                        cp.setBackgroundColor(fc);
                        cp.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                        cp.setPadding(4);
                        table.addCell(cp);
                    } else {
                        PdfPCell empty1 = new PdfPCell(new Phrase(""));
                        empty1.setBackgroundColor(bg);
                        table.addCell(empty1);

                        PdfPCell empty2 = new PdfPCell(new Phrase(""));
                        empty2.setBackgroundColor(bg);
                        table.addCell(empty2);
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
                if (!repartition.containsKey(prof)) {
                    repartition.put(prof, new ArrayList<Etudiant>());
                }
                repartition.get(prof).addAll(g.getEtudiants());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=repartition_encadrants.docx");

            XWPFDocument document = new XWPFDocument();

            XWPFTable headerTable = document.createTable(1, 1);
            headerTable.setWidth("70%");

            XWPFTableCell headerCell = headerTable.getRow(0).getCell(0);
            headerCell.setColor("FFFFFF");

            XWPFParagraph hp = headerCell.getParagraphs().get(0);
            hp.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);

            XWPFRun r1 = hp.createRun();
            r1.setText("Ecole Nationale des Sciences Appliquées - Al Hoceima");
            r1.setBold(true);
            r1.setFontSize(12);
            r1.setFontFamily("Calibri");
            r1.addBreak();

            XWPFRun r2 = hp.createRun();
            r2.setText("Département Mathématiques et Informatique");
            r2.setFontSize(11);
            r2.setFontFamily("Calibri");
            r2.addBreak();

            XWPFRun r3 = hp.createRun();
            r3.setText("Affectation des encadrants de Projet de Fin d'Etude");
            r3.setFontSize(10);
            r3.setFontFamily("Calibri");
            r3.addBreak();

            XWPFRun r4 = hp.createRun();
            r4.setText("Année Universitaire 2025/2026");
            r4.setFontSize(10);
            r4.setFontFamily("Calibri");

            document.createParagraph();

            XWPFTable legendTable = document.createTable(3, 2);
            legendTable.setWidth("25%");

            styleLegendCell(legendTable.getRow(0).getCell(0), "", "F4B183");
            styleLegendCell(legendTable.getRow(0).getCell(1), "Filière TDIA", "FFFFFF");

            styleLegendCell(legendTable.getRow(1).getCell(0), "", "B7E1CD");
            styleLegendCell(legendTable.getRow(1).getCell(1), "Filière ID", "FFFFFF");

            styleLegendCell(legendTable.getRow(2).getCell(0), "", "B4C6E7");
            styleLegendCell(legendTable.getRow(2).getCell(1), "Filière GI", "FFFFFF");

            document.createParagraph();

            XWPFTable table = document.createTable();

            XWPFTableRow hRow = table.getRow(0);

            String[] hCols = {
                "Encadrant Nom", "Encadrant Prénom",
                "Etudiant 1 Nom", "Etudiant 1 Prénom",
                "Etudiant 2 Nom", "Etudiant 2 Prénom",
                "Etudiant 3 Nom", "Etudiant 3 Prénom",
                "Etudiant 4 Nom", "Etudiant 4 Prénom"
            };

            styleHeaderCell(hRow.getCell(0), hCols[0]);

            for (int i = 1; i < hCols.length; i++) {
                styleHeaderCell(hRow.addNewTableCell(), hCols[i]);
            }

            int rowNum = 0;

            for (Map.Entry<Professeur, List<Etudiant>> entry : repartition.entrySet()) {
                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();

                XWPFTableRow row = table.createRow();

                String bgAlt = (rowNum % 2 == 0) ? "FFFFFF" : "F5F5F5";

                styleDataCell(row.getCell(0), prof.getNom(), "BDD7EE", true);
                styleDataCell(row.getCell(1), prof.getPrenom(), "BDD7EE", true);

                int col = 2;

                for (int i = 0; i < 4; i++) {
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);

                        String fc =
                            "GI".equalsIgnoreCase(e.getFiliere()) ? "B4C6E7" :
                            "ID".equalsIgnoreCase(e.getFiliere()) ? "B7E1CD" :
                            "TDIA".equalsIgnoreCase(e.getFiliere()) ? "F4B183" :
                            bgAlt;

                        styleDataCell(row.getCell(col), e.getNom(), fc, false);
                        styleDataCell(row.getCell(col + 1), e.getPrenom(), fc, false);
                    } else {
                        styleDataCell(row.getCell(col), "", bgAlt, false);
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

            // Compter le total d'étudiants à planifier
            int nbEtudiants = 0;
            for (GroupPfe g : groupes) {
                nbEtudiants += g.getEtudiants().size();
            }

            Part filePart = request.getPart("fichierExcel");

            // Stocker les bytes du fichier pour pouvoir le lire deux fois
            byte[] excelBytes = filePart.getInputStream().readAllBytes();

            // ── PHASE 1 : lecture pour vérification (sans toucher la base) ──────
            List<String[]> creneauxLus   = new ArrayList<>();
            List<String[]> sallesLues    = new ArrayList<>();
            List<String[]> profsLus      = new ArrayList<>();

            try (Workbook workbook = WorkbookFactory.create(
                    new java.io.ByteArrayInputStream(excelBytes))) {

                // Lire salles
                Sheet sheetSalles = workbook.getSheet("Salle");
                if (sheetSalles != null) {
                    for (int i = 1; i <= sheetSalles.getLastRowNum(); i++) {
                        Row row = sheetSalles.getRow(i);
                        if (row != null && row.getCell(0) != null) {
                            String nom = row.getCell(0).getStringCellValue().trim();
                            if (!nom.isEmpty()) {
                                int cap = (int) row.getCell(1).getNumericCellValue();
                                sallesLues.add(new String[]{nom, String.valueOf(cap)});
                            }
                        }
                    }
                }

                // Lire créneaux (Config_Creneaux ou Creneau)
                Sheet sheetConfig = workbook.getSheet("Config_Creneaux");
                if (sheetConfig != null) {
                    String matinDeb = sheetConfig.getRow(4).getCell(2).getStringCellValue().trim();
                    String matinFin = sheetConfig.getRow(5).getCell(2).getStringCellValue().trim();
                    String apmDeb   = sheetConfig.getRow(6).getCell(2).getStringCellValue().trim();
                    String apmFin   = sheetConfig.getRow(7).getCell(2).getStringCellValue().trim();
                    List<String[]> matinSlots = genererSlots(matinDeb, matinFin);
                    List<String[]> apmSlots   = genererSlots(apmDeb,   apmFin);
                    for (int i = 11; i <= sheetConfig.getLastRowNum(); i++) {
                        Row row = sheetConfig.getRow(i);
                        if (row == null || row.getCell(2) == null) continue;
                        String dateStr = row.getCell(2).getStringCellValue().trim();
                        if (dateStr.isEmpty()) continue;
                        String[] parts = dateStr.split("/");
                        if (parts.length == 3) {
                            String dateISO = parts[2] + "-" + parts[1] + "-" + parts[0];
                            for (String[] s : matinSlots) creneauxLus.add(new String[]{dateISO, s[0], s[1]});
                            for (String[] s : apmSlots)   creneauxLus.add(new String[]{dateISO, s[0], s[1]});
                        }
                    }
                } else {
                    Sheet sheetCreneaux = workbook.getSheet("Creneau");
                    if (sheetCreneaux != null) {
                        for (int i = 2; i <= sheetCreneaux.getLastRowNum(); i++) {
                            Row row = sheetCreneaux.getRow(i);
                            if (row == null || row.getCell(0) == null) continue;
                            String date = row.getCell(0).getStringCellValue().trim();
                            String hdeb = row.getCell(1).getStringCellValue().trim();
                            String hfin = row.getCell(2).getStringCellValue().trim();
                            if (!date.isEmpty()) creneauxLus.add(new String[]{date, hdeb, hfin});
                        }
                    }
                }

                // Lire professeurs depuis la feuille Excel
                Sheet sheetProfs = workbook.getSheet("Professeur");
                if (sheetProfs != null) {
                    for (int i = 1; i <= sheetProfs.getLastRowNum(); i++) {
                        Row row = sheetProfs.getRow(i);
                        if (row != null && row.getCell(0) != null) {
                            String nom  = row.getCell(0).getStringCellValue().trim();
                            String prenom = row.getCell(1).getStringCellValue().trim();
                            String spec = row.getCell(2).getStringCellValue().trim();
                            if (!nom.isEmpty()) profsLus.add(new String[]{nom, prenom, spec});
                        }
                    }
                }
                // Fallback : lire les profs depuis la base de données
                if (profsLus.isEmpty()) {
                    ProfesseurDao professeurDao = new ProfesseurDaoImpl();
                    for (ma.ensah.soutenance.model.entity.Professeur p : professeurDao.findAll()) {
                        profsLus.add(new String[]{p.getNom(), p.getPrenom(),
                                                   p.getSpecialite() == null ? "" : p.getSpecialite()});
                    }
                }
            }

            // ── PHASE 2 : vérification des contraintes ───────────────────────
            VerificationContraintes verificateur = new VerificationContraintes();
            List<VerificationContraintes.AlerteContrainte> alertes =
                verificateur.verifier(creneauxLus, sallesLues, profsLus, nbEtudiants);

            boolean confirmerForce = "true".equals(request.getParameter("confirmerMalgre"));

            if (!alertes.isEmpty() && !confirmerForce) {
                // Des alertes existent et l'utilisateur n'a pas encore confirmé
                // → renvoyer vers la JSP avec les alertes pour afficher la modale
                request.setAttribute("alertesContraintes", alertes);
                request.setAttribute("aDesBloquants", VerificationContraintes.aDesBloquants(alertes));

                // Encoder les données pour les transmettre au formulaire de confirmation
                // (on ne retransmet pas le fichier, on demande de re-soumettre)
                request.getRequestDispatcher("/WEB-INF/views/importerPlanning.jsp")
                       .forward(request, response);
                return;
            }

            // Si des alertes BLOQUANTES existent même après confirmation → bloquer
            if (VerificationContraintes.aDesBloquants(alertes)) {
                request.setAttribute("alertesContraintes", alertes);
                request.setAttribute("aDesBloquants", true);
                request.getRequestDispatcher("/WEB-INF/views/importerPlanning.jsp")
                       .forward(request, response);
                return;
            }

            // ── PHASE 3 : génération effective ──────────────────────────────
            try (Workbook workbook = WorkbookFactory.create(
                    new java.io.ByteArrayInputStream(excelBytes))) {

                SoutenanceDao soutenanceDao = new SoutenanceDaoImpl();
                soutenanceDao.deleteAll();
                SalleDao salleDao = new SalleDaoImpl();
                salleDao.deleteAll();

                // Sauvegarder salles
                Sheet sheetSalles = workbook.getSheet("Salle");
                if (sheetSalles != null) {
                    for (int i = 1; i <= sheetSalles.getLastRowNum(); i++) {
                        Row row = sheetSalles.getRow(i);
                        if (row != null && row.getCell(0) != null) {
                            String nom = row.getCell(0).getStringCellValue().trim();
                            int cap    = (int) row.getCell(1).getNumericCellValue();
                            if (!nom.isEmpty()) salleDao.save(new Salle(nom, cap));
                        }
                    }
                }

                PlanningService planningService = new PlanningServiceImpl();
                planningService.genererPlanning(creneauxLus, new ArrayList<>());
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
 // ── Méthode helper : génération de créneaux horaires ─
    private List<String[]> genererSlots(String heureDebut, String heureFin) {
        List<String[]> slots = new ArrayList<>();
        try {
            String[] pd = heureDebut.split(":");
            String[] pf = heureFin.split(":");
            int minDeb = Integer.parseInt(pd[0]) * 60 + Integer.parseInt(pd[1]);
            int minFin = Integer.parseInt(pf[0]) * 60 + Integer.parseInt(pf[1]);
            int cursor = minDeb;
            while (cursor + 60 <= minFin) {
                String debut = String.format("%02d:%02d", cursor / 60, cursor % 60);
                String fin   = String.format("%02d:%02d", (cursor + 60) / 60, (cursor + 60) % 60);
                slots.add(new String[]{debut, fin});
                cursor += 60;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slots;
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
    private PdfPCell getLegendPdfCell(String text, java.awt.Color color) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBackgroundColor(color);
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private void styleLegendCell(XWPFTableCell cell, String text, String bgColor) {
        cell.setColor(bgColor);

        XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT);

        XWPFRun run = p.createRun();
        run.setText(text);
        run.setFontSize(9);
        run.setFontFamily("Calibri");
    }  
}


