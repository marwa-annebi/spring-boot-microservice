package com.example.auth.helpers;

import java.util.Random;

public class GenerateUniqueName {
    public static String generateUsername() {
        // Descriptive terms to choose from
        String[] adjectives = {"Excellent", "Great", "Amazing", "Awesome", "Superb"};

        // Randomly select an adjective
        Random random = new Random();
        String adjective = adjectives[random.nextInt(adjectives.length)];

        // Generate a random number between 100 and 999
        int randomNumber = random.nextInt(900) + 100;

        // Combine the adjective, term, and random number to form the username
        String username = adjective + "-Serve-" + randomNumber;

        return username;
    }
}
