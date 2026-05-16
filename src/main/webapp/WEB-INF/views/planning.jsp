<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Planning des Soutenances</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
    <style>
        .header-table { background-color: #1F6FBF; color: white; }
        th { text-align: center; vertical-align: middle !important; }
        td { text-align: center; vertical-align: middle !important; }
        .badge-enc  { background-color: #28a745; color: white; padding: 3px 8px; border-radius: 4px; font-size:12px; }
        .badge-info { background-color: #17a2b8; color: white; padding: 3px 8px; border-radius: 4px; font-size:12px; }
        .badge-math { background-color: #6f42c1; color: white; padding: 3px 8px; border-radius: 4px; font-size:12px; }
        .filiere-TDIA { background-color: #F4B183; }
        .filiere-GI   { background-color: #B4C6E7; }
        .filiere-ID   { background-color: #B7E1CD; }
        .btn-export {
            background-color: #217346;
            color: white;
            border: none;
        }
        .btn-export:hover {
            background-color: #1a5c38;
            color: white;
        }
    </style>
</head>
<body>
<div class="container-fluid p-4">

    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2>📅 Planning des Soutenances PFE</h2>
        <div>
            <c:if test="${not empty planning}">
                <a href="<%=request.getContextPath()%>/app?action=exportPlanning"
                   class="btn btn-export me-2">
                    📥 Exporter Excel
                </a>
            </c:if>
            <a href="<%=request.getContextPath()%>/app?action=voirRepartition"
               class="btn btn-outline-primary me-2">
                👥 Voir Répartition
            </a>
            <a href="<%=request.getContextPath()%>/app" class="btn btn-secondary">
                ← Retour Accueil
            </a>
        <a class="btn btn-primary"
   href="${pageContext.request.contextPath}/app?action=listePv">

    Générer PV
</a>
        </div>
    </div>

    <c:if test="${not empty planning}">
        <p class="text-muted">
            Total : <strong>${planning.size()}</strong> soutenance(s) planifiée(s)
        </p>
    </c:if>
 
 
    <div class="table-responsive">
    <table class="table table-bordered table-hover table-sm">
        <thead>
            <tr class="header-table">
                <th>Date</th>
                <th>Heure Début</th>
                <th>Heure Fin</th>
                <th>Étudiant</th>
                <th>Filière</th>
                <th>Salle</th>
                <th>Encadrant (Jury)</th>
                <th>Jury Informatique</th>
                <th>Jury Mathématiques</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="s" items="${planning}">
                <tr>
                    <td><fmt:formatDate value="${s.dateSoutenance}" pattern="dd/MM/yyyy"/></td>
                    <td><strong>${s.heureDebut}</strong></td>
                    <td>${s.heureFin}</td>
                    <td class="filiere-${s.etudiant.filiere}">
                        <strong>${s.etudiant.nom}</strong> ${s.etudiant.prenom}
                    </td>
                    <td class="filiere-${s.etudiant.filiere}">
                        <strong>${s.etudiant.filiere}</strong>
                    </td>
                    <td>${s.salle.nom}</td>
                    <td><span class="badge-enc">${s.encadrant.nom} ${s.encadrant.prenom}</span></td>
                    <td><span class="badge-info">${s.membreInfo.nom} ${s.membreInfo.prenom}</span></td>
                    <td><span class="badge-math">${s.membreMath.nom} ${s.membreMath.prenom}</span></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    </div>

  <c:if test="${not empty planning}">
    
    <a href="<%=request.getContextPath()%>/app?action=exportPlanning"
       class="btn btn-export me-2">
        📥 Exporter Excel
    </a>

    <a href="<%=request.getContextPath()%>/app?action=exportPlanningPdf"
       class="btn btn-danger me-2">
        📄 Exporter PDF
    </a>

    <a href="<%=request.getContextPath()%>/app?action=exportPlanningWord"
       class="btn btn-primary me-2">
        📝 Exporter Word
    </a>

</c:if>

</div>
</body>
</html>
