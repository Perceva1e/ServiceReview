package com.example.reviewservice.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Review {
    private Long id;
    private int rating;
    private int numberOfLikes;
    private int numberOfDislikes;
    private String text;
    private LocalDate publicationDate;
    private User user;
    private Film film;
}