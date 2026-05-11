package com.ecommerce.model;

public class Review {
    private long id;
    private long productId;
    private long userId;
    private String reviewerName;
    private int rating;
    private String comment;

    public Review() {
    }

    public Review(long id, long productId, long userId, String reviewerName, int rating, String comment) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
