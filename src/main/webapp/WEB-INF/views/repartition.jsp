<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Répartition - Gestion PFE</title>

<link rel="stylesheet"
      href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">

<link rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

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

.hero h2 {
    font-weight: 700;
}

/* ACTION BAR */
.action-bar {
    margin-top: 20px;
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

/* TABLE CARD */
.table-card {
    margin-top: 30px;
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 6px 20px rgba(0,0,0,0.05);

    overflow-x: auto;   /* 🔥 IMPORTANT FIX */
}

/* TABLE */
.table {
    border-radius: 12px;
    overflow: hidden;
    min-width: 1100px;   /* 🔥 FORCE LARGE TABLE */
}

.table thead {
    background: #1f2937;
    color: white;
}

.table thead th {
    text-align: center;
    vertical-align: middle;
}

.table tbody td {
    text-align: center;
    vertical-align: middle;
    white-space: nowrap;  /* 🔥 FIX TEXT */
}

/* FILIÈRES */
.TDIA {
    background-color: #fff3e0 !important;
    color: #e67e22;
    font-weight: 600;
}

.ID {
    background-color: #e8f5e9 !important;
    color: #27ae60;
    font-weight: 600;
}

.GI {
    background-color: #e3f2fd !important;
    color: #2980b9;
    font-weight: 600;
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
        <h2>Répartition des Encadrants</h2>
        <p>Gestion automatique des étudiants par encadrant</p>

        <div class="action-bar">

            <a class="btn btn-primary btn-custom"
               href="${pageContext.request.contextPath}/app?action=voirPlanning">
                Voir Soutenances
            </a>

            <a class="btn btn-secondary btn-custom"
               href="${pageContext.request.contextPath}/app">
                Accueil
            </a>

            <a class="btn btn-success btn-custom"
               href="${pageContext.request.contextPath}/app?action=exportRepartitionExcel">
                Excel
            </a>

            <a class="btn btn-danger btn-custom"
               href="${pageContext.request.contextPath}/app?action=exportRepartitionPdf">
                PDF
            </a>

            <a class="btn btn-dark btn-custom"
               href="${pageContext.request.contextPath}/app?action=exportRepartitionWord">
                Word
            </a>

        </div>
    </div>

    <!-- TABLE -->
    <div class="table-card">

        <table class="table table-bordered table-hover text-center">

            <thead>
                <tr>
                    <th colspan="2">Encadrant</th>
                    <th colspan="8">Étudiants encadrés</th>
                </tr>

                <tr>
                    <th>Nom</th>
                    <th>Prénom</th>

                    <th colspan="2">Étudiant 1</th>
                    <th colspan="2">Étudiant 2</th>
                    <th colspan="2">Étudiant 3</th>
                    <th colspan="2">Étudiant 4</th>
                </tr>

                <tr>
                    <th></th><th></th>
                    <th>Nom</th><th>Prénom</th>
                    <th>Nom</th><th>Prénom</th>
                    <th>Nom</th><th>Prénom</th>
                    <th>Nom</th><th>Prénom</th>
                </tr>
            </thead>

            <tbody>

            <c:forEach var="entry" items="${repartition}">
                <tr>

                    <td><b>${entry.key.nom}</b></td>
                    <td><b>${entry.key.prenom}</b></td>

                    <c:forEach begin="0" end="3" var="i">

                        <c:choose>

                            <c:when test="${not empty entry.value[i]}">

                                <td class="${entry.value[i].filiere}">
                                    ${entry.value[i].nom}
                                </td>

                                <td class="${entry.value[i].filiere}">
                                    ${entry.value[i].prenom}
                                </td>

                            </c:when>

                            <c:otherwise>
                                <td>-</td>
                                <td>-</td>
                            </c:otherwise>

                        </c:choose>

                    </c:forEach>

                </tr>
            </c:forEach>

            </tbody>

        </table>

    </div>

</div>

</body>
</html>