package com.example.reviewservice.controller;

import com.example.reviewservice.model.Review;
import com.example.reviewservice.service.ReviewCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Catalog API", description = "API for managing reviews, ratings, and likes/dislikes")
public class ReviewCatalogController {

    private final ReviewCatalogService reviewCatalogService;

    @Autowired
    public ReviewCatalogController(ReviewCatalogService reviewCatalogService) {
        this.reviewCatalogService = reviewCatalogService;
    }

    @Operation(summary = "Get all reviews", description = "Retrieves a list of all reviews")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of reviews")
    })
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        log.info("Fetching all reviews");
        List<Review> reviews = reviewCatalogService.getAllReviews();
        log.debug("Retrieved {} reviews", reviews.size());
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get review by ID", description = "Retrieves a review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved review"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@Parameter(description = "ID of the review") @PathVariable Long id) {
        log.info("Fetching review with ID: {}", id);
        return reviewCatalogService.getReviewById(id)
                .map(review -> {
                    log.debug("Found review with ID: {}", id);
                    return ResponseEntity.ok(review);
                })
                .orElseGet(() -> {
                    log.warn("Review with ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Create a new review", description = "Creates a new review with rating, text, user, and film details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid review data provided"),
            @ApiResponse(responseCode = "404", description = "User or film not found")
    })
    @PostMapping
    public ResponseEntity<Review> createReview(@Parameter(description = "Review details") @RequestBody Review review) {
        log.info("Creating new review for film ID: {}", review.getFilm() != null ? review.getFilm().getId() : null);
        try {
            Review createdReview = reviewCatalogService.createReview(review);
            log.debug("Created review with ID: {}", createdReview.getId());
            return ResponseEntity.ok(createdReview);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Update a review", description = "Updates an existing review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid review data provided"),
            @ApiResponse(responseCode = "404", description = "Review, user, or film not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @Parameter(description = "ID of the review to update") @PathVariable Long id,
            @Parameter(description = "Updated review details") @RequestBody Review reviewDetails) {
        log.info("Updating review with ID: {}", id);
        try {
            Review updatedReview = reviewCatalogService.updateReview(id, reviewDetails);
            log.debug("Updated review with ID: {}", updatedReview.getId());
            return ResponseEntity.ok(updatedReview);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update review with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            log.warn("Review with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a review", description = "Deletes a review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/{id}") // Changed from @PutMapping to @DeleteMapping
    public ResponseEntity<Void> deleteReview(@Parameter(description = "ID of the review to delete") @PathVariable Long id) {
        log.info("Deleting review with ID: {}", id);
        try {
            reviewCatalogService.deleteReview(id);
            log.debug("Deleted review with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Failed to delete review with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Add a like to a review", description = "Increments the like count for a review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like added successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<Review> addLike(@Parameter(description = "ID of the review to like") @PathVariable Long id) {
        log.info("Adding like to review with ID: {}", id);
        try {
            Review updatedReview = reviewCatalogService.addLike(id);
            log.debug("Added like to review with ID: {}, new like count: {}", id, updatedReview.getNumberOfLikes());
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            log.warn("Failed to add like to review with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Add a dislike to a review", description = "Increments the dislike count for a review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dislike added successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PostMapping("/{id}/dislike")
    public ResponseEntity<Review> addDislike(@Parameter(description = "ID of the review to dislike") @PathVariable Long id) {
        log.info("Adding dislike to review with ID: {}", id);
        try {
            Review updatedReview = reviewCatalogService.addDislike(id);
            log.debug("Added dislike to review with ID: {}, new dislike count: {}", id, updatedReview.getNumberOfDislikes());
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            log.warn("Failed to add dislike to review with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}