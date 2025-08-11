package com.example.demo.model;

/**
 * Represents a user account with a username, game scores, and authentication data.
 * This class supports tracking cumulative score, last game score, and high score.
 * It also manages the current session account statically, allowing login/logout functionality.
 * Future plans include Firebase integration for authentication and data persistence.
 *
 * <p>Instances of this class are used to represent both guest and authenticated users.
 * The {@link #getCurrent()} method provides access to the currently logged-in session account.</p>
 *
 * @author  Gnerated JavaDoc
 * @version 1.0
 */
public class Account implements Comparable<Account> {

    /**
     * The currently active session account. Defaults to a guest user.
     * This static field enables global access to the current user context.
     */
    private static Account CURRENT = new Account("Guest");

    /**
     * Retrieves the currently logged-in account in the session.
     * If no user is explicitly logged in, this returns a guest account.
     *
     * @return the current {@link Account} instance representing the session user
     */
    public static Account getCurrent() {
        return CURRENT;
    }

    /**
     * Logs in as the specified user by creating a new account and setting it as current.
     * If the provided username is null or blank, defaults to "Guest".
     *
     * @param userName the name of the user to log in as; may be null or empty
     */
    public static void loginAs(String userName) {
        if (userName == null || userName.isBlank()) userName = "Guest";
        CURRENT = new Account(userName);
    }

    /**
     * Logs out the current user and resets the session to a guest account.
     * This clears any session-specific score data for the previous user.
     */
    public static void logoutToGuest() {
        CURRENT = new Account("Guest");
    }

    /**
     * The unique name identifying this account. Cannot be changed after creation.
     */
    private final String userName;

    /**
     * The cumulative score across multiple games or sessions (optional usage).
     * Can be incremented using {@link #addToScore(long)}.
     */
    private long score;

    /**
     * The score from the most recent game session.
     * Updated via {@link #recordFinalScore(long)}.
     */
    private long lastScore;

    /**
     * The highest score achieved in any single game session by this user.
     * Automatically updated when a new final score exceeds the current high score.
     */
    private long highScore;

    /**
     * Constructs a new account with the given username.
     * Initializes all score values to zero.
     *
     * @param userName the name to assign to this account; must not be null
     */
    public Account(String userName) {
        this.userName = userName;
        this.score = 0;
        this.lastScore = 0;
        this.highScore = 0;
    }

    /**
     * Gets the username associated with this account.
     *
     * @return the username as a non-null string
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the cumulative score for this account.
     *
     * @return the current cumulative score
     */
    public long getScore() {
        return score;
    }

    /**
     * Gets the score from the last completed game.
     *
     * @return the last recorded final score
     */
    public long getLastScore() {
        return lastScore;
    }

    /**
     * Gets the highest score achieved in a single game session.
     *
     * @return the high score; initially zero
     */
    public long getHighScore() {
        return highScore;
    }

    /**
     * Adds the specified amount to the cumulative score.
     * This does not affect the last or high score unless manually recorded.
     *
     * @param additionalScore the amount to add; can be negative
     */
    public void addToScore(long additionalScore) {
        this.score += additionalScore;
    }

    /**
     * Records the final score at the end of a game.
     * Updates both the last score and the high score if this is a new best.
     *
     * @param finalScore the score to record; typically non-negative
     */
    public void recordFinalScore(long finalScore) {
        this.lastScore = finalScore;
        if (finalScore > this.highScore) this.highScore = finalScore;
    }

    /**
     * Resets all session-specific score values (score, lastScore, highScore) to zero.
     * The username remains unchanged.
     */
    public void resetSessionScores() {
        this.lastScore = 0;
        this.highScore = 0;
        this.score = 0;
    }

    /**
     * Compares this account to another based on cumulative score in descending order.
     * Used for leaderboard sorting where higher scores rank first.
     *
     * @param other the other account to compare to; must not be null
     * @return a negative integer, zero, or positive integer if this score is less than,
     *         equal to, or greater than the other's score
     */
    @Override
    public int compareTo(Account other) {
        return Long.compare(other.getScore(), this.score); // descending order
    }

    // ========================
    // Firebase Integration (Planned)
    // ========================

    /**
     * Firebase User ID (UID), used to uniquely identify authenticated users.
     * Null for guest or unauthenticated accounts.
     */
    private String uid;

    /**
     * Firebase ID token for authentication and secure API access.
     * Should only be set for authenticated users and handled securely.
     */
    private String idToken;

    /**
     * Sets the Firebase authentication credentials for this account.
     *
     * @param uid the Firebase UID; may be null
     * @param idToken the Firebase ID token; may be null
     */
    public void setAuth(String uid, String idToken) {
        this.uid = uid;
        this.idToken = idToken;
    }

    /**
     * Checks whether this account is authenticated (i.e., has both UID and ID token).
     *
     * @return true if both uid and idToken are non-null; false otherwise
     */
    public boolean isLoggedIn() {
        return uid != null && idToken != null;
    }

    /**
     * Gets the Firebase UID associated with this account.
     *
     * @return the Firebase UID, or null if not logged in
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the Firebase ID token for this account.
     *
     * @return the ID token, or null if not logged in
     */
    public String getIdToken() {
        return idToken;
    }
}