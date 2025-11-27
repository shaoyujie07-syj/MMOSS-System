package edu.monash.repo;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Repository for membership records persisted in a CSV file.
 *
 * <p><strong>Schema:</strong> {@code memberships.csv} with header:
 * {@code email,type,startDate,endDate,status}</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Ensure the CSV file exists with the correct header</li>
 *   <li>Load all rows into an in-memory map keyed by email</li>
 *   <li>Save the in-memory map back to CSV</li>
 * </ul>
 *
 * <p>Date fields are serialized in ISO-8601 (YYYY-MM-DD). Blank dates are treated as {@code null}.</p>
 * @author Yujie Shao
 */
public class MembershipsRepo {

    /**
     * POJO representing one membership row.
     */
    public static class Row {
        public String email;
        public String type;       // e.g., "VIP"
        public LocalDate startDate;
        public LocalDate endDate;
        public String status;     // e.g., "ACTIVE", "CANCELLED"

        public Row(String email, String type, LocalDate startDate, LocalDate endDate, String status){
            this.email = email;
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }
    }

    /** In-memory index of memberships keyed by email. */
    public Map<String, Row> membership = new HashMap<>();

    /** Backing file {@code data/memberships.csv}. */
    private final File file;

    /**
     * Creates a repository targeting {@code memberships.csv} in the given directory.
     *
     * @param dir base directory for data files
     */
    public MembershipsRepo(String dir){ this.file = new File(dir, "memberships.csv"); }

    /**
     * Ensures the CSV exists and writes the header if missing.
     *
     * @throws Exception if the file cannot be created or written
     */
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("email,type,startDate,endDate,status");
            }
        }
    }

    /**
     * Loads all membership rows into memory.
     *
     * <p>Assumes simple comma-separated fields with no quoting.
     * Blank date fields are parsed as {@code null}.</p>
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
                LocalDate sdate = (s[2].isBlank() ? null : LocalDate.parse(s[2]));
                LocalDate edate = (s[3].isBlank() ? null : LocalDate.parse(s[3]));
                membership.put(s[0], new Row(s[0], s[1], sdate, edate, s[4]));
            }
        }
    }

    /**
     * Saves all in-memory records to the CSV.
     *
     * @throws Exception if writing fails
     */
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("email,type,startDate,endDate,status");
            for (var r : membership.values()) {
                pw.printf("%s,%s,%s,%s,%s%n",
                        r.email,
                        r.type,
                        r.startDate == null ? "" : r.startDate.toString(),
                        r.endDate == null ? "" : r.endDate.toString(),
                        r.status);
            }
        }
    }
}
