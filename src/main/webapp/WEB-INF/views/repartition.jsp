<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Répartition des encadrants</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
    <style>
        table {
            border-collapse: collapse;
            width: 100%;
            font-family: Arial;
        }
        th, td {
            border: 1px solid #aaa;
            padding: 6px;
            text-align: center;
        }
        .header {
            background-color: #00A9D6;
            color: white;
            font-weight: bold;
        }
        .subheader {
            background-color: #D9E2F3;
            font-weight: bold;
        }
        .TDIA { background-color: #F4B183; }
        .ID   { background-color: #B7E1CD; }
        .GI   { background-color: #B4C6E7; }
    </style>
</head>
<body>
<div class="container-fluid p-4">

    <!-- Navigation -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2>👥 Répartition des encadrants</h2>
        <div>
            <a href="<%=request.getContextPath()%>/app?action=voirPlanning"
               class="btn btn-outline-primary me-2">
                📅 Voir Planning
            </a>
            <a href="<%=request.getContextPath()%>/app" class="btn btn-secondary">
                ← Retour Accueil
            </a>
        </div>
    </div>

    <!-- Légende filières -->
    <div class="mb-3">
        <span class="badge" style="background-color:#F4B183; color:#000; margin-right:8px;">TDIA</span>
        <span class="badge" style="background-color:#B7E1CD; color:#000; margin-right:8px;">ID</span>
        <span class="badge" style="background-color:#B4C6E7; color:#000;">GI</span>
    </div>

    <table>
        <tr>
            <th class="header" colspan="2">Encadrant</th>
            <th class="header" colspan="8">Étudiants encadrés</th>
        </tr>
        <tr>
            <th class="subheader">Nom</th>
            <th class="subheader">Prénom</th>
            <th class="subheader" colspan="2">Étudiant 1</th>
            <th class="subheader" colspan="2">Étudiant 2</th>
            <th class="subheader" colspan="2">Étudiant 3</th>
            <th class="subheader" colspan="2">Étudiant 4</th>
        </tr>
        <tr>
            <th></th>
            <th></th>
            <th>Nom</th><th>Prénom</th>
            <th>Nom</th><th>Prénom</th>
            <th>Nom</th><th>Prénom</th>
            <th>Nom</th><th>Prénom</th>
        </tr>

        <c:forEach var="entry" items="${repartition}">
            <tr>
                <td><strong>${entry.key.nom}</strong></td>
                <td>${entry.key.prenom}</td>
                <c:forEach begin="0" end="3" var="i">
                    <c:choose>
                        <c:when test="${not empty entry.value[i]}">
                            <td class="${entry.value[i].filiere}">${entry.value[i].nom}</td>
                            <td class="${entry.value[i].filiere}">${entry.value[i].prenom}</td>
                        </c:when>
                        <c:otherwise>
                            <td></td>
                            <td></td>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>

</div>
</body>
</html>

