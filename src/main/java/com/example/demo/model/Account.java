package com.example.demo.model;

/**
 * Represents a user account with a username and score.
 */
public class Account implements Comparable<Account> {

    private long score;
    private final String userName;

    public Account(String userName) {
        this.userName = userName;
        this.score = 0;
    }

    public String getUserName() {
        return userName;
    }

    public long getScore() {
        return score;
    }

    public void addToScore(long additionalScore) {
        this.score += additionalScore;
    }

    /**
     * Accounts are compared based on score in descending order.
     */
    @Override
    public int compareTo(Account other) {
        return Long.compare(other.getScore(), this.score); // descending order
    }
}
