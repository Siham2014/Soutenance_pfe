<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <title>Répartition des encadrants</title>

    <style>
        table {
            border-collapse: collapse;
            width: 100%;
            font-family: Arial;
        }

        th, td {
            border: 1px solid black;
            padding: 6px;
            text-align: center;
        }

        .header {
            background-color: #00A9D6;
            font-weight: bold;
        }

        .subheader {
            background-color: #D9E2F3;
            font-weight: bold;
        }

        .TDIA {
            background-color: #F4B183;
        }

        .ID {
            background-color: #B7E1CD;
        }

        .GI {
            background-color: #B4C6E7;
        }
    </style>
</head>

<body>

<h2>Répartition des encadrants</h2>

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
            <td>${entry.key.nom}</td>
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

</body>
