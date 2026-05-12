<%@ page import="java.util.List" %>
<%@ page import="com.ecommerce.model.Product" %>
<%@ page import="com.ecommerce.model.Review" %>
<%@ page import="com.ecommerce.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Product product = (Product) request.getAttribute("product");
    List<Review> reviews = (List<Review>) request.getAttribute("reviews");
    User currentUser = (User) session.getAttribute("user");
    boolean isUser = currentUser != null && "USER".equalsIgnoreCase(currentUser.getRole());    String reviewError = (String) session.getAttribute("reviewError");
    if (reviewError != null) {
        session.removeAttribute("reviewError");
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Product Details</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        .star-input { display: inline-flex; flex-direction: row-reverse; gap: .25rem; }
        .star-input input { display: none; }
        .star-input label { font-size: 2rem; color: #d1d5db; cursor: pointer; line-height: 1; }
        .star-input label:hover,
        .star-input label:hover ~ label,
        .star-input input:checked ~ label { color: #f59e0b; }
    </style>
</head>
<body class="bg-light">
<jsp:include page="/navbar.jsp"/>
<main class="container py-4">
    <% if (product != null) { %>
    <div class="card shadow-sm mb-4">
        <div class="row g-0">
            <div class="col-md-4">
                <img src="<%= product.getImageUrl() %>" class="img-fluid rounded-start h-100" style="object-fit:cover;" alt="<%= product.getName() %>">
            </div>
            <div class="col-md-8">
                <div class="card-body">
                    <h1 class="h3"><%= product.getName() %></h1>
                    <p class="mb-2"><%= product.getDescription() %></p>
                    <p class="fw-bold">$<%= product.getPrice() %></p>
                </div>
            </div>
        </div>
    </div>
    <% } else { %>
    <div class="alert alert-warning">Product not found</div>
    <% } %>
    <section class="mb-4">
        <h2 class="h4">Reviews</h2>
        <% if (reviews == null || reviews.isEmpty()) { %>
        <div class="alert alert-light border">No reviews yet</div>
        <% } else { for (Review review : reviews) { %>
        <div class="card mb-2">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                        <strong><%= review.getReviewerName() %></strong>
                        <span class="text-warning"><%= "★".repeat(review.getRating()) %></span>
                        <span class="text-muted">(<%= review.getRating() %>/5)</span>
                        <p class="mb-0"><%= review.getComment() %></p>
                    </div>
                    <% if (currentUser != null && currentUser.getId() == review.getUserId()) { %>
                    <a href="${pageContext.request.contextPath}/reviews/delete?reviewId=<%= review.getId() %>" 
                       class="btn btn-sm btn-outline-danger" 
                       onclick="return confirm('Are you sure you want to delete this review?')">
                        <i class="bi bi-trash"></i> Delete
                    </a>
                    <% } %>
                </div>
            </div>
        </div>
        <% }} %>
    </section>
    <% if (isUser && product != null) { %>
    <section class="card shadow-sm">
        <div class="card-body">
        <h3 class="h5">Add Review</h3>
        <% if (reviewError != null) { %>
        <div class="alert alert-danger"><%= reviewError %></div>
        <% } %>
        <form method="post" action="${pageContext.request.contextPath}/reviews/add">
            <input type="hidden" name="productId" value="<%= product.getId() %>">
            <div class="mb-3">
                <label class="form-label">Rating</label>
                <div class="star-input">
                    <input id="star5" type="radio" name="rating" value="5" required>
                    <label for="star5" data-label="Excellent" title="Excellent">&#9733;</label>
                    <input id="star4" type="radio" name="rating" value="4">
                    <label for="star4" data-label="Very Good" title="Very Good">&#9733;</label>
                    <input id="star3" type="radio" name="rating" value="3">
                    <label for="star3" data-label="Good" title="Good">&#9733;</label>
                    <input id="star2" type="radio" name="rating" value="2">
                    <label for="star2" data-label="Fair" title="Fair">&#9733;</label>
                    <input id="star1" type="radio" name="rating" value="1">
                    <label for="star1" data-label="Poor" title="Poor">&#9733;</label>
                </div>
                <small id="ratingMeaning" class="text-muted d-block mt-1">Select a rating</small>
            </div>
            <div class="mb-3">
                <label class="form-label">Comment</label>
                <textarea class="form-control" name="comment" required placeholder="Write your review"></textarea>
            </div>
            <button class="btn btn-primary" type="submit">Submit Review</button>
        </form>
        </div>
    </section>
    <% } else if (currentUser == null && product != null) { %>
    <div class="alert alert-secondary">
        <a href="${pageContext.request.contextPath}/login.jsp" class="alert-link">Login to write a review</a>
    </div>
    <% } %>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (() => {
        const labels = document.querySelectorAll('.star-input label');
        const text = document.getElementById('ratingMeaning');
        if (!labels.length || !text) return;

        const updateSelected = () => {
            const checked = document.querySelector('.star-input input:checked');
            if (!checked) return;
            const label = document.querySelector('label[for="' + checked.id + '"]');
            if (label) text.textContent = checked.value + '/5 - ' + label.dataset.label;
        };

        labels.forEach((label) => {
            label.addEventListener('mouseenter', () => {
                const input = document.getElementById(label.getAttribute('for'));
                text.textContent = input.value + '/5 - ' + label.dataset.label;
            });
            label.addEventListener('click', updateSelected);
        });

        const starContainer = document.querySelector('.star-input');
        if (starContainer) {
            starContainer.addEventListener('mouseleave', updateSelected);
        }
    })();
</script>
</body>
</html>
