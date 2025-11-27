package edu.monash.repo;

import java.io.*;
import java.util.*;

/**
 * <p><strong>AccountsRepo — CSV-backed Account Balance Repository</strong></p>
 *
 * <p>Repository responsible for persisting and retrieving user account balances
 * from a simple CSV file (<code>accounts.csv</code>). Each record associates an email
 * with a balance value. Data is cached in-memory using a {@link HashMap} and synchronized
 * to disk through explicit calls to {@link #save()}.</p>
 *
 * <p>Typical usage lifecycle:
 * <ol>
 *   <li>{@link #ensure()} — create file if absent</li>
 *   <li>{@link #load()} — populate in-memory map</li>
 *   <li>{@link #save()} — persist changes</li>
 * </ol>
 * </p>
 *
 * <p>CSV schema: <code>email,balance</code></p>
 *
 * @author WeiChen
 * @version 1.0
 * @since 2025-10-16
 */
public class AccountsRepo {

    /** In-memory account balances mapped by user email. */
    public Map<String, Double> balance = new HashMap<>();

    /** Underlying CSV file used for persistence. */
    private final File file;

    /**
     * Constructs an {@code AccountsRepo} pointing to {@code accounts.csv}
     * within the specified directory.
     *
     * @param dir base directory containing (or to contain) {@code accounts.csv}.
     */
    public AccountsRepo(String dir) {
        this.file = new File(dir, "accounts.csv");
    }

    /**
     * Ensures the CSV file exists; creates it with default balances if missing.
     *
     * <p>Initial data includes three sample accounts:
     * <ul>
     *   <li>student@student.monash.edu — $1000</li>
     *   <li>staff@monash.edu — $1000</li>
     *   <li>admin@monash.edu — $1000</li>
     * </ul>
     * </p>
     *
     * @throws Exception if file or directory creation fails.
     */
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("email,balance");

                // Initial sample users with preset balances
                pw.println("student@student.monash.edu,1000");
                pw.println("staff@monash.edu,1000");
                pw.println("admin@monash.edu,1000");
            }
        }
    }

    /**
     * Loads account balances from the CSV file into the in-memory map.
     * <p>Skips malformed rows and ignores missing files.</p>
     *
     * @throws Exception if file reading fails.
     */
    public void load() throws Exception {
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",", 2);
                if (s.length < 2) continue;

                // Parse and store balance value
                balance.put(s[0], Double.parseDouble(s[1]));
            }
        }
    }

    /**
     * Persists the current in-memory balance map to the CSV file.
     * <p>Overwrites the entire file and writes a new header.</p>
     *
     * @throws Exception if writing fails.
     */
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("email,balance");

            // Write each entry as a line
            for (var e : balance.entrySet()) {
                pw.printf("%s,%.2f%n", e.getKey(), e.getValue());
            }
        }
    }
}
