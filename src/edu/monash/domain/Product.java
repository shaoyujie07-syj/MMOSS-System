package edu.monash.domain;

/**
 * Domain model representing a product in the catalog.
 *
 * <p>Each product includes core merchandising attributes (name, category,
 * brand, description), pricing (regular and member price), inventory (stock),
 * and optional food-related metadata (expiry, ingredients, storage, allergens).
 *
 * <p>Basic invariants are enforced at construction time via {@link #validate()}:
 * <ul>
 *   <li>{@code id} and {@code name} must be non-blank.</li>
 *   <li>{@code price} and {@code memberPrice} must be {@code >= 0}.</li>
 *   <li>{@code stock} must be {@code >= 0}.</li>
 * </ul>
 * @author Yujie Shao
 */
public class Product {
    /** Unique product identifier. */
    public String id;
    /** Display name. */
    public String name;
    /** Top-level category (case-insensitive semantics elsewhere). */
    public String category;
    /** Optional subcategory. */
    public String subcategory;
    /** Brand or manufacturer. */
    public String brand;
    /** Short marketing or descriptive text. */
    public String description;
    /** Regular unit price. */
    public double price;
    /** Member-only unit price (must be {@code >= 0}; can be lower than {@link #price}). */
    public double memberPrice;
    /** On-hand inventory units (must be {@code >= 0}). */
    public int stock;

    /** Optional: best-before / expiry date in ISO-8601 string (YYYY-MM-DD) or null. */
    public String expiry;
    /** Optional: list of ingredients (free text). */
    public String ingredients;
    /** Optional: storage instructions. */
    public String storage;
    /** Optional: allergen information. */
    public String allergens;

    /**
     * Constructs a product and validates basic invariants.
     *
     * @param id unique identifier (non-blank)
     * @param name display name (non-blank)
     * @param category top-level category (can be blank)
     * @param subcategory optional subcategory
     * @param brand brand or manufacturer
     * @param description marketing/description text
     * @param price regular unit price (must be {@code >= 0})
     * @param memberPrice member unit price (must be {@code >= 0})
     * @param stock available units (must be {@code >= 0})
     * @param expiry optional ISO date string (YYYY-MM-DD) or null
     * @param ingredients optional ingredients text
     * @param storage optional storage instructions
     * @param allergens optional allergen information
     * @throws IllegalArgumentException if required fields are missing or numeric invariants fail
     */
    public Product(String id, String name, String category, String subcategory, String brand, String description,
                   double price, double memberPrice, int stock,
                   String expiry, String ingredients, String storage, String allergens) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.memberPrice = memberPrice;
        this.stock = stock;
        this.expiry = expiry;
        this.ingredients = ingredients;
        this.storage = storage;
        this.allergens = allergens;
        validate();
    }

    /**
     * Verifies constructor invariants for id/name, pricing, and stock.
     *
     * @throws IllegalArgumentException if any invariant is violated
     */
    private void validate() throws IllegalArgumentException {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product id and name are required");
        }
        if (price < 0 || memberPrice < 0) throw new IllegalArgumentException("Price must be >= 0");
        if (stock < 0) throw new IllegalArgumentException("Stock must be >= 0");
    }

    /**
     * Adjusts on-hand stock by {@code delta}.
     *
     * <p>Negative deltas reduce stock and cannot drive stock below zero.
     *
     * @param delta increment (positive) or decrement (negative)
     * @throws IllegalStateException if the resulting stock would be negative
     */
    public void adjustStock(int delta) {
        int next = this.stock + delta;
        if (next < 0) throw new IllegalStateException("Insufficient inventory");
        this.stock = next;
    }
}
