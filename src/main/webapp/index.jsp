<%@ page import="java.util.List" %>
<%@ page import="com.ecommerce.model.Product" %>
<%@ page import="com.ecommerce.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    User currentUser = (User) session.getAttribute("user");
    boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    if (products == null) {
        response.sendRedirect(request.getContextPath() + "/products");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>eCommerce - Home</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="/partials/navbar.jsp"/>
<main class="container py-4">
    <h1 class="mb-4">Products</h1>
    <% if (products == null || products.isEmpty()) { %>
    <div class="alert alert-info">No products found</div>
    <% } %>
    <div class="row g-4">
        <% if (products != null) { for (Product product : products) { %>
        <div class="col-md-6 col-lg-4">
            <div class="card h-100 shadow-sm">
                <a class="text-decoration-none text-dark" href="${pageContext.request.contextPath}/products/details?id=<%= product.getId() %>">
                    <img src="<%= product.getImageUrl() %>" class="card-img-top" style="height:220px; object-fit:cover;" alt="<%= product.getName() %>">
                    <div class="card-body">
                        <h5 class="card-title"><%= product.getName() %></h5>
                        <p class="card-text fw-semibold mb-3">$<%= product.getPrice() %></p>
                    </div>
                </a>
                <div class="card-footer bg-white border-0 pt-0 pb-3 px-3">
                    <% if (isAdmin) { %>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-outline-primary w-50" data-bs-toggle="modal" data-bs-target="#editModal-<%= product.getId() %>">Edit</button>
                        <button type="button" class="btn btn-outline-danger w-50" data-bs-toggle="modal" data-bs-target="#deleteModal-<%= product.getId() %>">Delete</button>
                    </div>
                    <% } else { %>
                    <button type="button" class="btn btn-primary w-100">Add to Cart</button>
                    <% } %>
                </div>
            </div>
        </div>

        <% if (isAdmin) { %>
        <div class="modal fade" id="deleteModal-<%= product.getId() %>" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Confirm Delete</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">Are you sure you want to delete this product?</div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <form method="post" action="${pageContext.request.contextPath}/products/delete">
                            <input type="hidden" name="id" value="<%= product.getId() %>">
                            <button type="submit" class="btn btn-danger">Delete</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="editModal-<%= product.getId() %>" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/products/edit">
                        <div class="modal-header">
                            <h5 class="modal-title">Edit Product</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <input type="hidden" name="id" value="<%= product.getId() %>">
                            <div class="mb-3">
                                <label class="form-label">Name</label>
                                <input class="form-control" type="text" name="name" value="<%= product.getName() %>" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Price</label>
                                <input class="form-control" type="number" step="0.01" name="price" value="<%= product.getPrice() %>" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Image URL</label>
                                <input class="form-control" type="url" name="imageUrl" value="<%= product.getImageUrl() %>" required>
                            </div>
                            <div class="mb-0">
                                <label class="form-label">Description</label>
                                <textarea class="form-control" name="description" required><%= product.getDescription() %></textarea>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="submit" class="btn btn-primary">Save Changes</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <% } %>
        <% }} %>
    </div>
</main>
<jsp:include page="/partials/footer.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>