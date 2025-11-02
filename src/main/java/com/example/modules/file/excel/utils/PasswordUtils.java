package com.example.modules.file.excel.utils;

import java.security.SecureRandom;

public class PasswordUtils {

  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL = "@!#$&^";
  private static final String ALL = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
  private static final SecureRandom RANDOM = new SecureRandom();

  public static String generateRandomPassword(int length) {
    if (length < 8) {
      length = 8;
    }

    StringBuilder password = new StringBuilder(length);
    password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
    password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
    password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
    password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

    for (int i = 4; i < length; i++) {
      password.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
    }

    char[] chars = password.toString().toCharArray();
    for (int i = 0; i < chars.length; i++) {
      int randomIndex = RANDOM.nextInt(chars.length);
      char temp = chars[i];
      chars[i] = chars[randomIndex];
      chars[randomIndex] = temp;
    }

    return new String(chars);
  }
}
