package edu.monash.service;

import edu.monash.repo.AccountsRepo;

/**
 * AccountService provides balance operations for customer accounts.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Top up (credit) a user's balance with basic input validation</li>
 *   <li>Deduct (debit) funds when sufficient balance is available</li>
 *   <li>Persist changes via {@link AccountsRepo#save()}</li>
 * </ul>
 *
 * <p>All monetary values are treated as simple doubles in AUD. Callers are
 * responsible for higher-level validations (e.g., business limits beyond those
 * enforced here) and for sequencing debits/credits as part of larger flows.</p>
 * @author Yujie Shao
 */
public class AccountService {
    private final AccountsRepo accounts;

    /**
     * Creates an AccountService bound to the provided repository.
     *
     * @param accounts repository that stores account balances
     */
    public AccountService(AccountsRepo accounts){ this.accounts = accounts; }

    /**
     * Credits the user's account balance.
     *
     * <p>Validation:</p>
     * <ul>
     *   <li>Amount must be &gt; 0</li>
     *   <li>Per top-up maximum is $1000</li>
     * </ul>
     *
     * @param email  user identifier
     * @param amount amount to add (AUD)
     * @return "OK" on success, otherwise a human-readable error message
     */
    public String topUp(String email, double amount){
        if (amount <= 0) return "Please enter a positive amount";
        if (amount > 1000) return "Please enter a smaller amount (max $1000 per top-up)";
        double cur = accounts.balance.getOrDefault(email, 0.0);
        accounts.balance.put(email, cur + amount);
        try { accounts.save(); } catch (Exception ignored){}
        return "OK";
    }

    /**
     * Debits the user's account balance if sufficient funds exist.
     *
     * @param email  user identifier
     * @param amount amount to deduct (AUD)
     * @return {@code true} if the balance was sufficient and funds were deducted; otherwise {@code false}
     */
    public boolean deduct(String email, double amount){
        double cur = accounts.balance.getOrDefault(email, 0.0);
        if (cur < amount) return false;
        accounts.balance.put(email, cur - amount);
        try { accounts.save(); } catch (Exception ignored){}
        return true;
    }
}
