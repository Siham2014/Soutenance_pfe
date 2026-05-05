<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Importation</title>
</head>
<body>
<form method="post"
      action="${pageContext.request.contextPath}/app?action=importerEtRepartir"
      enctype="multipart/form-data">

    Selectionner fichier :
    <input type="file" name="fichierExcel" accept=".xlsx" required />

    <input type="submit" value="Upload"/>
</form>
</body>
</html>