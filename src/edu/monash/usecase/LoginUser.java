package edu.monash.usecase;

import edu.monash.service.AuthService;

/**
 * <p><strong>LoginUser — User Login Use Case</strong></p>
 *
 * <p>This use case coordinates the login process for both customers and administrators
 * in the Monash Market Online Supermarket System (MMOSS). It delegates actual authentication
 * verification to the {@link AuthService}, providing a cleaner interface between
 * the presentation layer and service layer.</p>
 *
 * <p>Its purpose is to encapsulate login logic and ensure consistent access control
 * handling for both user roles while keeping the UI layer free of low-level validation details.</p>
 *
 * @author XiaoWei
 * @version 1.0
 * @since 2025-10-16
 */
public class LoginUser {

    /** Reference to the authentication service used for verifying user credentials. */
    private final AuthService auth;

    /**
     * Constructs the {@code LoginUser} use case with a dependency on {@link AuthService}.
     *
     * @param auth the authentication service that performs credential validation.
     */
    public LoginUser(AuthService auth) {
        this.auth = auth;
    }

    /**
     * Authenticates a customer user by delegating to {@link AuthService#loginCustomer(String, String)}.
     *
     * @param email customer's email address.
     * @param pwd   customer's password.
     * @return {@code true} if the login is successful and the role is CUSTOMER; otherwise {@code false}.
     */
    public boolean asCustomer(String email, String pwd) {
        // Delegate authentication to service layer
        return auth.loginCustomer(email, pwd);
    }

    /**
     * Authenticates an administrator user by delegating to {@link AuthService#loginAdmin(String, String)}.
     *
     * @param email administrator's email address.
     * @param pwd   administrator's password.
     * @return {@code true} if the login is successful and the role is ADMIN; otherwise {@code false}.
     */
    public boolean asAdmin(String email, String pwd) {
        // Delegate authentication to service layer
        return auth.loginAdmin(email, pwd);
    }
}
