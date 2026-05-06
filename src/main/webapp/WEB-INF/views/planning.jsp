<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Planning des Soutenances</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
    <style>
        .header { background-color: #1F6FBF; color: white; }
        th { text-align: center; }
        td { text-align: center; vertical-align: middle; }
    </style>
</head>
<body>
<div class="container-fluid p-4">
    <h2>📅 Planning des Soutenances PFE</h2>
    <a href="<%=request.getContextPath()%>/app" class="btn btn-secondary mb-3">
        ← Retour
    </a>

    <table class="table table-bordered table-hover">
        <thead>
            <tr class="header">
                <th>Date</th>
                <th>Heure Début</th>
                <th>Heure Fin</th>
                <th>Étudiant</th>
                <th>Filière</th>
                <th>Encadrant</th>
                <th>Encadrant</th>
<th>Jury Info</th>
<th>Jury Math</th>
<th>Salle</th>


                <th>Salle</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="s" items="${planning}">
                <tr>
                    <td><fmt:formatDate value="${s.dateSoutenance}" pattern="dd/MM/yyyy"/></td>
                    <td>${s.heureDebut}</td>
                    <td>${s.heureFin}</td>
                    <td>${s.etudiant.nom} ${s.etudiant.prenom}</td>
                    <td>${s.etudiant.filiere}</td>
                    <td>${s.encadrant.nom} ${s.encadrant.prenom}</td>
                    <!-- Dans le forEach -->
<td>${s.encadrant.nom} ${s.encadrant.prenom}</td>
<td>${s.membreInfo.nom} ${s.membreInfo.prenom}</td>
<td>${s.membreMath.nom} ${s.membreMath.prenom}</td>
<td>${s.salle.nom}</td>
                    <td>${s.salle.nom}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <c:if test="${empty planning}">
        <div class="alert alert-warning">
            Aucune soutenance planifiée. Veuillez importer un fichier Excel.
        </div>
    </c:if>
</div>
</body>
</html>