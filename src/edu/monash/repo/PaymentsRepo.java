package edu.monash.repo;

import java.io.*;
import java.util.Date;

/**
 * Repository for persisting payment records to a CSV file.
 *
 * <p><strong>Schema:</strong> {@code payments.csv} with header:
 * {@code orderId,pre,post,paidAt}</p>
 * <ul>
 *   <li><code>orderId</code>: associated order identifier</li>
 *   <li><code>pre</code>: account balance before payment (two decimals)</li>
 *   <li><code>post</code>: account balance after payment (two decimals)</li>
 *   <li><code>paidAt</code>: epoch milliseconds when the payment was recorded</li>
 * </ul>
 *
 * <p>All writes are append-only. Caller is responsible for concurrency control.</p>
 * @author Yujie Shao
 */
public class PaymentsRepo {
    private final File file;

    /**
     * Constructs a repository targeting {@code payments.csv} under the given directory.
     *
     * @param dir base directory for data files
     */
    public PaymentsRepo(String dir){ this.file = new File(dir, "payments.csv"); }

    /**
     * Ensures the CSV file exists with header row.
     *
     * @throws Exception if the file cannot be created
     */
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("orderId,pre,post,paidAt");
            }
        }
    }

    /**
     * Appends a payment record to the CSV.
     *
     * @param orderId associated order id
     * @param pre     balance before payment
     * @param post    balance after payment
     * @param paidAt  timestamp of payment
     * @throws Exception if writing fails
     */
    public void append(String orderId, double pre, double post, Date paidAt) throws Exception {
        try (FileWriter fw = new FileWriter(file, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("%s,%.2f,%.2f,%d%n", orderId, pre, post, paidAt.getTime());
        }
    }
}
