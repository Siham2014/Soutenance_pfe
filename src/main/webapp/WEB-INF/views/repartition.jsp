<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <title>Répartition des encadrants</title>

    <link rel="stylesheet"
          href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">

    <style>
        .TDIA {
            background-color: #fff2cc !important;
        }

        .ID {
            background-color: #d9ead3 !important;
        }

        .GI {
            background-color: #c9daf8 !important;
        }
    </style>
</head>

<body>

<div class="container mt-4">

    <h2>Répartition des encadrants</h2>

    <a class="btn btn-primary btn-sm"
       href="${pageContext.request.contextPath}/app?action=voirPlanning">
        Voir Soutenances
    </a>

    <a class="btn btn-secondary btn-sm"
       href="${pageContext.request.contextPath}/app">
        Retour Accueil
    </a>

    <br><br>

    <a class="btn btn-success"
   href="${pageContext.request.contextPath}/app?action=exportRepartitionExcel">
    Exporter Excel
</a>

<a class="btn btn-danger"
   href="${pageContext.request.contextPath}/app?action=exportRepartitionPdf">
    Exporter PDF
</a>

<a class="btn btn-primary"
   href="${pageContext.request.contextPath}/app?action=exportRepartitionWord">
    Exporter Word
</a>

    <br><br>

    <table class="table table-bordered table-hover text-center">

        <tr class="table-primary">
            <th colspan="2">Encadrant</th>
            <th colspan="8">Étudiants encadrés</th>
        </tr>

        <tr class="table-secondary">
            <th>Nom</th>
            <th>Prénom</th>

            <th colspan="2">Étudiant 1</th>
            <th colspan="2">Étudiant 2</th>
            <th colspan="2">Étudiant 3</th>
            <th colspan="2">Étudiant 4</th>
        </tr>

        <tr>
            <th></th>
            <th></th>

            <th>Nom</th>
            <th>Prénom</th>

            <th>Nom</th>
            <th>Prénom</th>

            <th>Nom</th>
            <th>Prénom</th>

            <th>Nom</th>
            <th>Prénom</th>
        </tr>

        <c:forEach var="entry" items="${repartition}">
            <tr>
                <td>${entry.key.nom}</td>
                <td>${entry.key.prenom}</td>

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

    </table>

</div>

</body>
</html>