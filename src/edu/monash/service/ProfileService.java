package edu.monash.service;

import edu.monash.repo.AccountsRepo;
import edu.monash.repo.MembershipsRepo;

/**
 * ProfileService exposes read-only profile information derived from
 * account balances and membership records.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Retrieve current account balance</li>
 *   <li>Determine whether a user's membership is currently active</li>
 *   <li>Render a concise membership status string for display</li>
 * </ul>
 *
 * <p>All methods are side-effect free.</p>
 * @author Yujie Shao
 */
public class ProfileService {
    private final AccountsRepo accounts;
    private final MembershipsRepo memberships;

    /**
     * Creates a ProfileService bound to accounts and memberships repositories.
     *
     * @param accounts     repository providing balances
     * @param memberships  repository providing membership records
     */
    public ProfileService(AccountsRepo accounts, MembershipsRepo memberships){
        this.accounts = accounts;
        this.memberships = memberships;
    }

    /**
     * Returns the current balance for the given user.
     *
     * @param email user identifier
     * @return available balance, or 0.0 if no record exists
     */
    public double getBalance(String email){
        return accounts.balance.getOrDefault(email, 0.0);
    }

    /**
     * Indicates whether the user's membership is active as of today.
     *
     * <p>A membership is considered active if:
     * <ul>
     *   <li>Status equals {@code "ACTIVE"} (case-insensitive), and</li>
     *   <li>{@code endDate} is null or not before today's date.</li>
     * </ul>
     * </p>
     *
     * @param email user identifier
     * @return true if membership is currently active; otherwise false
     */
    public boolean isMemberActive(String email){
        var r = memberships.membership.get(email);
        if (r == null) return false;
        return "ACTIVE".equalsIgnoreCase(r.status)
                && (r.endDate == null || !r.endDate.isBefore(java.time.LocalDate.now()));
    }

    /**
     * Returns a human-readable membership summary.
     * <p>Examples: {@code "ACTIVE (2025-01-01 ~ 2026-01-01)"},
     * {@code "CANCELLED (2024-05-01 ~ 2024-10-01)"}, or {@code "No membership"}.</p>
     *
     * @param email user identifier
     * @return formatted membership text
     */
    public String membershipText(String email){
        var r = memberships.membership.get(email);
        if (r == null) return "No membership";
        return r.status + " (" + r.startDate + " ~ " + r.endDate + ")";
    }
}
