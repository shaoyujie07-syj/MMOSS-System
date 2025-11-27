package edu.monash.domain;

public class Subcategory {
    public String name;
    public Category parent;

    public Subcategory(String n, Category p) {
        name = n;
        parent = p;
    }
}