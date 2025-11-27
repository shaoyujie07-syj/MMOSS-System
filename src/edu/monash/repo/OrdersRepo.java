package edu.monash.repo;

import java.io.*;
import java.util.*;
import edu.monash.domain.CartItem;

/**
 * <p><strong>OrdersRepo — CSV-backed Order Repository</strong></p>
 *
 * <p>Repository responsible for persisting order headers and order items in two separate CSV files:
 * <ul>
 *   <li><b>orders.csv</b> — high-level order information</li>
 *   <li><b>order_items.csv</b> — associated product IDs and quantities</li>
 * </ul>
 * The repository provides lifecycle operations for ensuring, appending, and reading orders.</p>
 *
 * <p>Files are stored under a configurable directory (typically <code>data/</code>).
 * The implementation is deliberately simple and non-transactional, suitable for
 * coursework or demonstration systems like MMOSS (Monash Market Online Supermarket System).</p>
 *
 * @author WeiChen
 * @version 1.0
 * @since 2025-10-16
 */
public class OrdersRepo {

    /** File storing order headers. */
    private final File orders;

    /** File storing individual order items. */
    private final File items;

    /**
     * Constructs an {@code OrdersRepo} that manages {@code orders.csv} and {@code order_items.csv}.
     *
     * @param dir the directory path containing (or to contain) order data files.
     */
    public OrdersRepo(String dir) {
        this.orders = new File(dir, "orders.csv");
        this.items = new File(dir, "order_items.csv");
    }

    /**
     * Ensures that both {@code orders.csv} and {@code order_items.csv} exist.
     * If missing, they are created with appropriate headers.
     *
     * @throws Exception if file creation fails.
     */
    public void ensure() throws Exception {
        if (!orders.exists()) {
            orders.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(orders)) {
                pw.println("orderId,email,fulfilment,where,promo,subtotal,discount,fee,total");
            }
        }
        if (!items.exists()) {
            items.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(items)) {
                pw.println("orderId,productId,qty");
            }
        }
    }

    /**
     * Appends a new order and its associated items to the CSV files.
     * <p>Generates a unique order ID based on system time.</p>
     *
     * @param email       the customer's email.
     * @param fulfilment  fulfilment method ("PICKUP" or "DELIVERY").
     * @param where       pickup location or delivery address.
     * @param promo       promo code (nullable).
     * @param subtotal    pre-discount total.
     * @param discount    discount amount.
     * @param fee         service/delivery fee.
     * @param total       final total after all adjustments.
     * @param cartItems   list of {@link CartItem} included in the order.
     * @return generated order ID.
     * @throws Exception if file writing fails.
     */
    public String appendOrder(String email, String fulfilment, String where, String promo,
                              double subtotal, double discount, double fee, double total,
                              List<CartItem> cartItems) throws Exception {

        // Generate unique order ID (millisecond timestamp)
        String id = "O" + System.currentTimeMillis();

        // Write order header
        try (FileWriter fw = new FileWriter(orders, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f%n",
                    id, email, fulfilment, CsvUtil.esc(where),
                    promo == null ? "" : promo,
                    subtotal, discount, fee, total);
        }

        // Write associated order items
        try (FileWriter fw = new FileWriter(items, true);
             PrintWriter pw = new PrintWriter(fw)) {
            for (CartItem i : cartItems) {
                pw.printf("%s,%s,%d%n", id, i.productId, i.quantity);
            }
        }
        return id;
    }

    /**
     * Counts how many orders a given user (by email) has placed.
     * <p>Reads directly from {@code orders.csv} and skips the header.</p>
     *
     * @param email user's email.
     * @return order count (or 0 if none or file read error).
     */
    public int countOrdersByEmailSafe(String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(orders))) {
            br.readLine(); // skip header
            int count = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",", 3);
                if (s.length > 1 && s[1].equals(email)) count++;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * <p><strong>OrderRow — Immutable Order Representation</strong></p>
     *
     * <p>Used when returning results from {@link #listOrdersByEmail(String)}.</p>
     */
    public static class OrderRow {
        public final String orderId;
        public final String email;
        public final String fulfilment;   // PICKUP / DELIVERY
        public final String where;
        public final String promo;
        public final double subtotal, discount, fee, total;

        /**
         * Constructs an immutable {@code OrderRow} instance.
         *
         * @param orderId     order identifier
         * @param email       customer email
         * @param fulfilment  fulfilment type
         * @param where       location or address
         * @param promo       promo code
         * @param subtotal    subtotal value
         * @param discount    discount value
         * @param fee         service or delivery fee
         * @param total       final total value
         */
        public OrderRow(String orderId, String email, String fulfilment, String where, String promo,
                        double subtotal, double discount, double fee, double total) {
            this.orderId = orderId;
            this.email = email;
            this.fulfilment = fulfilment;
            this.where = where;
            this.promo = promo;
            this.subtotal = subtotal;
            this.discount = discount;
            this.fee = fee;
            this.total = total;
        }
    }

    /**
     * Returns all orders belonging to a specific email address.
     * <p>Parses {@code orders.csv}, ignoring malformed lines or headers,
     * and creates a list of {@link OrderRow} objects.</p>
     *
     * @param email target customer's email (case-insensitive).
     * @return list of matching orders; may be empty if none found.
     */
    public List<OrderRow> listOrdersByEmail(String email) {
        File f = this.orders;
        List<OrderRow> out = new ArrayList<>();

        try (var br = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] a = line.split(",", -1);

                // Expected columns:
                // orderId,email,fulfilment,where,promo,subtotal,discount,fee,total
                if (a.length < 9) continue;
                if (!email.equalsIgnoreCase(a[1])) continue;

                double sub = parseDouble(a[5]);
                double dis = parseDouble(a[6]);
                double fee = parseDouble(a[7]);
                double tot = parseDouble(a[8]);

                out.add(new OrderRow(a[0], a[1], a[2], a[3], a[4], sub, dis, fee, tot));
            }
        } catch (Exception ignore) {}

        return out;
    }

    /**
     * Parses a string into a double, returning 0.0 on failure.
     *
     * @param s input string.
     * @return parsed double or 0.0 if invalid.
     */
    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
