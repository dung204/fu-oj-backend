package com.example.modules.Judge0.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Utils {

  /**
   * Mã hóa chuỗi UTF-8 sang Base64.
   *
   * @param input Chuỗi gốc (plain text)
   * @return Chuỗi sau khi mã hóa Base64, hoặc null nếu input null
   */
  public static String encodeBase64(String input) {
    if (input == null) return null;
    // .getBytes(StandardCharsets.UTF_8): chuyển chuỗi sang mảng byte theo chuẩn UTF-8
    return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Giải mã Base64 sang chuỗi UTF-8 (decode an toàn).
   *
   * @param input Chuỗi Base64 hoặc plain text
   * @return Chuỗi UTF-8 đã decode (hoặc gốc nếu không hợp lệ)
   */
  public static String decodeBase64Safe(String input) {
    if (input == null) return null;
    try {
      // Remove tất cả whitespace (newlines, spaces, etc.) trong base64 string
      String cleanedInput = input.replaceAll("\\s+", "");
      return new String(Base64.getDecoder().decode(cleanedInput), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return input; // nếu không phải base64 thì giữ nguyên
    }
  }
}
