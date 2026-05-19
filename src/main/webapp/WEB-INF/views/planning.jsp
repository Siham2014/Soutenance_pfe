<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Planning - Gestion PFE</title>

<link rel="stylesheet"
      href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">

<style>

body {
    background: #f5f7fb;
    font-family: 'Segoe UI', sans-serif;
}

/* NAVBAR */
.navbar {
    background: #1f2937;
}

.navbar-brand {
    font-weight: 600;
}

/* HERO */
.hero {
    background: linear-gradient(135deg, #1f2937, #374151);
    color: white;
    padding: 40px 20px;
    border-radius: 16px;
    margin-top: 25px;
    text-align: center;
}

/* ACTIONS */
.action-bar {
    margin-top: 15px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
}

/* BUTTONS */
.btn-custom {
    border-radius: 10px;
    font-weight: 500;
}

/* CARD TABLE */
.table-card {
    margin-top: 30px;
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 6px 20px rgba(0,0,0,0.05);
    overflow-x: auto;
}

/* TABLE */
.table thead {
    background: #1f2937;
    color: white;
}

.table thead th {
    text-align: center;
    vertical-align: middle;
    white-space: nowrap;
}

.table tbody td {
    text-align: center;
    vertical-align: middle;
    white-space: nowrap;
}

/* FILIÈRES */
.filiere-TDIA {
    background-color: #fff3e0;
    font-weight: 600;
    color: #e67e22;
}

.filiere-GI {
    background-color: #e3f2fd;
    font-weight: 600;
    color: #2980b9;
}

.filiere-ID {
    background-color: #e8f5e9;
    font-weight: 600;
    color: #27ae60;
}

/* BADGES */
.badge-enc {
    background-color: #111827;
    color: white;
    padding: 4px 10px;
    border-radius: 8px;
    font-size: 12px;
}

.badge-info {
    background-color: #2980b9;
    color: white;
    padding: 4px 10px;
    border-radius: 8px;
    font-size: 12px;
}

.badge-math {
    background-color: #8e44ad;
    color: white;
    padding: 4px 10px;
    border-radius: 8px;
    font-size: 12px;
}

/* TABLE HOVER */
.table tbody tr:hover {
    background: #f3f4f6;
}

</style>
</head>

<body>

<!-- NAVBAR -->
<nav class="navbar navbar-dark px-4">
    <a class="navbar-brand" href="#">
        🎓 Gestion PFE - ENSAH
    </a>
</nav>

<div class="container">

    <!-- HERO -->
    <div class="hero">

        <h2> Planning des Soutenances PFE</h2>
        <p>Organisation automatique des soutenances et jurys</p>

        <div class="action-bar">

            <c:if test="${not empty planning}">

                <a href="<%=request.getContextPath()%>/app?action=exportExcel&type=planning"
                   class="btn btn-success btn-custom">
                    Excel
                </a>

                <a href="<%=request.getContextPath()%>/app?action=exportPdf&type=planning"
                   class="btn btn-danger btn-custom">
                    PDF
                </a>

                <a href="<%=request.getContextPath()%>/app?action=exportWord&type=planning"
                   class="btn btn-primary btn-custom">
                    Word
                </a>

            </c:if>

            <a href="<%=request.getContextPath()%>/app?action=voirRepartition"
               class="btn btn-outline-light btn-custom">
                Répartition
            </a>

            <a href="<%=request.getContextPath()%>/app"
               class="btn btn-light btn-custom">
                Accueil
            </a>

            <a href="<%=request.getContextPath()%>/app?action=listePv"
               class="btn btn-warning btn-custom">
                Générer PV
            </a>

        </div>
    </div>

    <!-- TABLE -->
    <div class="table-card">

        <c:if test="${not empty planning}">
            <p class="text-muted mb-3">
                Total : <strong>${planning.size()}</strong> soutenances planifiées
            </p>
        </c:if>

        <table class="table table-bordered table-hover table-sm">

            <thead>
                <tr>
                    <th>Date</th>
                    <th>Heure Début</th>
                    <th>Heure Fin</th>
                    <th>Étudiant</th>
                    <th>Filière</th>
                    <th>Salle</th>
                    <th>Encadrant</th>
                    <th>Jury Info</th>
                    <th>Jury Math</th>
                </tr>
            </thead>

            <tbody>

            <c:forEach var="s" items="${planning}">
                <tr>

                    <td>
                        <fmt:formatDate value="${s.dateSoutenance}" pattern="dd/MM/yyyy"/>
                    </td>

                    <td><strong>${s.heureDebut}</strong></td>
                    <td>${s.heureFin}</td>

                    <td class="filiere-${s.etudiant.filiere}">
                        ${s.etudiant.nom} ${s.etudiant.prenom}
                    </td>

                    <td class="filiere-${s.etudiant.filiere}">
                        ${s.etudiant.filiere}
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

</div>

</body>
</html>