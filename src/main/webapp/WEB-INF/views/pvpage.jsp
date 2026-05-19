<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>PV des soutenances</title>

<link rel="stylesheet"
      href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">

<style>

body{
    background:#f5f7fb;
    font-family:'Segoe UI', sans-serif;
}

/* container */
.page-container{
    max-width:900px;
    margin:60px auto;
    background:#fff;
    padding:30px;
    border-radius:16px;
    box-shadow:0 10px 30px rgba(0,0,0,0.08);
}

/* title */
.page-title{
    font-size:1.6rem;
    font-weight:700;
    color:#1f2937;
    margin-bottom:20px;
}

/* professor card */
.prof-card{
    background:#f9fafb;
    border:1px solid #e5e7eb;
    border-radius:12px;
    padding:15px;
    margin-bottom:15px;
}

/* prof header */
.prof-header{
    display:flex;
    justify-content:space-between;
    align-items:center;
}

/* name */
.prof-name{
    font-weight:600;
    color:#111827;
}

/* links */
a{
    text-decoration:none;
}

/* button style */
.btn-custom{
    font-size:.85rem;
    padding:6px 10px;
    border-radius:8px;
}

/* student list */
.student-list{
    margin-top:10px;
    padding-left:20px;
}

/* student item */
.student-item{
    background:#fff;
    padding:10px;
    border-radius:10px;
    margin-bottom:8px;
    border:1px solid #eee;
    display:flex;
    justify-content:space-between;
    align-items:center;
}

.pv-btn{
    background:#1f2937;
    color:#fff;
    padding:6px 10px;
    border-radius:8px;
    font-size:.8rem;
}

.pv-btn:hover{
    background:#374151;
}

.toggle-link{
    font-size:.85rem;
    color:#2563eb;
}

.toggle-link:hover{
    text-decoration:underline;
}

</style>

</head>

<body>

<div class="page-container">

    <div class="page-title"> PV des soutenances</div>

    <c:forEach var="entry" items="${pvParProf}">

        <div class="prof-card">

            <div class="prof-header">

                <div class="prof-name">
                     Pr. ${entry.key.nom} ${entry.key.prenom}
                </div>

                <a class="toggle-link"
                   href="${pageContext.request.contextPath}/app?action=listePv&profId=${entry.key.id}">
                    Voir les étudiants
                </a>

            </div>

            <c:if test="${profIdSelectionne == entry.key.id}">

                <div class="student-list">

                    <c:forEach var="s" items="${entry.value}">

                        <div class="student-item">

                            <div>
                                 ${s.etudiant.nom} ${s.etudiant.prenom}
                            </div>

                            <a class="pv-btn"
                               href="${pageContext.request.contextPath}/app?action=genererPv&id=${s.id}">
                                Générer PV
                            </a>

                        </div>

                    </c:forEach>

                </div>

            </c:if>

        </div>

    </c:forEach>

</div>

</body>
</html>