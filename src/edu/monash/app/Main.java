package edu.monash.app;

import edu.monash.domain.*;
import edu.monash.repo.*;
import edu.monash.service.*;
import edu.monash.usecase.*;

import java.util.*;

/**
 * <p><strong>MMOSS — Main Entry Point</strong></p>
 *
 * <p>This class boots the text-based Monash Market Online Supermarket System (MMOSS),
 * wiring repositories, services and use cases, and presenting the console menus for
 * customer and administrator flows. It also seeds in-memory stores by reading CSV files
 * from the <code>data/</code> folder to satisfy the assessment's requirement for
 * file-based persistence.</p>
 *
 * <p>All interactions are performed via a shared {@link Scanner} on standard input,
 * following simple menu loops. This class contains only orchestration logic; domain
 * rules are implemented in the respective service and use-case layers.</p>
 *
 * <p><em>Note:</em> Keep I/O and menu flow here; move business rules to services/use-cases
 * to preserve testability and separation of concerns.</p>
 *
 * @author Wei Chen (34849033)
 * @version 1.1
 * @since 2025-10-17
 */
public class Main {

    /** Directory that contains CSV files for initial data load. */
    private static final String DATA_DIR = "data";

    /** Shared scanner for all console input to avoid multiple stream handles. */
    private static final Scanner sc = new Scanner(System.in);

    /**
     * Application entry point. Initializes repositories/services/use cases, then
     * shows a simple role selection menu for Customer/Admin.
     *
     * @param args CLI arguments (unused).
     * @throws Exception if any repository initialization or loading fails.
     */
    public static void main(String[] args) throws Exception {

        // ---------------------- Repositories ----------------------
        var users = new UsersRepo(DATA_DIR);
        var accounts = new AccountsRepo(DATA_DIR);
        var memberships = new MembershipsRepo(DATA_DIR);
        var products = new ProductsRepo(DATA_DIR);
        var stores = new StoresRepo(DATA_DIR);
        var promos = new PromosRepo(DATA_DIR);
        var orders = new OrdersRepo(DATA_DIR);
        var payments = new PaymentsRepo(DATA_DIR);
        var memberHist = new MembershipHistoryRepo(DATA_DIR);

        // Ensure files and seed data exist
        memberHist.ensure();
        users.ensure(); accounts.ensure(); memberships.ensure(); products.ensure(); stores.ensure(); promos.ensure(); orders.ensure(); payments.ensure();
        users.load(); accounts.load(); memberships.load(); products.load(); stores.load(); promos.load();

        // ---------------------- Services ----------------------
        var auth = new AuthService(users);
        var profile = new ProfileService(accounts, memberships);
        var pricing = new PricingService();
        var promo = new PromoService(promos, orders);
        var catalog = new CatalogService(products);
        var cartSvc = new CartService(products);
        var accountSvc = new AccountService(accounts);
        var memberSvc = new MembershipService(memberships, accountSvc);
        var adminSvc = new AdminService(products);
        var checkout = new CheckoutService(accounts, products, orders, payments, profile, pricing, promo);

        // ---------------------- Use Cases ----------------------
        var loginUC = new LoginUser(auth);
        var viewProfileUC = new ViewProfile(profile);

        // ---------------------- Main Loop ----------------------
        while (true) {
            System.out.println("\n=== MMOSS ===");
            System.out.println("1) Customer Login");
            System.out.println("2) Admin Login");
            System.out.println("0) Exit");
            System.out.print("> ");
            String choice = sc.nextLine().trim();
            if ("0".equals(choice)) break;
            switch (choice) {
                case "1":
                    doCustomerFlow(
                            loginUC, catalog, cartSvc, checkout, viewProfileUC,
                            accountSvc, memberSvc, stores, products, pricing, profile, memberHist, orders
                    );
                    break;
                case "2":
                    doAdminFlow(loginUC, adminSvc, catalog, users);
                    break;
                default:
                    System.out.println("Unknown option");
            }
        }
        System.out.println("Goodbye.");
    }

    /**
     * Handles the entire customer interaction flow: authentication, browsing, cart,
     * checkout, profile, top-up, membership, order count, history, and filtering.
     *
     * @param loginUC       login coordination use case
     * @param catalog       product catalog service
     * @param cartSvc       shopping cart service
     * @param checkout      checkout service
     * @param viewProfileUC profile viewing use case
     * @param accountSvc    account/balance service
     * @param memberSvc     membership service
     * @param stores        stores repository (for pickup selection)
     * @param products      products repository (for cart lookups)
     * @param pricing       pricing service (member/regular prices)
     * @param profile       profile service (membership checks)
     * @param memberHist    membership history repo (audit trail)
     * @param orders        orders repo (for counts/history)
     * @throws Exception propagated from checkout flow
     */
    private static void doCustomerFlow(
            LoginUser loginUC,
            CatalogService catalog,
            CartService cartSvc,
            CheckoutService checkout,
            ViewProfile viewProfileUC,
            AccountService accountSvc,
            MembershipService memberSvc,
            StoresRepo stores,
            ProductsRepo products,
            PricingService pricing,
            ProfileService profile,
            MembershipHistoryRepo memberHist,
            OrdersRepo orders
    ) throws Exception {
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        if (!loginUC.asCustomer(email, pwd)) { System.out.println("Login failed."); return; }
        System.out.println("Welcome, " + email);

        // Customer menu loop
        while (true) {
            System.out.println("\n-- Customer Menu --");
            System.out.println("1) Browse products");
            System.out.println("2) Search by keyword");
            System.out.println("3) Cart");
            System.out.println("4) Checkout");
            System.out.println("5) Profile");
            System.out.println("6) Top up");
            System.out.println("7) Membership");
            System.out.println("8) Visit History (orders count)");
            System.out.println("9) Filter (cat/brand/price/in-stock)");
            System.out.println("10) History (orders & membership)");
            System.out.println("0) Logout");
            System.out.print("> ");
            String c = sc.nextLine().trim();
            if ("0".equals(c)) {
                // Clear cart on logout as a safety/convenience feature
                cartSvc.clear(email);
                System.out.println("Logged out. Cart cleared.");
                break;
            }
            switch (c) {
                case "1" -> {
                    // Table header
                    System.out.printf(java.util.Locale.US, "%-6s %-28s %10s %12s %8s%n",
                            "ID", "Name", "Price", "Member", "Stock");
                    catalog.listAll().forEach(p -> {
                        String nm = (p.name == null ? "Unknown" : p.name);
                        if (nm.length() > 28) nm = nm.substring(0, 27) + "…";
                        System.out.printf(java.util.Locale.US, "%-6s %-28s %10.2f %12.2f %8d%n",
                                p.id, nm, p.price, p.memberPrice, p.stock);
                    });
                }
                case "2" -> {
                    System.out.print("keyword: "); String k = sc.nextLine();
                    System.out.printf(java.util.Locale.US, "%-6s %-28s %10s %12s %8s%n",
                            "ID", "Name", "Price", "Member", "Stock");
                    catalog.search(k).forEach(p -> {
                        String nm = (p.name == null ? "Unknown" : p.name);
                        if (nm.length() > 28) nm = nm.substring(0, 27) + "…";
                        System.out.printf(java.util.Locale.US, "%-6s %-28s %10.2f %12.2f %8d%n",
                                p.id, nm, p.price, p.memberPrice, p.stock);
                    });
                }
                case "3" -> doCart(email, cartSvc, products, pricing, profile);
                case "4" -> doCheckout(email, cartSvc, checkout, stores);
                case "5" -> System.out.println(viewProfileUC.execute(email));
                case "6" -> { System.out.print("amount: "); double amt = Double.parseDouble(sc.nextLine()); System.out.println(accountSvc.topUp(email, amt)); }
                case "7" -> doMembership(email, memberSvc, memberHist);
                case "8" -> System.out.println("Orders placed: " + new OrdersRepo("data").countOrdersByEmailSafe(email));
                case "9" -> doFilter(catalog);
                case "10" -> doHistory(email, orders, memberHist);
                default -> System.out.println("Unknown option");
            }
        }
    }

    /**
     * Displays and manages the customer's cart: add/edit/remove/clear, with
     * running subtotals and member pricing where applicable.
     *
     * @param email   current customer's email (cart owner)
     * @param cartSvc cart service handling cart state
     * @param products products repository for product lookups
     * @param pricing pricing service for effective unit price
     * @param profile profile service to check membership status
     */
    private static void doCart(String email,
                               CartService cartSvc,
                               ProductsRepo products,
                               PricingService pricing,
                               ProfileService profile) {
        boolean isMember = false;
        try { isMember = profile.isMemberActive(email); } catch (Exception ignored) {}

        while (true) {
            var cart = cartSvc.get(email);

            System.out.println("-- Cart --");
            // Header: ID | Name | Qty | Unit | Line | VIP Unit
            System.out.printf(java.util.Locale.US, "%-6s %-24s %6s %10s %12s %12s%n",
                    "ID", "Name", "Qty", "Unit", "Line", "VIP Unit");

            double subtotal = 0.0;
            for (var i : cart.getItems()) {
                var p = products.products.get(i.productId);
                if (p == null || i.quantity <= 0) continue;

                String nm = (p.name == null ? "Unknown" : p.name);
                if (nm.length() > 24) nm = nm.substring(0, 23) + "…";

                double unit = pricing.unitPrice(p, isMember);   // effective unit (member or regular)
                double line = unit * i.quantity;
                subtotal += line;

                System.out.printf(java.util.Locale.US, "%-6s %-24s %6d %10.2f %12.2f %12.2f%n",
                        p.id, nm, i.quantity, unit, line, p.memberPrice);
            }

            System.out.println("---------------------------------------------------------------------");
            System.out.printf(java.util.Locale.US, "%-6s %-24s %6s %10s %12.2f %12s%n",
                    "", "Subtotal", "", "", subtotal, "");

            System.out.println("1) Add");
            System.out.println("2) Edit quantity");
            System.out.println("3) Remove");
            System.out.println("4) Clear");
            System.out.println("0) Back");
            System.out.print("> ");
            String c = sc.nextLine().trim();

            if ("0".equals(c)) break;

            switch (c) {
                case "1" -> {
                    System.out.print("productId: "); String pid = sc.nextLine().trim();
                    System.out.print("qty: "); int q = Integer.parseInt(sc.nextLine().trim());
                    System.out.println(cartSvc.add(email, pid, q));
                }
                case "2" -> { // edit quantity (0 removes item)
                    System.out.print("productId: "); String pid = sc.nextLine().trim();
                    System.out.print("new qty (0 = remove): "); int q = Integer.parseInt(sc.nextLine().trim());
                    System.out.println(cartSvc.editQuantity(email, pid, q));
                }
                case "3" -> {
                    System.out.print("productId: "); String pid = sc.nextLine().trim();
                    System.out.println(cartSvc.remove(email, pid) ? "Removed" : "Not found");
                }
                case "4" -> {
                    cartSvc.clear(email);
                    System.out.println("Cleared");
                }
                default -> { /* ignore */ }
            }
        }
    }

    /**
     * Executes the checkout flow including fulfilment selection (pickup/delivery),
     * promo code, order summary preview, payment, and confirmation output.
     *
     * @param email    current customer's email
     * @param cartSvc  cart service to obtain cart snapshot
     * @param checkout checkout service orchestrating totals and payment
     * @param stores   repository used to show pickup stores and resolve location text
     * @throws Exception propagated from underlying services during checkout
     */
    private static void doCheckout(String email, CartService cartSvc, CheckoutService checkout, StoresRepo stores) throws Exception {
        var cart = cartSvc.get(email);
        System.out.print("Fulfilment (PICKUP/DELIVERY): "); String f = sc.nextLine().trim().toUpperCase();
        String where = "";
        if ("PICKUP".equalsIgnoreCase(f)) {
            System.out.println("Available stores:");
            for (var s : stores.stores.values()) {
                System.out.println(s.id + " | " + s.name + " | " + s.address + " | " + s.phone + " | " + s.hours);
            }
            System.out.print("Enter store id: "); String sid = sc.nextLine().trim();
            var st = stores.stores.getOrDefault(sid, null);
            where = (st==null) ? "Unknown store" : (st.name + " - " + st.address);
        } else {
            where = "Default Delivery Address";
        }
        System.out.print("Promo code (blank if none): "); String promo = sc.nextLine().trim();

        // Preview order summary before committing
        String summary = checkout.buildOrderSummary(null, email, cart, f, where, promo);
        System.out.println(summary);

        var res = checkout.checkout(email, cart, f, where, promo);
        if (!"OK".equals(res.message)) System.out.println(res.message);
        else {
            System.out.println("Order Confirmed: " + res.orderId);
            System.out.println("Total: $" + String.format("%.2f", res.total));
            System.out.println("New Balance: $" + String.format("%.2f", res.newBalance));
        }
    }

    /**
     * Handles membership operations (purchase multi-year, renew multi-year, cancel) and logs to history.
     *
     * @param email      current customer's email
     * @param memberSvc  membership service
     * @param memberHist membership history repository (nullable-safe)
     */
    private static void doMembership(String email,
                                     MembershipService memberSvc,
                                     MembershipHistoryRepo memberHist) {
        while (true) {
            System.out.println("-- Membership --");
            System.out.println("1) Purchase (multi-year)");
            System.out.println("2) Renew (multi-year)");
            System.out.println("3) Cancel");
            System.out.println("0) Back");
            System.out.print("> ");
            String c = sc.nextLine().trim();
            if ("0".equals(c)) break;

            switch (c) {
                case "1" -> {
                    Integer years = askYears();
                    if (years == null) break; // back
                    int success = 0; String lastMsg = "";
                    for (int i = 0; i < years; i++) {
                        lastMsg = memberSvc.purchase(email);
                        if (lastMsg == null || lastMsg.toLowerCase().startsWith("insufficient")) break;
                        success++;
                    }
                    if (success == years) {
                        System.out.println("Purchased " + years + " year(s).");
                        if (memberHist != null) memberHist.append(email, "PURCHASE", years, 20.0 * years, java.time.LocalDate.now());
                    } else if (success > 0) {
                        System.out.println("Partially purchased " + success + " year(s). Balance may be low.");
                        if (memberHist != null) memberHist.append(email, "PURCHASE", success, 20.0 * success, java.time.LocalDate.now());
                    } else {
                        System.out.println(lastMsg == null ? "Failed" : lastMsg);
                    }
                }
                case "2" -> {
                    Integer years = askYears();
                    if (years == null) break; // back
                    int success = 0; String lastMsg = "";
                    for (int i = 0; i < years; i++) {
                        lastMsg = memberSvc.renew(email);
                        if (lastMsg == null || lastMsg.toLowerCase().startsWith("insufficient")) break;
                        success++;
                    }
                    if (success == years) {
                        System.out.println("Renewed " + years + " year(s).");
                        if (memberHist != null) memberHist.append(email, "RENEW", years, 20.0 * years, java.time.LocalDate.now());
                    } else if (success > 0) {
                        System.out.println("Partially renewed " + success + " year(s). Balance may be low.");
                        if (memberHist != null) memberHist.append(email, "RENEW", success, 20.0 * success, java.time.LocalDate.now());
                    } else {
                        System.out.println(lastMsg == null ? "Failed" : lastMsg);
                    }
                }
                case "3" -> {
                    String msg = memberSvc.cancel(email);
                    System.out.println(msg);
                    if (msg != null && !msg.toLowerCase().startsWith("insufficient")) {
                        if (memberHist != null) memberHist.append(email, "CANCEL", 0, 0.0, java.time.LocalDate.now());
                    }
                }
                default -> { /* ignore */ }
            }
        }
    }

    /**
     * Prompts for a positive number of years. Returns {@code null} when the user chooses to go back.
     * Caps the value at 10 years to avoid accidental large inputs.
     *
     * @return parsed years (1..10) or {@code null} if the user backs out
     */
    private static Integer askYears() {
        System.out.print("Years (enter 'b' to back): ");
        String s = sc.nextLine().trim();
        if (s.equalsIgnoreCase("b")) return null;
        try {
            int y = Integer.parseInt(s);
            if (y <= 0) { System.out.println("Years must be >= 1"); return null; }
            // Optional upper bound protection, max 10 years
            if (y > 10) { System.out.println("Max 10 years allowed; using 10."); y = 10; }
            return y;
        } catch (Exception e) {
            System.out.println("Invalid number");
            return null;
        }
    }

    /**
     * Provides a multi-criteria product filter by category/brand/price/stock and prints a table.
     *
     * @param catalog catalog service used to execute the filter
     */
    private static void doFilter(CatalogService catalog){
        System.out.print("category (blank for all): "); String cat = sc.nextLine();
        System.out.print("brand (blank for all): "); String brand = sc.nextLine();
        System.out.print("min price (blank for none): "); String minS = sc.nextLine();
        System.out.print("max price (blank for none): "); String maxS = sc.nextLine();
        System.out.print("in-stock only? (y/n): "); String io = sc.nextLine().trim().toLowerCase();
        Double min = (minS.isBlank()? null : Double.parseDouble(minS));
        Double max = (maxS.isBlank()? null : Double.parseDouble(maxS));
        Boolean instock = ("y".equals(io)? Boolean.TRUE : ("n".equals(io)? Boolean.FALSE : null));
        System.out.printf(java.util.Locale.US, "%-6s %-28s %10s %12s %8s%n",
                "ID", "Name", "Price", "Member", "Stock");
        catalog.filter(cat, brand, min, max, instock).forEach(p -> {
            String nm = (p.name == null ? "Unknown" : p.name);
            if (nm.length() > 28) nm = nm.substring(0, 27) + "…";
            System.out.printf(java.util.Locale.US, "%-6s %-28s %10.2f %12.2f %8d%n",
                    p.id, nm, p.price, p.memberPrice, p.stock);
        });

    }

    /**
     * Handles admin authentication and provides product CRUD + profile view.
     *
     * @param loginUC  use case to authenticate as admin
     * @param adminSvc admin service to add/edit/delete products
     * @param catalog  catalog service for listing/previewing products
     * @param users    users repo, used to fetch admin profile for display
     */
    private static void doAdminFlow(LoginUser loginUC, AdminService adminSvc, CatalogService catalog, UsersRepo users){
        System.out.print("Admin email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        if (!loginUC.asAdmin(email, pwd)) { System.out.println("Login failed."); return; }
        System.out.println("Welcome admin.");

        while (true) {
            System.out.println("\n-- Admin Panel --");
            System.out.println("1) List products");
            System.out.println("2) Add product");
            System.out.println("3) Edit product");
            System.out.println("4) Delete product");
            System.out.println("5) View profile");
            System.out.println("0) Back");
            System.out.print("> ");
            String c = sc.nextLine().trim();
            if ("0".equals(c)) break;
            switch (c) {
                case "1" -> catalog.listAll().forEach(p -> System.out.println(p.id + " | " + p.name + " | " + p.brand + " | $" + p.price + " | stock:" + p.stock));
                case "2" -> { var p = readProduct(); System.out.println(adminSvc.addProduct(p)); }
                case "3" -> { var p = readProduct(); System.out.println(adminSvc.editProduct(p)); }
                case "4" -> { System.out.print("productId: "); String id = sc.nextLine(); System.out.println(adminSvc.deleteProduct(id)); }
                case "5" -> {
                    var profile = users.getAdminProfileByEmail(email);
                    if (profile == null) System.out.println("(Profile not found)");
                    else {
                        System.out.println("\n-- Administrator Profile --");
                        System.out.println(profile.toString());
                    }
                }
                default -> System.out.println("Unknown option");
            }
        }
    }

    /**
     * Prints order history and membership history for the given user.
     *
     * @param email      user email
     * @param orders     orders repository (for order rows)
     * @param memberHist membership history repository (for membership events)
     */
    private static void doHistory(String email, OrdersRepo orders, MembershipHistoryRepo memberHist) {
        System.out.println("\n-- Order History --");
        var list = orders.listOrdersByEmail(email);
        if (list.isEmpty()) {
            System.out.println("(No orders)");
        } else {
            System.out.printf(java.util.Locale.US, "%-10s %-9s %-10s %-10s %-10s %-10s%n",
                    "OrderID", "Mode", "Subtotal", "Discount", "Fee", "Total");
            for (var r : list) {
                System.out.printf(java.util.Locale.US, "%-10s %-9s %10.2f %10.2f %10.2f %10.2f%n",
                        r.orderId, r.fulfilment, r.subtotal, r.discount, r.fee, r.total);
            }
        }

        System.out.println("\n-- Membership History --");
        var mh = memberHist.listByEmail(email);
        if (mh.isEmpty()) {
            System.out.println("(No membership records)");
        } else {
            System.out.printf(java.util.Locale.US, "%-10s %-8s %-8s %-12s%n", "Action", "Years", "Amount", "Date");
            for (var row : mh) {
                System.out.printf(java.util.Locale.US, "%-10s %-8d %-8.2f %-12s%n",
                        row.action, row.years, row.amount, row.date.toString());
            }
        }
    }

    /**
     * Reads a product from console input and builds a {@link Product} instance with basic validation.
     *
     * @return newly constructed product
     */
    private static Product readProduct() {
        String id = askNonBlank("id");
        String name = askNonBlank("name");
        String category = askNonBlank("category");

        System.out.print("subcategory (optional): ");
        String subcategory = sanitize(sc.nextLine());

        System.out.print("brand (optional): ");
        String brand = sanitize(sc.nextLine());

        System.out.print("description (optional): ");
        String description = sanitize(sc.nextLine());

        double price = askDouble("price (>=0)");
        double mprice = askDouble("memberPrice (>=0, <= price)");
        while (mprice > price) {
            System.out.println("memberPrice cannot be greater than price.");
            mprice = askDouble("memberPrice (>=0, <= price)");
        }

        int stock = askInt("stock (>=0)");

        System.out.print("expiry (YYYY-MM-DD, blank if N/A): ");
        String expiry = blankToNull(sc.nextLine().trim());

        System.out.print("ingredients (blank if N/A): ");
        String ingredients = blankToNull(sanitize(sc.nextLine()));

        System.out.print("storage (blank if N/A): ");
        String storage = blankToNull(sanitize(sc.nextLine()));

        System.out.print("allergens (blank if N/A): ");
        String allergens = blankToNull(sanitize(sc.nextLine()));

        return new Product(id, name, category, subcategory, brand, description,
                price, mprice, stock, expiry, ingredients, storage, allergens);
    }

    /**
     * Prompts repeatedly until a non-blank, sanitized string is entered.
     *
     * @param label field name to display
     * @return non-blank sanitized input
     */
    private static String askNonBlank(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine();
            if (s != null && !s.trim().isBlank()) return sanitize(s);
            System.out.println(label + " is required.");
        }
    }

    /**
     * Prompts for a non-negative double value, retrying until valid.
     *
     * @param label prompt text
     * @return parsed non-negative double
     */
    private static double askDouble(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                if (v < 0) { System.out.println("Must be >= 0."); continue; }
                return v;
            } catch (Exception e) {
                System.out.println("Invalid number.");
            }
        }
    }

    /**
     * Prompts for a non-negative integer value, retrying until valid.
     *
     * @param label prompt text
     * @return parsed non-negative integer
     */
    private static int askInt(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < 0) { System.out.println("Must be >= 0."); continue; }
                return v;
            } catch (Exception e) {
                System.out.println("Invalid integer.");
            }
        }
    }

    /**
     * Sanitizes free-text input by removing commas and trimming whitespace.
     *
     * @param s input string (nullable)
     * @return sanitized non-null string (empty when input is null)
     */
    private static String sanitize(String s) {
        return (s == null) ? "" : s.replace(",", " ").trim();
    }

    /**
     * Converts blank strings to {@code null} to represent missing optional fields.
     *
     * @param s input string
     * @return {@code null} if blank; otherwise the original string
     */
    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

}
