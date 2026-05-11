<%@ page import="com.ecommerce.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User user = (User) request.getAttribute("userDetails");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Profile</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="/partials/navbar.jsp"/>
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h1 class="h3 mb-3">Profile</h1>
                    <p><strong>Name:</strong> <%= user.getName() %></p>
                    <p><strong>Email:</strong> <%= user.getEmail() %></p>
                    <p><strong>Role:</strong> <%= user.getRole() %></p>
                    <div class="d-flex gap-2">
                        <form method="post" action="${pageContext.request.contextPath}/profile/delete">
                            <button type="submit" class="btn btn-danger">Delete Account</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/logout">
                            <button type="submit" class="btn btn-outline-secondary">Logout</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
