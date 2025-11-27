package edu.monash.usecase;

import edu.monash.service.ProfileService;

/**
 * <p><strong>ViewProfile — User Profile Viewing Use Case</strong></p>
 *
 * <p>This use case retrieves and formats a user's account information,
 * including balance and membership details, for display in the Monash Market
 * Online Supermarket System (MMOSS). It acts as a bridge between the UI layer
 * and {@link ProfileService}, ensuring consistent formatting and presentation
 * of profile data.</p>
 *
 * <p>The formatted output includes the user’s email, account balance, and
 * membership status, ready to be shown directly on the console or UI.</p>
 *
 * @author XiaoWei
 * @version 1.0
 * @since 2025-10-16
 */
public class ViewProfile {

    /** Service layer reference used to fetch profile and membership information. */
    private final ProfileService profile;

    /**
     * Constructs a {@code ViewProfile} use case with a profile service dependency.
     *
     * @param profile the {@link ProfileService} instance providing user account data.
     */
    public ViewProfile(ProfileService profile) {
        this.profile = profile;
    }

    /**
     * Executes the profile view operation for a given user email.
     *
     * <p>Retrieves the user's current balance and membership status,
     * then returns them as a formatted, human-readable string.</p>
     *
     * @param email the user's email address whose profile will be displayed.
     * @return formatted profile summary string.
     */
    public String execute(String email) {
        // Retrieve and format basic profile details
        return "Email: " + email + "\n" +
                "Balance: $" + String.format("%.2f", profile.getBalance(email)) + "\n" +
                "Membership: " + profile.membershipText(email);
    }
}
