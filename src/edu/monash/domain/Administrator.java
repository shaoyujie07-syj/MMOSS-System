/**
 * Administrator class represents an administrator entity who manages the system.
 * It provides administrative capabilities such as managing users, products, and other entities.
 *
 * @author Xiao Wei
 */
package edu.monash.domain;


public class Administrator extends User {
    
    public Administrator(String email, String password) {
        super(email, password, "ADMIN");
    }
}

