package com.example.demo.model;
/**
 * Represents a user account with a username and scores.
 */
public class Account implements Comparable<Account> {

    private static Account CURRENT = new Account("Guest");

    /** Get the current session account (default: "Guest"). */
    public static Account getCurrent() { return CURRENT; }

    public static void loginAs(String userName) {
        if (userName == null || userName.isBlank()) userName = "Guest";
        CURRENT = new Account(userName);
    }

    /** Reset the session to Guest (useful on logout). */
    public static void logoutToGuest() { CURRENT = new Account("Guest"); }

    private final String userName;
    private long score;     
    private long lastScore;
    private long highScore;  

    public Account(String userName) {
        this.userName = userName;
        this.score = 0;
        this.lastScore = 0;
        this.highScore = 0;
    }

    public String getUserName() { return userName; }
    public long getScore() { return score; }
    public long getLastScore() { return lastScore; }
    public long getHighScore() { return highScore; }

    /** Add to cumulative score (optional usage). */
    public void addToScore(long additionalScore) {
        this.score += additionalScore;
    }

    /** Call this when a game ends. Updates lastScore and highScore. */
    public void recordFinalScore(long finalScore) {
        this.lastScore = finalScore;
        if (finalScore > this.highScore) this.highScore = finalScore;
    }

    /** Clear session-local scores (doesn't change username). */
    public void resetSessionScores() {
        this.lastScore = 0;
        this.highScore = 0;
        this.score = 0;
    }

    /** Compare by score (desc). Keep existing behavior for leaderboards. */
    @Override
    public int compareTo(Account other) {
        return Long.compare(other.getScore(), this.score); // descending
    }
//Future plans of firebase integration
    private String uid;      // Firebase UID
    private String idToken;  // Firebase idToken

    public void setAuth(String uid, String idToken) {
        this.uid = uid;
        this.idToken = idToken;
    }
    public boolean isLoggedIn() { return uid != null && idToken != null; }
    public String getUid() { return uid; }
    public String getIdToken() { return idToken; }
}