package edu.monash.service;

import edu.monash.domain.Product;

/**
 * PricingService encapsulates all pricing-related rules:
 * <ul>
 *   <li>Computing effective unit price based on membership status</li>
 *   <li>Determining fulfilment (delivery/pickup) fees</li>
 *   <li>Applying student pickup discount eligibility</li>
 * </ul>
 *
 * <p>All methods are side-effect free and rely only on inputs and product data.</p>
 * @author Yujie Shao
 */
public class PricingService {

    /**
     * Returns the effective unit price for a product given membership status.
     *
     * @param p         product to price
     * @param isMember  whether the buyer has an active membership
     * @return member price if {@code isMember} is true; otherwise regular price
     */
    public double unitPrice(Product p, boolean isMember) {
        return isMember ? p.memberPrice : p.price;
    }

    /**
     * Computes the fulfilment fee for an order.
     * <ul>
     *   <li>Pickup: no fee</li>
     *   <li>Delivery: AUD $20, waived for Monash students</li>
     * </ul>
     *
     * @param email        buyer email (used to detect Monash student)
     * @param fulfilment   "PICKUP" or "DELIVERY"
     * @return fee in AUD
     */
    public double fulfilmentFee(String email, String fulfilment) {
        if (!"DELIVERY".equalsIgnoreCase(fulfilment)) return 0.0;
        if (isMonashStudent(email)) return 0.0;
        return 20.0;
    }

    /**
     * Determines whether an email belongs to a Monash student domain.
     *
     * @param email email to evaluate
     * @return true if email ends with {@code @student.monash.edu}
     */
    private boolean isMonashStudent(String email) {
        return email != null && email.toLowerCase(java.util.Locale.ROOT).endsWith("@student.monash.edu");
    }

    /**
     * Returns the discount rate for student pickup orders.
     * <ul>
     *   <li>Eligible domains: {@code @student.monash.edu} and {@code @monash.edu}</li>
     *   <li>Applies only when {@code fulfilment == "PICKUP"}</li>
     *   <li>Rate: 5% (0.05)</li>
     * </ul>
     *
     * @param email        buyer email
     * @param fulfilment   "PICKUP" or "DELIVERY"
     * @return discount rate in [0, 1], 0.05 if eligible, otherwise 0
     */
    public double studentPickupDiscountRate(String email, String fulfilment) {
        boolean isStudent = email != null && (email.endsWith("@student.monash.edu") || email.endsWith("@monash.edu"));
        if (isStudent && "PICKUP".equalsIgnoreCase(fulfilment)) return 0.05;
        return 0.0;
    }
}
