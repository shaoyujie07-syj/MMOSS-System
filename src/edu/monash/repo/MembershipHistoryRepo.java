package edu.monash.repo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for persisting and querying membership activity history.
 *
 * <p><strong>CSV schema:</strong> {@code membership_history.csv} with header
 * {@code email,action,years,amount,date} where:</p>
 * <ul>
 *   <li><code>email</code>  — user identifier</li>
 *   <li><code>action</code> — one of PURCHASE, RENEW, CANCEL</li>
 *   <li><code>years</code>  — number of years affected (usually 1)</li>
 *   <li><code>amount</code> — charge applied for the action</li>
 *   <li><code>date</code>   — ISO date (YYYY-MM-DD)</li>
 * </ul>
 *
 * <p>Writes are append-only. Callers manage higher-level consistency.</p>
 * @author Yujie Shao
 */
public class MembershipHistoryRepo {
    private final File file;

    /**
     * Immutable row model representing one history record.
     */
    public static class Row {
        public final String email;
        public final String action;   // PURCHASE | RENEW | CANCEL
        public final int years;       // usually 1 for purchase/renew
        public final double amount;   // fee charged
        public final LocalDate date;  // yyyy-MM-dd

        public Row(String email, String action, int years, double amount, LocalDate date) {
            this.email = email; this.action = action; this.years = years; this.amount = amount; this.date = date;
        }
    }

    /**
     * Creates a repository targeting {@code membership_history.csv} under {@code dataDir}.
     *
     * @param dataDir base directory for data files
     */
    public MembershipHistoryRepo(String dataDir) {
        this.file = new File(dataDir, "membership_history.csv");
    }

    /**
     * Ensures the CSV file exists and has a header row.
     *
     * @throws IOException if the file cannot be created
     */
    public void ensure() throws IOException {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            try (var w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                w.println("email,action,years,amount,date");
            }
        }
    }

    /**
     * Appends a single membership history record.
     *
     * @param email  user identifier
     * @param action action name (PURCHASE, RENEW, CANCEL)
     * @param years  number of years affected
     * @param amount amount charged
     * @param date   action date
     */
    public synchronized void append(String email, String action, int years, double amount, LocalDate date) {
        try (var w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            w.printf("%s,%s,%d,%.2f,%s%n",
                    safe(email), safe(action), years, amount, date.toString());
        } catch (IOException ignore) {}
    }

    /**
     * Returns all history records for the given email.
     *
     * @param email user identifier
     * @return list of rows ordered by file insertion order
     */
    public List<Row> listByEmail(String email) {
        List<Row> out = new ArrayList<>();
        try (var br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line; boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] a = line.split(",", -1);
                if (a.length < 5) continue;
                if (!email.equalsIgnoreCase(a[0])) continue;
                String action = a[1];
                int years = parseInt(a[2]);
                double amount = parseDouble(a[3]);
                LocalDate date = parseDate(a[4]);
                out.add(new Row(a[0], action, years, amount, date));
            }
        } catch (IOException ignore) {}
        return out;
    }

    // Helpers

    private static String safe(String s){ return s==null? "" : s.replace(",", " "); }
    private static int parseInt(String s){ try{ return Integer.parseInt(s.trim()); }catch(Exception e){ return 0; } }
    private static double parseDouble(String s){ try{ return Double.parseDouble(s.trim()); }catch(Exception e){ return 0.0; } }
    private static LocalDate parseDate(String s){ try{ return LocalDate.parse(s.trim()); }catch(Exception e){ return LocalDate.now(); } }
}
