package com.example.reviewservice.service;

import com.example.reviewservice.model.Film;
import com.example.reviewservice.model.Review;
import com.example.reviewservice.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewCatalogService {

    private final RestTemplate restTemplate;

    @Autowired
    public ReviewCatalogService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Review> getAllReviews() {
        log.info("Fetching all reviews from servicedb");
        Review[] reviewsArray = restTemplate.getForObject("/api/reviews", Review[].class);
        List<Review> reviews = Arrays.asList(reviewsArray != null ? reviewsArray : new Review[0]);
        log.debug("Retrieved {} reviews", reviews.size());
        return reviews;
    }

    public Optional<Review> getReviewById(Long id) {
        log.info("Fetching review with ID: {} from servicedb", id);
        try {
            Review review = restTemplate.getForObject("/api/reviews/{id}", Review.class, id);
            return Optional.ofNullable(review);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Review with ID {} not found", id);
                return Optional.empty();
            }
            log.error("Failed to retrieve review with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public Review createReview(Review review) {
        log.info("Creating new review for film ID: {}", review.getFilm().getId());
        validateReview(review);

        verifyUserExists(review.getUser().getId());
        verifyFilmExists(review.getFilm().getId());

        if (review.getPublicationDate() == null) {
            review.setPublicationDate(LocalDate.now());
        }

        Review createdReview = restTemplate.postForObject("/api/reviews", review, Review.class);
        log.debug("Created review with ID: {}", createdReview.getId());
        return createdReview;
    }

    public Review updateReview(Long id, Review reviewDetails) {
        log.info("Updating review with ID: {}", id);
        validateReview(reviewDetails);

        getReviewById(id).orElseThrow(() -> {
            log.warn("Review with ID {} not found for update", id);
            return new RuntimeException("Review not found with ID: " + id);
        });

        verifyUserExists(reviewDetails.getUser().getId());
        verifyFilmExists(reviewDetails.getFilm().getId());

        restTemplate.put("/api/reviews/{id}", reviewDetails, id);
        Review updatedReview = restTemplate.getForObject("/api/reviews/{id}", Review.class, id);
        log.debug("Updated review with ID: {}", updatedReview.getId());
        return updatedReview;
    }

    public void deleteReview(Long id) {
        log.info("Deleting review with ID: {}", id);
        try {
            restTemplate.delete("/api/reviews/{id}", id);
            log.debug("Deleted review with ID: {}", id);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Review with ID {} not found for deletion", id);
                throw new RuntimeException("Review not found with ID: " + id);
            }
            throw e;
        }
    }

    public Review addLike(Long reviewId) {
        log.info("Adding like to review with ID: {}", reviewId);
        Review review = getReviewById(reviewId).orElseThrow(() -> {
            log.warn("Review with ID {} not found for adding like", reviewId);
            return new RuntimeException("Review not found with ID: " + reviewId);
        });

        review.setNumberOfLikes(review.getNumberOfLikes() + 1);
        restTemplate.put("/api/reviews/{id}", review, reviewId);
        Review updatedReview = restTemplate.getForObject("/api/reviews/{id}", Review.class, reviewId);
        log.debug("Added like to review with ID: {}, new like count: {}", reviewId, updatedReview.getNumberOfLikes());
        return updatedReview;
    }

    public Review addDislike(Long reviewId) {
        log.info("Adding dislike to review with ID: {}", reviewId);
        Review review = getReviewById(reviewId).orElseThrow(() -> {
            log.warn("Review with ID {} not found for adding dislike", reviewId);
            return new RuntimeException("Review not found with ID: " + reviewId);
        });

        review.setNumberOfDislikes(review.getNumberOfDislikes() + 1);
        restTemplate.put("/api/reviews/{id}", review, reviewId);
        Review updatedReview = restTemplate.getForObject("/api/reviews/{id}", Review.class, reviewId);
        log.debug("Added dislike to review with ID: {}, new dislike count: {}", reviewId, updatedReview.getNumberOfDislikes());
        return updatedReview;
    }

    private void validateReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 10) {
            log.warn("Invalid rating: {} for review", review.getRating());
            throw new IllegalArgumentException("Rating must be between 1 and 10");
        }
        if (review.getUser() == null || review.getUser().getId() == null) {
            log.warn("User ID is required for review");
            throw new IllegalArgumentException("User ID is required");
        }
        if (review.getFilm() == null || review.getFilm().getId() == null) {
            log.warn("Film ID is required for review");
            throw new IllegalArgumentException("Film ID is required");
        }
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) {
            log.warn("Review text is required");
            throw new IllegalArgumentException("Review text is required");
        }
    }

    private void verifyUserExists(Long userId) {
        try {
            restTemplate.getForObject("/api/users/{id}", User.class, userId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User with ID {} not found", userId);
                throw new IllegalArgumentException("User with ID " + userId + " not found");
            }
            throw e;
        }
    }

    private void verifyFilmExists(Long filmId) {
        try {
            restTemplate.getForObject("/api/films/{id}", Film.class, filmId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Film with ID {} not found", filmId);
                throw new IllegalArgumentException("Film with ID " + filmId + " not found");
            }
            throw e;
        }
    }
}