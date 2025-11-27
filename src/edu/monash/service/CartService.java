package edu.monash.service;

import edu.monash.domain.ShoppingCart;
import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;
import java.util.*;

/**
 * Service layer for session-scoped shopping carts keyed by customer email.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Create or fetch a user's in-memory {@link ShoppingCart}.</li>
 *   <li>Add items, edit quantities, remove items, and clear a cart.</li>
 *   <li>Apply basic constraints when editing quantities:
 *       <ul>
 *         <li>Per-item quantity is clamped to a maximum of 10.</li>
 *         <li>Total distinct product lines capped at 20.</li>
 *         <li>Stock availability is enforced for the requested quantity.</li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * Note: Carts are kept in-memory and are cleared on logout or app termination
 * (i.e., they are session-based and not persisted).
 * @author Yujie Shao
 */
public class CartService {
    private final ProductsRepo products;
    private final Map<String, ShoppingCart> carts = new HashMap<>(); // email -> cart

    /**
     * Creates a CartService bound to a product repository.
     *
     * @param products repository containing product definitions and stock
     */
    public CartService(ProductsRepo products){ this.products = products; }

    /**
     * Returns the cart for the given user, creating it if absent.
     *
     * @param email user identifier
     * @return the existing or newly created {@link ShoppingCart}
     */
    public ShoppingCart get(String email){
        return carts.computeIfAbsent(email, k -> new ShoppingCart());
    }

    /**
     * Adds a product to the user's cart, merging with an existing line if present.
     * Quantity validation and line merging are delegated to {@link ShoppingCart#addOrMerge(String, int)}.
     *
     * @param email user identifier
     * @param productId product to add
     * @param qty desired quantity delta (can be &gt; 0)
     * @return status message ("OK" or error)
     */
    public String add(String email, String productId, int qty){
        Product p = products.products.get(productId);
        if (p == null) return "Product not found";
        var cart = get(email);
        return cart.addOrMerge(productId, qty);
    }

    /**
     * Edits the absolute quantity of a product line in the user's cart.
     * <ul>
     *   <li>If {@code newQty &lt;= 0}, behaves like a remove.</li>
     *   <li>Quantity is clamped to a maximum of 10 per item.</li>
     *   <li>Stock is checked against {@code newQty}.</li>
     *   <li>If the line does not exist and {@code newQty &gt; 0}, a new line is created
     *       subject to a 20-line cart limit.</li>
     * </ul>
     *
     * @param email user identifier
     * @param productId product to edit
     * @param newQty new absolute quantity (0 removes the line)
     * @return status message ("Updated: ..." / "Removed" / error)
     */
    public String editQuantity(String email, String productId, int newQty) {
        var cart = get(email);
        var p = products.products.get(productId);
        if (p == null) return "Product not found";

        if (newQty <= 0) {
            boolean removed = remove(email, productId);
            return removed ? "Removed" : "Not found";
        }

        if (newQty > 10) newQty = 10;

        if (newQty > p.stock) return "Insufficient stock (" + p.stock + " available)";

        edu.monash.domain.CartItem target = null;
        for (var it : cart.getItems()) {
            if (productId.equals(it.productId)) { target = it; break; }
        }

        if (target == null) {
            if (cart.getItems().size() >= 20) return "Cart item limit reached (max 20 items)";
            target = new edu.monash.domain.CartItem(productId, newQty);
            cart.getItems().add(target);
        } else {
            target.quantity = newQty;
        }
        return "Updated: " + productId + " -> " + newQty;
    }

    /**
     * Removes a product line from the user's cart.
     *
     * @param email user identifier
     * @param productId product to remove
     * @return {@code true} if the line existed and was removed; otherwise {@code false}
     */
    public boolean remove(String email, String productId){
        return get(email).remove(productId);
    }

    /**
     * Clears all items from the user's cart.
     *
     * @param email user identifier
     */
    public void clear(String email){ get(email).clear(); }
}
