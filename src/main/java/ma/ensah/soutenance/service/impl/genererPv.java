package ma.ensah.soutenance.service.impl;

import java.io.File;
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

            BaseFont bf = BaseFont.createFont(
                    BaseFont.TIMES_ROMAN,
                    BaseFont.CP1252,
                    BaseFont.NOT_EMBEDDED
            );

            Font normal = new Font(bf, 12, Font.NORMAL);
            Font bold = new Font(bf, 12, Font.BOLD);
            Font title = new Font(bf, 14, Font.BOLD);

            Etudiant etudiant = s.getEtudiant();
            Professeur encadrant = s.getEncadrant();
            Professeur membreInfo = s.getMembreInfo();
            Professeur membreMath = s.getMembreMath();

            ajouterEnteteAvecLogos(document);

            document.add(new Paragraph(" "));

            Paragraph p;

            p = new Paragraph(
            	    "Département de Mathématiques et Informatique",
            	    title
            	);

            	p.setAlignment(Element.ALIGN_CENTER);

            	document.add(p);

     
            p = new Paragraph("Fiche d’évaluation du Projet de Fin d’Étude", title);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            p = new Paragraph("Année Universitaire : 2025-2026", bold);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Nom - Prénom de l’élève ingénieur :", bold));
            document.add(new Paragraph("• " + etudiant.getNom() + " " + etudiant.getPrenom(), normal));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Filière : " + etudiant.getFiliere(), bold));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Intitulé du rapport :", bold));
            document.add(new Paragraph("• .................................................................", normal));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("L'encadrant(e) interne :", bold));
            document.add(new Paragraph("• Pr. " + encadrant.getNom() + " " + encadrant.getPrenom(), normal));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Membres du jury :", bold));

            document.add(juryLine(
                    "Pr. " + encadrant.getNom() + " " + encadrant.getPrenom(),
                    "Président",
                    bf
            ));

            document.add(new Paragraph("\n"));

            document.add(juryLine(
                    "Pr. " + membreInfo.getNom() + " " + membreInfo.getPrenom(),
                    "Rapporteur",
                    bf
            ));

            document.add(new Paragraph("\n"));

            document.add(juryLine(
                    "Pr. " + membreMath.getNom() + " " + membreMath.getPrenom(),
                    "Rapporteur",
                    bf
            ));

            

            Paragraph p1 = new Paragraph();
            p1.add(new Chunk("Note du Contenu ", bold));
            p1.add(new Chunk("(En prenant en compte l’appréciation de l’entreprise)", normal));
            document.add(p1);

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

           

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            document.add(new Paragraph("Le : " + sdf.format(s.getDateSoutenance()), normal));

           
            document.add(new Paragraph("Signature des membres du jury :", bold));
    
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);

            table.addCell(getSignatureCell("Pr. " + encadrant.getNom(), bf));
            table.addCell(getSignatureCell("Pr. " + membreInfo.getNom(), bf));
            table.addCell(getSignatureCell("Pr. " + membreMath.getNom(), bf));

            document.add(table);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajouterEnteteAvecLogos(Document document) {

        try {
            File webInfClasses = new File(
                    genererPv.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            File webInf = webInfClasses.getParentFile();
            File webRoot = webInf.getParentFile();

            String logoEnsahPath = new File(webRoot, "static/images/ENSAH.png").getAbsolutePath();
            String logoUaePath = new File(webRoot, "static/images/UAE.png").getAbsolutePath();

            Image logoUae = Image.getInstance(logoUaePath);
            Image logoEnsah = Image.getInstance(logoEnsahPath);

            logoUae.scaleToFit(55, 55);
            logoEnsah.scaleToFit(55, 55);

            BaseFont bf = BaseFont.createFont(
                    BaseFont.TIMES_ROMAN,
                    BaseFont.CP1252,
                    BaseFont.NOT_EMBEDDED
            );

            Font bold = new Font(bf, 11, Font.BOLD);
            Font normal = new Font(bf, 10, Font.NORMAL);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{20, 60, 20});

            PdfPCell cellLogoUae = new PdfPCell(logoUae);
            cellLogoUae.setBorder(Rectangle.NO_BORDER);
            cellLogoUae.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellLogoUae.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph center = new Paragraph();
            center.setAlignment(Element.ALIGN_CENTER);
            center.add(new Phrase("UNIVERSITE ABDELMALEK ESSAADI\n", bold));
            center.add(new Phrase("Ecole Nationale des Sciences Appliquées d’Al-Hoceima - Maroc\n", normal));
           

            PdfPCell cellText = new PdfPCell(center);
            cellText.setBorder(Rectangle.NO_BORDER);
            cellText.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellText.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell cellLogoEnsah = new PdfPCell(logoEnsah);
            cellLogoEnsah.setBorder(Rectangle.NO_BORDER);
            cellLogoEnsah.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellLogoEnsah.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cellLogoUae);
            table.addCell(cellText);
            table.addCell(cellLogoEnsah);

            document.add(table);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PdfPTable juryLine(String nomProf, String role, BaseFont bf) throws DocumentException {

        Font normal = new Font(bf, 12, Font.NORMAL);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{70, 30});

        PdfPCell cell1 = new PdfPCell(new Phrase("• " + nomProf, normal));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setPaddingLeft(25);

        PdfPCell cell2 = new PdfPCell(new Phrase(role, normal));
        cell2.setBorder(Rectangle.NO_BORDER);

        table.addCell(cell1);
        table.addCell(cell2);

        return table;
    }

    private PdfPCell getSignatureCell(String text, BaseFont bf) {

        Font normal = new Font(bf, 12, Font.NORMAL);

        PdfPCell cell = new PdfPCell(new Phrase(text, normal));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(30);

        return cell;
    }
}