package com.datn.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    // Loại bỏ dấu tiếng Việt và chuyển về chữ thường
    public static String removeVietnameseDiacritics(String str) {
        if (str == null)
            return null;
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        temp = temp.replaceAll("đ", "d").replaceAll("Đ", "D");
        return temp.toLowerCase();
    }
}
