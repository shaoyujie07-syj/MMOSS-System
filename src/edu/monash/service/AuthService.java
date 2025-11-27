package edu.monash.service;

import edu.monash.repo.UsersRepo;

/**
 * <p><strong>AuthService — Authentication Service</strong></p>
 *
 * <p>This service is responsible for validating user login credentials
 * in the Monash Market Online Supermarket System (MMOSS). It verifies
 * user identity by matching the provided email and password against
 * stored user records.</p>
 *
 * <p>The service supports both <em>Customer</em> and <em>Admin</em>
 * authentication modes, distinguishing roles based on stored metadata
 * in {@link UsersRepo}.</p>
 *
 * @author XiaoWei
 * @version 1.0
 * @since 2025-10-16
 */
public class AuthService {

    /** Repository for retrieving user account information. */
    private final UsersRepo users;

    /**
     * Constructs the authentication service with a user repository dependency.
     *
     * @param users the {@link UsersRepo} instance containing user credentials.
     */
    public AuthService(UsersRepo users) {
        this.users = users;
    }

    /**
     * Validates a customer's login credentials.
     *
     * <p>The method checks if the provided email exists and whether the user
     * has the role {@code CUSTOMER}. It also verifies that the password
     * matches the stored value.</p>
     *
     * @param email customer's email address.
     * @param pwd   customer's password.
     * @return {@code true} if authentication is successful; {@code false} otherwise.
     */
    public boolean loginCustomer(String email, String pwd) {
        var r = users.users.get(email);
        return r != null && r.role.equals("CUSTOMER") && r.password.equals(pwd);
    }

    /**
     * Validates an administrator's login credentials.
     *
     * <p>The method checks if the provided email exists and whether the user
     * has the role {@code ADMIN}. It also verifies that the password matches
     * the stored value.</p>
     *
     * @param email administrator's email address.
     * @param pwd   administrator's password.
     * @return {@code true} if authentication is successful; {@code false} otherwise.
     */
    public boolean loginAdmin(String email, String pwd) {
        var r = users.users.get(email);
        return r != null && r.role.equals("ADMIN") && r.password.equals(pwd);
    }
}
