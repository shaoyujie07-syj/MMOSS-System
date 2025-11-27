package edu.monash.repo;

import java.io.*;
import java.util.*;
import edu.monash.domain.Product;

/**
 * <p><strong>ProductsRepo — CSV-backed Product Repository</strong></p>
 *
 * <p>Repository responsible for persisting and retrieving {@link Product} data
 * from a CSV file. It uses an in-memory {@link LinkedHashMap} to preserve
 * insertion order, while providing simple <em>ensure → load → save</em> lifecycle
 * operations for the Monash Market Online Supermarket System (MMOSS).</p>
 *
 * <p>CSV schema (13 columns):<br/>
 * <code>id,name,category,subcategory,brand,description,price,memberPrice,stock,expiry,ingredients,storage,allergens</code></p>
 *
 * <p><em>Notes:</em>
 * <ul>
 *   <li>This class intentionally performs minimal validation; higher-level services enforce rules.</li>
 *   <li>Fields that are blank may be written as empty strings; quoting/escaping is delegated to {@code CsvUtil}.</li>
 * </ul>
 * </p>
 *
 * @author WeiChen
 * @version 1.0
 * @since 2025-10-16
 */
public class ProductsRepo {

    /** In-memory product map keyed by product id; preserves insertion order. */
    public Map<String, Product> products = new LinkedHashMap<>();

    /** Underlying CSV file for persistence. */
    private final File file;

    /**
     * Constructs the repository, pointing to {@code products.csv} under the provided directory.
     *
     * @param dir directory that contains (or will contain) {@code products.csv}.
     */
    public ProductsRepo(String dir) {
        this.file = new File(dir, "products.csv");
    }

    /**
     * Ensures the CSV exists. If missing, creates directories and writes a header with two sample rows.
     *
     * @throws Exception if file or directory creation fails.
     */
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                // header
                pw.println("id,name,category,subcategory,brand,description,price,memberPrice,stock,expiry,ingredients,storage,allergens");

                // sample rows (kept minimal for demo/bootstrapping)
                pw.println("P1001,Apple iPhone,Electronics,Phone,Apple,Smart phone,1299.0,1199.0,5,,,,");
                pw.println("P2001,Almond Milk,Food,Beverages,Monash,1L almond milk,3.5,3.0,10,2026-12-31,Almonds;Water,Keep refrigerated,Tree nuts");
            }
        }
    }

    /**
     * Loads all products from the CSV file into memory. Skips malformed lines.
     *
     * @throws Exception if file access fails.
     */
    public void load() throws Exception {
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = split(line);
                if (s.length < 13) continue; // guard: malformed row

                Product p = new Product(
                        s[0], s[1], s[2], s[3], s[4], s[5],
                        Double.parseDouble(s[6]), Double.parseDouble(s[7]), Integer.parseInt(s[8]),
                        s[9], s[10], s[11], s[12]
                );
                products.put(p.id, p);
            }
        }
    }

    /**
     * Writes the current in-memory product map to the CSV file, overwriting existing content.
     *
     * @throws Exception if file writing fails.
     */
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            // header
            pw.println("id,name,category,subcategory,brand,description,price,memberPrice,stock,expiry,ingredients,storage,allergens");

            // rows
            for (Product p : products.values()) {
                pw.printf(
                        "%s,%s,%s,%s,%s,%s,%.2f,%.2f,%d,%s,%s,%s,%s%n",
                        CsvUtil.esc(p.id), CsvUtil.esc(p.name), CsvUtil.esc(p.category), CsvUtil.esc(p.subcategory),
                        CsvUtil.esc(p.brand), CsvUtil.esc(p.description),
                        p.price, p.memberPrice, p.stock,
                        CsvUtil.esc(p.expiry), CsvUtil.esc(p.ingredients), CsvUtil.esc(p.storage), CsvUtil.esc(p.allergens)
                );
            }
        }
    }

    /**
     * Splits a single CSV line into fields, handling quoted sections that may include commas.
     * This is a minimal parser tailored for the repository's needs.
     *
     * @param line a raw CSV line (no trailing newline).
     * @return an array of column values (unescaped).
     */
    private static String[] split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean q = false; // inside-quote flag

        // Walk through characters, toggling quote state and splitting on commas when not quoted
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') { // toggle quoting
                q = !q;
                continue;
            }

            if (c == ',' && !q) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        // append last token
        out.add(sb.toString());

        return out.toArray(new String[0]);
    }
}
