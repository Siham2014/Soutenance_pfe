package ma.ensah.soutenance.service.impl;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import ma.ensah.soutenance.model.entity.*;

public class genererPv {

	
	

	public void genererPv(HttpServletResponse response, GroupPfe groupe) throws IOException {

	    response.setContentType("application/pdf");
	    response.setHeader("Content-Disposition", "attachment; filename=pv_soutenance.pdf");

	    try {
	        Document document = new Document(PageSize.A4, 50, 50, 40, 40);
	        PdfWriter.getInstance(document, response.getOutputStream());

	        document.open();

	        Font normal = new Font(Font.FontFamily.TIMES_ROMAN, 12);
	        Font bold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	        Font title = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);

	        Paragraph header = new Paragraph();
	        header.setAlignment(Element.ALIGN_CENTER);
	        header.add(new Phrase("Université Abdelmalek Essaadi\n", bold));
	        header.add(new Phrase("Ecole National des sciences appliquée\n", bold));
	        header.add(new Phrase("Département Informatique\n\n", bold));
	        document.add(header);

	        Paragraph titre = new Paragraph("PROCÈS-VERBAL DE SOUTENANCE", title);
	        titre.setAlignment(Element.ALIGN_CENTER);
	        titre.setSpacingAfter(20);
	        document.add(titre);

	        Professeur encadrant = groupe.getEncadrant();

	        document.add(new Paragraph("Encadrant :", bold));
	        document.add(new Paragraph(
	            "Nom : " + encadrant.getNom() +
	            "        Prénom : " + encadrant.getPrenom(),
	            normal
	        ));

	        document.add(new Paragraph("\nÉtudiant(s) :", bold));

	        for (Etudiant e : groupe.getEtudiants()) {
	            document.add(new Paragraph(
	                "Nom : " + e.getNom() +
	                "        Prénom : " + e.getPrenom() +
	                "        Filière : " + e.getFiliere(),
	                normal
	            ));
	        }

	        document.add(new Paragraph("\nProjet :", bold));
	        document.add(new Paragraph("Titre du PFE : ............................................................", normal));

	        document.add(new Paragraph("\nSoutenance :", bold));
	        document.add(new Paragraph("Date : .........................        Heure : .........................", normal));
	        document.add(new Paragraph("Salle : .................................................................", normal));

	        document.add(new Paragraph("\nJury :", bold));
	        document.add(new Paragraph("Jury 1 : .............................................................", normal));
	        document.add(new Paragraph("Jury 2: .............................................................", normal));
	        document.add(new Paragraph("Jury 3: .............................................................", normal));


	        document.add(new Paragraph("Encadrant : " + encadrant.getNom() + " " + encadrant.getPrenom(), normal));

	        document.add(new Paragraph("\nRésultat :", bold));
	        document.add(new Paragraph("Note : ......................... /20", normal));
	        document.add(new Paragraph("Mention : ...............................................................", normal));

	        document.add(new Paragraph("\nObservations :", bold));
	        document.add(new Paragraph("..........................................................................", normal));
	        document.add(new Paragraph("..........................................................................", normal));
	        document.add(new Paragraph("..........................................................................", normal));

	        document.add(new Paragraph("\n\nSignatures :", bold));

	        PdfPTable table = new PdfPTable(4);
	        table.setWidthPercentage(100);
	        table.addCell(getSignatureCell("Jury 1"));
	        table.addCell(getSignatureCell("Jury 2"));
	        table.addCell(getSignatureCell("Jury 3"));
	        table.addCell(getSignatureCell("Encadrant"));

	        document.add(table);

	        document.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	private PdfPCell getSignatureCell(String text) {
	    PdfPCell cell = new PdfPCell(new Phrase(text));
	    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    cell.setBorder(Rectangle.NO_BORDER);
	    cell.setPaddingTop(30);
	    return cell;
	}
}
