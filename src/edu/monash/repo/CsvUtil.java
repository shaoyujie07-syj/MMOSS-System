package edu.monash.repo;

public class CsvUtil {
    public static String esc(String s) {
        return s == null ? "" : s.replace(",", ";");
    }
}