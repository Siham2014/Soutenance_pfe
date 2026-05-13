package ma.ensah.soutenance.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import ma.ensah.soutenance.model.entity.*;

public class genererPv {

    public void genererPv(HttpServletResponse response, Soutenance s) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=fiche_evaluation_pfe.pdf");

        try {
            Document document = new Document(PageSize.A4, 50, 50, 35, 35);
            PdfWriter.getInstance(document, response.getOutputStream());

            document.open();

            Font normal = new Font(Font.FontFamily.TIMES_ROMAN, 12);
            Font bold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
            Font title = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);

            Etudiant etudiant = s.getEtudiant();
            Professeur encadrant = s.getEncadrant();
            Professeur membreInfo = s.getMembreInfo();
            Professeur membreMath = s.getMembreMath();

            Paragraph p;

            p = new Paragraph("UNIVERSITE ABDELMALEK ESSAADI", bold);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            p = new Paragraph("Ecole Nationale des Sciences Appliquées d’Al-Hoceima - Maroc", normal);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.add(new Paragraph(" "));

            p = new Paragraph("Département de Mathématiques et Informatique", normal);
            p.setAlignment(Element.ALIGN_LEFT);
            document.add(p);

            p = new Paragraph("Fiche d’évaluation du Projet de Fin d’Étude", title);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            p = new Paragraph("Année Universitaire : 2023-2024", bold);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Nom - Prénom de l’élève ingénieur :", bold));
            document.add(new Paragraph("• " + etudiant.getNom() + " " + etudiant.getPrenom(), normal));

            document.add(new Paragraph("Filière : " + etudiant.getFiliere(), normal));

            document.add(new Paragraph("Intitulé du rapport :", bold));
            document.add(new Paragraph("• .................................................................", normal));

            document.add(new Paragraph("L'encadrant(e) interne :", bold));
            document.add(new Paragraph("• Pr. " + encadrant.getNom() + " " + encadrant.getPrenom(), normal));

            document.add(new Paragraph("Membres du jury :", bold));

            document.add(juryLine("Pr. " + membreInfo.getNom() + " " + membreInfo.getPrenom(), "Président"));
            document.add(juryLine("Pr. " + membreMath.getNom() + " " + membreMath.getPrenom(), "Rapporteur"));
            document.add(juryLine("Pr. " + encadrant.getNom() + " " + encadrant.getPrenom(), "Rapporteur"));

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Note du Contenu (En prenant en compte l’appréciation de l’entreprise)", bold));
            document.add(new Paragraph("C = ", normal));

            document.add(new Paragraph("\nNote du Mémoire", bold));
            document.add(new Paragraph("M = ", normal));

            document.add(new Paragraph("\nNote de la Soutenance", bold));
            document.add(new Paragraph("S = ", normal));

            document.add(new Paragraph("\n"));

            p = new Paragraph("MOYENNE", bold);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            p = new Paragraph("Moyenne = C * 0,5 + M * 0,2 + S * 0,3 = ", normal);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.add(new Paragraph("\n\n"));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            document.add(new Paragraph("Le : " + sdf.format(s.getDateSoutenance()), normal));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Signature des membres du jury :", bold));
            document.add(new Paragraph("\n\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);

            table.addCell(getSignatureCell("Pr. " + membreInfo.getNom()));
            table.addCell(getSignatureCell("Pr. " + membreMath.getNom()));
            table.addCell(getSignatureCell("Pr. " + encadrant.getNom()));

            document.add(table);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PdfPTable juryLine(String nomProf, String role) throws DocumentException {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{70, 30});

        PdfPCell cell1 = new PdfPCell(new Phrase("• " + nomProf));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setPaddingLeft(25);

        PdfPCell cell2 = new PdfPCell(new Phrase(role));
        cell2.setBorder(Rectangle.NO_BORDER);

        table.addCell(cell1);
        table.addCell(cell2);

        return table;
    }

    private PdfPCell getSignatureCell(String text) {

        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(30);

        return cell;
    }
}