package edu.monash.domain;

/**
 * A single line item inside a {@link ShoppingCart}.
 *
 * <p>Captures the chosen product, the desired quantity, and a snapshot
 * of the unit price at the time of checkout calculation (if used).
 * The {@code addedAtMillis} timestamp records when the line was created,
 * which is useful for stable display ordering.</p>
 * @author Yujie Shao
 */
public class CartItem {
    /** Product identifier this line refers to. */
    public String productId;

    /** Quantity requested for this product (must be &gt; 0). */
    public int quantity;

    /**
     * Unit price captured at calculation/checkout time to ensure pricing
     * consistency even if catalog prices change later. Optional; can be 0
     * until populated by the pricing/checkout flow.
     */
    public double snapshotUnitPrice;

    /** Epoch milliseconds when this line item was created (insertion order). */
    public long addedAtMillis;

    /**
     * Creates a new cart line item.
     *
     * @param productId product identifier
     * @param quantity positive quantity (&gt; 0)
     * @throws IllegalArgumentException if {@code quantity &lt;= 0}
     */
    public CartItem(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        this.productId = productId;
        this.quantity = quantity;
        this.addedAtMillis = System.currentTimeMillis();
    }
}
