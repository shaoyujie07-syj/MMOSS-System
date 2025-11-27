package edu.monash.domain;

import java.util.*;

/**
 * In-memory shopping cart for a single user.
 *
 * <p><strong>Capacity rules:</strong>
 * <ul>
 *   <li>{@link #MAX_ITEMS}: maximum total quantity across the entire cart (sum of all line quantities).</li>
 *   <li>{@link #MAX_QTY_PER_ITEM}: maximum quantity allowed per single product line.</li>
 * </ul>
 *
 * <p>Items are returned sorted by their {@code addedAtMillis} (ascending) to preserve insertion order.
 * This class is not thread-safe.</p>
 * @author Yujie Shao
 */
public class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    /** Maximum total quantity across all items in the cart. */
    public static final int MAX_ITEMS = 20;
    /** Maximum quantity per single product line. */
    public static final int MAX_QTY_PER_ITEM = 10;

    /**
     * Returns a view of cart items sorted by insertion time (ascending).
     *
     * @return list of {@link CartItem} sorted by {@code addedAtMillis}
     */
    public List<CartItem> getItems() {
        items.sort(Comparator.comparingLong(i -> i.addedAtMillis));
        return items;
    }

    /**
     * Adds a product to the cart or merges with an existing line.
     *
     * <p>Validation and effects:</p>
     * <ul>
     *   <li>Quantity must be positive.</li>
     *   <li>The sum of quantities across the entire cart must not exceed {@link #MAX_ITEMS}.</li>
     *   <li>Per-line quantity must not exceed {@link #MAX_QTY_PER_ITEM}.</li>
     *   <li>If the product already exists in the cart, its quantity is increased.</li>
     *   <li>If it does not exist, a new line is created.</li>
     * </ul>
     *
     * @param productId product identifier
     * @param qty quantity to add (must be &gt; 0)
     * @return "OK" on success, otherwise a human-readable error message
     */
    public String addOrMerge(String productId, int qty) {
        if (qty <= 0) return "Please enter a positive quantity";

        int totalQty = 0;
        for (CartItem i : items) {
            totalQty += i.quantity;
        }

        if (totalQty + qty > MAX_ITEMS)
            return "Cart can hold at most 20 items in total";

        // Try to merge with an existing line
        for (CartItem i : items) {
            if (i.productId.equals(productId)) {
                if (i.quantity + qty > MAX_QTY_PER_ITEM)
                    return "Max 10 units per product";
                i.quantity += qty;
                return "OK";
            }
        }

        // Create a new line
        if (qty > MAX_QTY_PER_ITEM)
            return "Max 10 units per product";
        items.add(new CartItem(productId, qty));
        return "OK";
    }

    /**
     * Removes a product line from the cart, if present.
     *
     * @param productId product identifier
     * @return {@code true} if a line was removed, {@code false} otherwise
     */
    public boolean remove(String productId) {
        return items.removeIf(i -> i.productId.equals(productId));
    }

    /**
     * Clears all items from the cart.
     */
    public void clear() { items.clear(); }

}
