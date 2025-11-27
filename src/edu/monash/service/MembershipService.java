package edu.monash.service;

import edu.monash.repo.MembershipsRepo;
import java.time.LocalDate;

/**
 * Service layer for managing customer memberships (purchase, renew, cancel).
 *
 * <p><strong>Pricing & duration:</strong> each successful purchase or renewal
 * charges AUD $20 and extends membership by 1 year. Extension stacks:
 * if the current membership is still active (endDate ≥ today), the new end date
 * is computed from the existing end date; otherwise it starts from today.</p>
 *
 * <p>Persistence is handled via {@link MembershipsRepo}. Balance deduction is
 * delegated to {@link AccountService}.</p>
 * @author Yujie Shao
 */
public class MembershipService {
    private final MembershipsRepo repo;
    private final AccountService accounts;

    /**
     * Creates a MembershipService with required repositories/services.
     *
     * @param repo      membership repository
     * @param accounts  account service used for balance deduction
     */
    public MembershipService(MembershipsRepo repo, AccountService accounts){ this.repo = repo; this.accounts = accounts; }

    /**
     * Purchases membership for 1 year (AUD $20).
     *
     * <p>If a membership already exists, the end date is extended by 1 year
     * on top of the current end (if not expired) or from today (if expired).
     * On success the status is set to {@code ACTIVE}.</p>
     *
     * @param email customer identifier
     * @return "OK" on success, or "Insufficient funds" if balance is not enough
     */
    public String purchase(String email){
        if (!accounts.deduct(email, 20.0)) return "Insufficient funds";
        var today = LocalDate.now();
        var r = repo.membership.get(email);
        if (r == null) {
            r = new MembershipsRepo.Row(email, "VIP", today, today.plusYears(1), "ACTIVE");
        } else {
            var base = (r.endDate != null && !r.endDate.isBefore(today)) ? r.endDate : today;
            r.status = "ACTIVE";
            if (r.startDate == null) r.startDate = today;
            r.endDate = base.plusYears(1);
        }
        repo.membership.put(email, r);
        try { repo.save(); } catch (Exception ignored){}
        return "OK";
    }

    /**
     * Renews membership for 1 additional year (AUD $20).
     *
     * <p>Behavior matches {@link #purchase(String)} for stacking semantics:
     * extends from current end date if active, otherwise from today. On success
     * the status is set to {@code ACTIVE}.</p>
     *
     * @param email customer identifier
     * @return "OK" on success, or "Insufficient funds" if balance is not enough
     */
    public String renew(String email){
        if (!accounts.deduct(email, 20.0)) return "Insufficient funds";
        var today = LocalDate.now();
        var r = repo.membership.get(email);
        if (r == null) {
            r = new MembershipsRepo.Row(email, "VIP", today, today.plusYears(1), "ACTIVE");
        } else {
            var base = (r.endDate != null && !r.endDate.isBefore(today)) ? r.endDate : today;
            r.status = "ACTIVE";
            if (r.startDate == null) r.startDate = today;
            r.endDate = base.plusYears(1);
        }
        repo.membership.put(email, r);
        try { repo.save(); } catch (Exception ignored){}
        return "OK";
    }

    /**
     * Cancels the current membership.
     *
     * <p>If no membership exists, returns a friendly message. Otherwise sets
     * status to {@code CANCELLED} and persists.</p>
     *
     * @param email customer identifier
     * @return "OK" if a record existed and was cancelled; "No membership" otherwise
     */
    public String cancel(String email){
        var r = repo.membership.get(email);
        if (r == null) return "No membership";
        r.status = "CANCELLED";
        try { repo.save(); } catch (Exception ignored){}
        return "OK";
    }
}
