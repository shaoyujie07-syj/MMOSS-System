/**
 * User class represents a user entity with properties such as name, email, and role.
 * It provides basic methods to access and modify user information.
 *
 * @author Xiao Wei
 */
package edu.monash.domain;


public class User {
    
    public final String email;
    
    public final String password;
    
    public final String role;
    
    public String address;

    
    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}

