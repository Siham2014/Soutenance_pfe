<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Home </title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
</head>
<body>
<div class="container text-center" style="margin-top: 100px;">
    <h1>Bienvenue </h1>
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-body">
                    
                    <a href="<%=request.getContextPath()%>/app?action=pageImportEncadrants" class="btn btn-danger">Repartition des encadrants </a>
                     <a href="${pageContext.request.contextPath}/app?action=genererPv&id=${g.id}">
                           Générer PV
                     </a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>