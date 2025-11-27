package edu.monash.repo;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import edu.monash.domain.PromoCode;

/**
 * Repository for promotional codes backed by a CSV file.
 *
 * <p><strong>CSV schema:</strong> {@code promos.csv} with header:
 * {@code code,percent,scope,expiry}</p>
 * <ul>
 *   <li><code>code</code>   — unique promo identifier (case-insensitive)</li>
 *   <li><code>percent</code>— integer discount percent (e.g., 10, 15, 20)</li>
 *   <li><code>scope</code>  — arbitrary scope tag (e.g., "ALL", "PICKUP")</li>
 *   <li><code>expiry</code> — ISO-8601 date (YYYY-MM-DD); blank = no expiry</li>
 * </ul>
 *
 * <p>On load, codes are normalized to uppercase keys in {@link #promos}.</p>
 *
 * <p>Note: The {@link PromoCode} domain object is expected to hold the parsed
 * values and may expose helper methods such as {@code isExpired()} based on
 * the stored expiry date.</p>
 * @author Yujie Shao
 */
public class PromosRepo {

    /** In-memory index of promo code (uppercased) -> {@link PromoCode}. */
    public Map<String, PromoCode> promos = new HashMap<>();

    /** Backing CSV file (e.g., data/promos.csv). */
    private final File file;

    /**
     * Creates a repository targeting {@code promos.csv} under the given directory.
     *
     * @param dir base directory for data files
     */
    public PromosRepo(String dir){ this.file = new File(dir, "promos.csv"); }

    /**
     * Ensures the CSV exists and writes a header plus a few seed promos if missing.
     *
     * @throws Exception if the file cannot be created or written
     */
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("code,percent,scope,expiry");
                pw.println("PROMO10,10,ALL,2099-12-31");
                pw.println("FIRST_PICKUP,15,PICKUP,2099-12-31");
                pw.println("NEWMONASH20,20,PICKUP,2099-12-31");
            }
        }
    }

    /**
     * Loads all promos from CSV into {@link #promos}.
     *
     * <p>Assumes simple comma-separated fields without quoting. Blank or missing
     * expiry is parsed as {@code null}.</p>
     *
     * @throws Exception if reading fails
     */
    public void load() throws Exception {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while((line=br.readLine())!=null){
                String[] s = line.split(",", 4);
                LocalDate exp = (s.length>3 && !s[3].isBlank()) ? LocalDate.parse(s[3]) : null;
                promos.put(s[0].toUpperCase(), new PromoCode(s[0], Integer.parseInt(s[1]), s[2], exp));
            }
        }
    }
}
