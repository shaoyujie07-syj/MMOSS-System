package edu.monash.service;

import java.util.*;
import java.util.stream.Collectors;
import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;

/**
 * CatalogService provides read-only catalog operations such as listing,
 * keyword search, and multi-criteria filtering over products loaded
 * by {@link ProductsRepo}. All operations return new lists and never
 * mutate repository state.
 *
 * <p><strong>Sorting:</strong> All public query methods return results
 * sorted by (1) in-stock items first (stock &gt; 0), then (2) product
 * name ascending (case-insensitive).</p>
 *
 * <p><strong>Case-insensitivity:</strong> Search and filter comparisons
 * on textual fields are case-insensitive unless noted otherwise.</p>
 * @author Yujie Shao
 */
public class CatalogService {
    private final ProductsRepo products;

    /**
     * Creates a CatalogService bound to a given {@link ProductsRepo}.
     *
     * @param products non-null repository that holds all products
     */
    public CatalogService(ProductsRepo products){ this.products = products; }

    /**
     * Returns all products in the catalog.
     *
     * <p>Result is sorted with in-stock items first, then by name (A→Z).</p>
     *
     * @return a new list containing every product
     */
    public List<Product> listAll(){
        return sortByStock(new ArrayList<>(products.products.values()));
    }

    /**
     * Performs a case-insensitive keyword search over product name, brand,
     * category, and subcategory.
     *
     * <p>If {@code keyword} is null, it is treated as an empty string and
     * the method returns all products.</p>
     *
     * @param keyword term to match against name/brand/category/subcategory (case-insensitive)
     * @return matching products, sorted with in-stock items first then by name
     */
    public List<Product> search(String keyword){
        String k = keyword==null? "": keyword.toLowerCase();
        return sortByStock(products.products.values().stream().filter(p ->
                p.name.toLowerCase().contains(k) ||
                        (p.brand!=null && p.brand.toLowerCase().contains(k)) ||
                        (p.category!=null && p.category.toLowerCase().contains(k)) ||
                        (p.subcategory!=null && p.subcategory.toLowerCase().contains(k))
        ).collect(Collectors.toList()));
    }

    /**
     * Filters products by optional criteria and returns a sorted result.
     *
     * <ul>
     *   <li><strong>category</strong>: when non-blank, matches case-insensitively against {@code p.category}.</li>
     *   <li><strong>brand</strong>: when non-blank, matches case-insensitively against {@code p.brand}.</li>
     *   <li><strong>minPrice</strong>: when non-null, requires {@code p.price &gt;= minPrice}.</li>
     *   <li><strong>maxPrice</strong>: when non-null, requires {@code p.price &lt;= maxPrice}.</li>
     *   <li><strong>inStockOnly</strong>: when {@code Boolean.TRUE}, requires {@code p.stock &gt; 0}.</li>
     * </ul>
     *
     * <p>Blank strings for {@code category} or {@code brand} are ignored.
     * Null numeric bounds are ignored. When both price bounds are provided,
     * the product must satisfy the closed interval [minPrice, maxPrice].</p>
     *
     * @param category optional category filter (case-insensitive; blank ignored)
     * @param brand optional brand filter (case-insensitive; blank ignored)
     * @param minPrice optional inclusive lower bound on {@code price}; null to ignore
     * @param maxPrice optional inclusive upper bound on {@code price}; null to ignore
     * @param inStockOnly when {@code TRUE}, only include items with stock &gt; 0; null or FALSE includes all
     * @return filtered products, sorted with in-stock items first then by name
     */
    public List<Product> filter(String category, String brand, Double minPrice, Double maxPrice, Boolean inStockOnly){
        return sortByStock(products.products.values().stream().filter(p -> {
            if (category!=null && !category.isBlank() && (p.category==null || !category.equalsIgnoreCase(p.category))) return false;
            if (brand!=null && !brand.isBlank() && (p.brand==null || !brand.equalsIgnoreCase(p.brand))) return false;
            if (minPrice!=null && p.price < minPrice) return false;
            if (maxPrice!=null && p.price > maxPrice) return false;
            if (inStockOnly!=null && inStockOnly && p.stock<=0) return false;
            return true;
        }).collect(Collectors.toList()));
    }

    /**
     * Sorts a list in-place with in-stock items first, then by name (A→Z, case-insensitive),
     * and returns the same list for chaining.
     *
     * @param list list to sort (modified in-place)
     * @return the same list instance, sorted
     */
    private List<Product> sortByStock(List<Product> list){
        list.sort(Comparator.<Product>comparingInt(p -> p.stock<=0?1:0).thenComparing(p -> p.name.toLowerCase()));
        return list;
    }

    /**
     * Returns a product by its id (no copying).
     *
     * @param id product identifier
     * @return the product instance from the repository map, or {@code null} if not found
     */
    public Product getById(String id){ return products.products.get(id); }
}
