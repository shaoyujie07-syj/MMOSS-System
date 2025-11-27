package edu.monash.service;

import edu.monash.repo.PromosRepo;
import edu.monash.repo.OrdersRepo;

/**
 * PromoService encapsulates business rules for promotional discounts.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Lookup promo definitions from {@link PromosRepo}</li>
 *   <li>Validate usage constraints (expiry, first-order, fulfilment type)</li>
 *   <li>Return a discount rate in [0.0, 0.9]</li>
 * </ul>
 *
 * <p>Current special-case constraints:</p>
 * <ul>
 *   <li>{@code FIRST_PICKUP} and {@code NEWMONASH20}:
 *       applies only to the customer's first-ever order <em>and</em> only for PICKUP fulfilment.</li>
 *   <li>All promo percentages are clamped to a maximum of 90%.</li>
 * </ul>
 * @author Yujie Shao
 */
public class PromoService {
    private final PromosRepo promos;
    private final OrdersRepo orders;

    /**
     * Creates a PromoService bound to promo and order repositories.
     *
     * @param promos repository of promo definitions
     * @param orders repository used to check first-order eligibility
     */
    public PromoService(PromosRepo promos, OrdersRepo orders){ this.promos = promos; this.orders = orders; }

    /**
     * Resolves the applicable discount rate for a promo code.
     *
     * <p>Validation flow:</p>
     * <ol>
     *   <li>Empty/blank code → 0.0</li>
     *   <li>Promo not found or expired → 0.0</li>
     *   <li>Special codes ({@code FIRST_PICKUP}, {@code NEWMONASH20}) require:
     *       <ul>
     *         <li>Customer has no prior orders (first order)</li>
     *         <li>Fulfilment is PICKUP</li>
     *       </ul>
     *       If unmet, → 0.0
     *   </li>
     *   <li>Otherwise return {@code percent/100.0}, clamped to ≤ 0.90</li>
     * </ol>
     *
     * @param email     customer email (used to determine first-order eligibility)
     * @param code      promo code (case-insensitive)
     * @param isPickup  whether the order fulfilment is PICKUP
     * @return discount rate in [0.0, 0.9]
     */
    public double getDiscountPercent(String email, String code, boolean isPickup) {
        if (code == null || code.isBlank()) return 0.0;

        var r = promos.promos.get(code.toUpperCase());
        if (r == null) return 0.0;
        if (r.isExpired()) return 0.0;

        boolean first = orders.countOrdersByEmailSafe(email) == 0;
        if (("FIRST_PICKUP".equalsIgnoreCase(code) || "NEWMONASH20".equalsIgnoreCase(code))) {
            if (!isPickup || !first) return 0.0;
        }

        int pct = Math.max(0, Math.min(90, r.percent));
        return pct / 100.0;
    }
}

