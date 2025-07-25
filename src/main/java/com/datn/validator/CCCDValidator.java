package com.datn.validator;

import java.util.Set;

public class CCCDValidator {
    
    private static final Set<String> VALID_PREFIXES = Set.of(
        "001", "002", "004", "006", "008", "010", "012", "014", "015", "017",
        "019", "020", "022", "024", "025", "026", "027", "030", "031", "033",
        "034", "035", "036", "037", "038", "040", "042", "044", "045", "046",
        "048", "049", "051", "052", "054", "056", "058", "060", "062", "064",
        "066", "067", "068", "070", "072", "074", "075", "077", "079", "080",
        "082", "083", "084", "086", "087", "089", "091", "092", "093", "094",
        "095", "096"
    );

     public static boolean isValidCCCD(String cccd) {
        if (cccd == null || !cccd.matches("^\\d{12}$")) {
            return false; // Không đúng 12 chữ số
        }

        String prefix = cccd.substring(0, 3);
        return VALID_PREFIXES.contains(prefix);
    }

    
}
