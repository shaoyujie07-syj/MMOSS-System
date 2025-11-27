package edu.monash.repo;

import java.io.*;
import java.util.*;

/**
 * <p><strong>UsersRepo — CSV-backed User Repository</strong></p>
 *
 * <p>Repository for loading, persisting and querying user records from a CSV file.
 * Data is held in-memory via a {@link HashMap} keyed by email. The CSV schema is:</p>
 *
 * <p><code>email,password,firstName,lastName,mobile,role</code></p>
 *
 * <p>Typical lifecycle: {@link #ensure()} → {@link #load()} → (mutations) → {@link #save()}.</p>
 *
 * <p><em>Notes:</em> Validation is intentionally light at the repository layer; higher-level
 * services enforce business rules. Helper methods (e.g., {@link #idx(String[], String)})
 * are kept private within the repo.</p>
 *
 * @author WeiChen
 * @version 1.0
 * @since 2025-10-16
 */
public class UsersRepo {

    /**
     * <p><strong>Row — User Record DTO</strong></p>
     *
     * <p>Simple data holder for a user row parsed from CSV. This DTO mirrors the CSV columns
     * and is stored in-memory inside {@link UsersRepo#users}.</p>
     */
    public static class Row {
        /** Unique email identifier. */
        public String email;
        /** Plain-text password as stored in CSV (demo only; not secure). */
        public String password;
        /** Role string (e.g., CUSTOMER, ADMIN). */
        public String role;
        /** Profile first name. */
        public String firstName;
        /** Profile last name. */
        public String lastName;
        /** Mobile phone number. */
        public String mobile;

        /**
         * Minimal constructor.
         *
         * @param e email
         * @param p password
         * @param r role
         */
        public Row(String e, String p, String r) { email = e; password = p; role = r; }

        /**
         * Full constructor mirroring CSV columns.
         *
         * @param e  email
         * @param p  password
         * @param fn first name
         * @param ln last name
         * @param mb mobile
         * @param r  role
         */
        public Row(String e, String p, String fn, String ln, String mb, String r) {
            this.email = e; this.password = p; this.firstName = fn; this.lastName = ln; this.mobile = mb; this.role = r;
        }
    }

    /** In-memory user map keyed by email. */
    public Map<String, Row> users = new HashMap<>();

    /** Underlying CSV file handle. */
    private final File file;

    /**
     * Constructs a {@code UsersRepo} pointing to {@code users.csv} in the given directory.
     *
     * @param dir base directory containing (or to contain) {@code users.csv}.
     */
    public UsersRepo(String dir) { this.file = new File(dir, "users.csv"); }

    /**
     * Ensures the CSV exists; if missing, creates it with a header and sample rows.
     *
     * @throws Exception if file or directory creation fails.
     */
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                // header
                pw.println("email,password,firstName,lastName,mobile,role");

                // sample users
                pw.println("student@student.monash.edu,Monash1234!,Student,User,0400000001,CUSTOMER");
                pw.println("staff@monash.edu,Monash1234!,Staff,User,0400000002,CUSTOMER");
                pw.println("admin@monash.edu,Admin1234!,Admin,User,0400000000,ADMIN");
            }
        }
    }

    /**
     * Loads users from CSV into {@link #users}. Missing CSV is tolerated (noop).
     *
     * @throws Exception if reading fails.
     */
    public void load() throws Exception {
        users.clear();
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) return;

            // Compute column indexes (case-insensitive)
            String[] cols = header.split(",", -1);
            int iEmail  = idx(cols, "email");
            int iPwd    = idx(cols, "password");
            int iRole   = idx(cols, "role");
            int iFN     = idx(cols, "firstname");
            int iLN     = idx(cols, "lastname");
            int iMobile = idx(cols, "mobile");

            String line;
            while ((line = br.readLine()) != null) {
                String[] a = line.split(",", -1);
                // Guard against malformed rows or missing mandatory columns
                if (iEmail < 0 || iPwd < 0 || iRole < 0 || a.length < cols.length) continue;

                String email = a[iEmail];
                String pwd   = a[iPwd];
                String role  = a[iRole];
                String fn    = (iFN     >= 0 ? a[iFN]     : "");
                String ln    = (iLN     >= 0 ? a[iLN]     : "");
                String mb    = (iMobile >= 0 ? a[iMobile] : "");

                Row r = new Row(email, pwd, fn, ln, mb, role);
                users.put(email, r);
            }
        }
    }

    /**
     * Persists the current {@link #users} map to CSV, overwriting any existing content.
     *
     * @throws Exception if writing fails.
     */
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            // header
            pw.println("email,password,firstName,lastName,mobile,role");

            // rows
            for (Row r : users.values()) {
                pw.printf("%s,%s,%s,%s,%s,%s%n",
                        nz(r.email), nz(r.password), nz(r.firstName), nz(r.lastName), nz(r.mobile), nz(r.role));
            }
        }
    }

    /**
     * Returns an empty string when input is {@code null}; otherwise returns the input as-is.
     *
     * @param s input string
     * @return non-null string
     */
    private static String nz(String s) { return (s == null ? "" : s); }

    /**
     * <p><strong>AdminProfile — Read-only Admin View</strong></p>
     *
     * <p>Immutable projection for displaying an admin profile (email/password/name/mobile).
     * This is returned by {@link #getAdminProfileByEmail(String)} when a matching record is found.</p>
     */
    public static class AdminProfile {
        /** Admin email. */      public final String email;
        /** Admin password. */   public final String password;
        /** Admin first name. */ public final String firstName;
        /** Admin last name. */  public final String lastName;
        /** Admin mobile. */     public final String mobile;

        /**
         * Constructs an immutable admin profile view.
         *
         * @param email     email
         * @param password  password
         * @param firstName first name
         * @param lastName  last name
         * @param mobile    mobile number
         */
        public AdminProfile(String email, String password, String firstName, String lastName, String mobile) {
            this.email = email; this.password = password; this.firstName = firstName; this.lastName = lastName; this.mobile = mobile;
        }

        /** Human-readable multi-line summary (for console display). */
        @Override public String toString() {
            return "Email: " + n(email) + "\n"
                    + "Password: " + n(password) + "\n"
                    + "First Name: " + n(firstName) + "\n"
                    + "Last  Name: " + n(lastName) + "\n"
                    + "Mobile: " + n(mobile);
        }

        /** Null/blank helper for {@link #toString()}. */
        private static String n(String s) { return (s == null || s.isBlank()) ? "(N/A)" : s; }
    }

    /**
     * Scans the CSV file and returns an {@link AdminProfile} for the specified email if present.
     * <p>Note: The current implementation reads directly from disk (not from {@link #users}) and
     * includes a role check block that does not alter control flow if the role is not ADMIN.
     * This behavior is preserved intentionally (no logic changes).</p>
     *
     * @param email target admin email (case-insensitive comparison).
     * @return {@link AdminProfile} if found; otherwise {@code null}.
     */
    public AdminProfile getAdminProfileByEmail(String email) {
        java.io.File f = this.file;
        try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(
                new java.io.FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return null;

            String[] cols = header.split(",", -1);
            int iEmail = idx(cols, "email"), iPwd = idx(cols, "password"),
                    iFn = idx(cols, "firstname"), iLn = idx(cols, "lastname"),
                    iMobile = idx(cols, "mobile"), iRole = idx(cols, "role");

            String line;
            while ((line = br.readLine()) != null) {
                String[] a = line.split(",", -1);
                if (a.length < cols.length) continue;

                // match email (case-insensitive)
                if (!a[iEmail].equalsIgnoreCase(email)) continue;

                // role check block intentionally preserved (no-op if not ADMIN)
                if (iRole >= 0 && !equalsIgnoreCase(a[iRole], "admin")) {
                }

                String pwd = (iPwd >= 0 ? a[iPwd] : "");
                String fn  = (iFn  >= 0 ? a[iFn]  : "");
                String ln  = (iLn  >= 0 ? a[iLn]  : "");
                String mb  = (iMobile >= 0 ? a[iMobile] : "");

                return new AdminProfile(a[iEmail], pwd, fn, ln, mb);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ------------------------------ helpers ------------------------------

    /**
     * Returns the index of a column in the header array by name (case-insensitive), or -1 if absent.
     *
     * @param cols header array
     * @param want column name to find
     * @return index or -1
     */
    private static int idx(String[] cols, String want) {
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].trim().equalsIgnoreCase(want)) return i;
        }
        return -1;
    }

    /**
     * Safe equalsIgnoreCase: returns {@code true} only when {@code a} is non-null and equalsIgnoreCase to {@code b}.
     *
     * @param a left string (must be non-null to return true)
     * @param b right string
     * @return equality ignoring case, with null-safety
     */
    private static boolean equalsIgnoreCase(String a, String b) { return a != null && a.equalsIgnoreCase(b); }
}
