package edu.monash.service;

import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;

/**
 * <p><strong>AdminService — Advanced Product Management Service</strong></p>
 *
 * <p>This service enables administrators of the Monash Market Online Supermarket System (MMOSS)
 * to add, edit, and delete products from the {@link ProductsRepo}. It includes input validation,
 * category normalization, and business rules such as limiting the total number of distinct
 * product categories to 10.</p>
 *
 * <p>All repository modifications are persisted through {@link ProductsRepo#save()} calls.
 * The service ensures that product data integrity and category constraints are maintained
 * consistently across the system.</p>
 *
 * @author WeiChen
 * @version 1.1
 * @since 2025-10-16
 */
public class AdminService {

    /** Repository for accessing and modifying product records. */
    private final ProductsRepo products;

    /**
     * Constructs an {@code AdminService} with a reference to the product repository.
     *
     * @param products the repository used for product persistence.
     */
    public AdminService(ProductsRepo products) {
        this.products = products;
    }

    /**
     * Adds a new product to the repository with validation and category limit checks.
     *
     * <p>Ensures that the product fields are valid (non-null, non-negative prices, etc.)
     * and that the category count does not exceed 10 unique normalized categories.</p>
     *
     * @param p the {@link Product} to be added.
     * @return a result message describing the outcome (e.g., "OK", "Invalid product id",
     *         "Add failed: category limit is 10.").
     */
    public String addProduct(Product p) {
        // Input validation for required fields
        if (p == null || p.id == null || p.id.isBlank()) return "Invalid product id";
        if (p.name == null || p.name.isBlank()) return "Invalid name";
        if (p.category == null || p.category.isBlank()) return "Invalid category";
        if (p.price < 0 || p.memberPrice < 0) return "Invalid price";
        if (p.memberPrice > p.price) return "memberPrice cannot be greater than price";
        if (p.stock < 0) return "Invalid stock";
        if (products.products.containsKey(p.id)) return "Product already exists";

        // Normalize new category for comparison
        String newCatNorm = p.category.trim().toLowerCase();

        // Build a set of existing normalized categories
        java.util.Set<String> cats = new java.util.HashSet<>();
        for (var x : products.products.values()) {
            if (x.category == null) continue;
            String c = x.category.trim().toLowerCase();
            if (!c.isBlank()) cats.add(c);
        }

        // Ensure category limit does not exceed 10
        boolean introducesNewCategory = !newCatNorm.isBlank() && !cats.contains(newCatNorm);
        if (introducesNewCategory && cats.size() >= 10) {
            return "Add failed: category limit is 10.";
        }

        // Normalize and clean string fields
        p.id = p.id.trim();
        p.name = p.name.trim();
        p.category = p.category.trim();
        if (p.subcategory != null) p.subcategory = p.subcategory.trim();
        if (p.brand != null) p.brand = p.brand.trim();
        if (p.description != null) p.description = p.description.trim();
        if (p.expiry != null && p.expiry.isBlank()) p.expiry = null;
        if (p.ingredients != null && p.ingredients.isBlank()) p.ingredients = null;
        if (p.storage != null && p.storage.isBlank()) p.storage = null;
        if (p.allergens != null && p.allergens.isBlank()) p.allergens = null;

        // Insert and persist
        products.products.put(p.id, p);
        try {
            products.save();
        } catch (Exception ignored) {}
        return "OK";
    }

    /**
     * Edits an existing product while validating category changes and ensuring the
     * total number of categories does not exceed the allowed limit (10).
     *
     * @param p the updated {@link Product} to be persisted.
     * @return a message indicating success or the reason for failure.
     */
    public String editProduct(Product p) {
        if (p == null || p.id == null || p.id.isBlank()) return "Invalid product id";
        if (!products.products.containsKey(p.id)) return "Product not found";

        String newCat = (p.category == null ? "" : p.category.trim().toLowerCase());

        // Count existing categories
        java.util.Map<String, Integer> count = new java.util.HashMap<>();
        for (var x : products.products.values()) {
            if (x.category == null) continue;
            String c = x.category.trim().toLowerCase();
            if (c.isBlank()) continue;
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        int distinct = count.size();

        var old = products.products.get(p.id);
        String oldCat = (old.category == null ? "" : old.category.trim().toLowerCase());

        // Check if category change affects distinct count
        boolean categoryChanged = !oldCat.equals(newCat);
        if (categoryChanged) {
            boolean newCatIsNonBlank = !newCat.isBlank();
            boolean newCatIsNew = newCatIsNonBlank && !count.containsKey(newCat);
            boolean oldCatNonBlank = !oldCat.isBlank();
            boolean oldCatWouldDisappear = oldCatNonBlank && count.getOrDefault(oldCat, 0) == 1;

            int projectedDistinct = distinct
                    + (newCatIsNew ? 1 : 0)
                    - (oldCatWouldDisappear ? 1 : 0);

            if (projectedDistinct > 10) {
                return "Edit failed: category limit is 10.";
            }
        }

        // Persist updated product
        products.products.put(p.id, p);
        try {
            products.save();
        } catch (Exception ignored) {}
        return "OK";
    }

    /**
     * Deletes a product by its ID and persists the repository state.
     *
     * @param id the unique identifier of the product to be deleted.
     * @return a result message ("OK" or "Product not found").
     */
    public String deleteProduct(String id) {
        if (products.products.remove(id) == null) return "Product not found";
        try {
            products.save();
        } catch (Exception ignored) {}
        return "OK";
    }

    /**
     * Counts the number of products in each normalized (trimmed and lower-cased) category.
     *
     * @return a mapping of category names to product counts.
     */
    private java.util.Map<String, Integer> categoryCounts() {
        java.util.Map<String, Integer> m = new java.util.HashMap<>();
        for (var p : products.products.values()) {
            String c = p.category == null ? "" : p.category.trim().toLowerCase();
            if (c.isEmpty()) continue;
            m.put(c, m.getOrDefault(c, 0) + 1);
        }
        return m;
    }

    /**
     * Normalizes a category name by trimming whitespace and converting to lowercase.
     *
     * @param c the category string to normalize.
     * @return the normalized category string (never {@code null}).
     */
    private String normCat(String c) {
        return (c == null ? "" : c.trim().toLowerCase());
    }
}
