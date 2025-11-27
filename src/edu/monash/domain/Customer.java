/**
 * Customer class represents a customer entity in the system.
 * It stores information such as customer details, membership status, and other relevant data.
 *
 * @author Xiao Wei
 */
package edu.monash.domain;


public class Customer extends User {

    public Customer(String email, String password) {
        super (email, password, "CUSTOMER");
    }
}

