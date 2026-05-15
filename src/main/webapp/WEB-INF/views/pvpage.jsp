<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>




<h2>PV des soutenances</h2>

<ul>
    <c:forEach var="entry" items="${pvParProf}">

        <li>
            <strong>
                Pr. ${entry.key.nom} ${entry.key.prenom}
            </strong>

            <a href="${pageContext.request.contextPath}/app?action=listePv&profId=${entry.key.id}">
                Voir les étudiants
            </a>

            <c:if test="${profIdSelectionne == entry.key.id}">
                <ul>
                    <c:forEach var="s" items="${entry.value}">
                        <li>
                            ${s.etudiant.nom} ${s.etudiant.prenom}

                            <a href="${pageContext.request.contextPath}/app?action=genererPv&id=${s.id}">
                                PV
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>
        </li>

    </c:forEach>
</ul>
</body>
</html>