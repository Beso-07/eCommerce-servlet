<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>500 - Server Error</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="/partials/navbar.jsp"/>
<main class="container py-5">
    <div class="alert alert-danger shadow-sm">
        <h1 class="h4 mb-2">500 - Internal Server Error</h1>
        <p class="mb-0">Something went wrong. Please try again later.</p>
    </div>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/products">Back to Home</a>
</main>
</body>
</html>
