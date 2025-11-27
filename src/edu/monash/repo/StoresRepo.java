package edu.monash.repo;

import java.io.*;
import java.util.*;
import edu.monash.domain.Store;

/**
 * Repository for loading and maintaining the in-memory store directory.
 *
 * <p><strong>CSV schema:</strong> {@code stores.csv} with header:
 * {@code id,name,address,hours,phone}</p>
 *
 * <p><strong>Lifecycle:</strong>
 * <ul>
 *   <li>{@link #ensure()} creates the file with seed data if absent.</li>
 *   <li>{@link #load()} reads all rows into {@link #stores} (linked for stable iteration order).</li>
 * </ul>
 *
 * <p>No write/update operations are provided; callers that modify {@link #stores}
 * directly are responsible for persistence if needed.</p>
 * @author Yujie Shao
 */
public class StoresRepo {

    /** In-memory map of storeId -> {@link Store}. */
    public Map<String, Store> stores = new LinkedHashMap<>();

    /** Backing CSV file (data/stores.csv). */
    private final File file;

    /**
     * Creates a repository that targets {@code stores.csv} under the given directory.
     *
     * @param dir base directory for data files
     */
    public StoresRepo(String dir) { this.file = new File(dir, "stores.csv"); }

    /**
     * Ensures the CSV exists and writes a header plus two seed stores if missing.
     *
     * @throws Exception if the file cannot be created
     */
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("id,name,address,hours,phone");
                pw.println("S1,Clayton Campus Store,21 College Walk,9am-5pm,+61 3 9905 0000");
                pw.println("S2,Caulfield Campus Store,24 Sir John Monash Dr,9am-5pm,+61 3 9903 0000");
            }
        }
    }

    /**
     * Loads all stores from {@code stores.csv} into the {@link #stores} map.
     *
     * <p>Assumes simple comma-separated fields; no CSV quoting is handled.
     * Each row must have at least {@code id,name,address,hours}; {@code phone} is optional.</p>
     *
     * @throws Exception if reading fails
     */
    public void load() throws Exception {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",", 5);
                stores.put(s[0], new Store(s[0], s[1], s[2], s[3], s.length>4?s[4]:""));
            }
        }
    }
}
