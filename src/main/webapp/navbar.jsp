<%@ page import="com.ecommerce.model.User" %>
<%
    User navUser = (User) session.getAttribute("user");
    boolean isAdmin = navUser != null && "ADMIN".equalsIgnoreCase(navUser.getRole());
%>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/index.jsp">eCommerce</a>
        
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav ms-auto mb-2 mb-lg-0 d-flex align-items-center">
                
                <li class="nav-item mx-3">
                    <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp">Home</a>
                </li>

                <% if (navUser == null) { %>
                    <li class="nav-item mx-2"><a class="nav-link" href="${pageContext.request.contextPath}/login.jsp">Login</a></li>
                    <li class="nav-item mx-2"><a class="nav-link" href="${pageContext.request.contextPath}/register.jsp">Register</a></li>
                <% } else { %>
                    
                    <% if (isAdmin) { %>
                        <li class="nav-item mx-3">
                            <a class="nav-link text-warning fw-bold border border-warning rounded px-2" href="${pageContext.request.contextPath}/admin">Dashboard</a>
                        </li>
                    <% } %>

                    <li class="nav-item px-4 border-start border-secondary">
                        <div class="text-end text-light">
                            <div class="fw-semibold fs-6 d-flex align-items-center justify-content-end gap-2">
                                <span><%= navUser.getName() %></span>
                                <% if (isAdmin) { %><span class="badge bg-warning text-dark">Admin</span><% } %>
                            </div>
                            <small class="text-secondary" style="font-size: 0.75rem;"><%= navUser.getEmail() %></small>
                        </div>
                    </li>

                    <li class="nav-item ms-3 d-flex flex-column align-items-center gap-1">
                        <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
                            <button class="btn btn-link nav-link py-0 fw-bold text-white" type="submit">Logout</button>
                        </form>
                        
                        <% if (!isAdmin) { %>
                            <a class="nav-link text-info py-0" 
                               style="font-size: 0.8rem; text-decoration: underline;" 
                               href="${pageContext.request.contextPath}/profile">
                                View Profile
                            </a>
                        <% } %>
                    </li>
                <% } %>
            </ul>
        </div>
    </div>
</nav>