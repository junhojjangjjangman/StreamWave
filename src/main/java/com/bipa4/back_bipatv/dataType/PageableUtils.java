package com.bipa4.back_bipatv.dataType;

import java.awt.print.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageableUtils {

  public static Pageable getPageableWithCursor(String cursor, int size) {
    int page = 0;
    Sort sort = Sort.by(Sort.Order.asc("id")); // 정렬 기준은 ID로 변경 가능

    if (cursor != null && !cursor.isEmpty()) {
      try {
        page = Integer.parseInt(cursor);
        if (page < 0) {
          page = 0;
        }
      } catch (NumberFormatException e) {
        // 커서 값이 숫자가 아닌 경우 기본값으로 설정
        page = 0;
      }
    }

    return (Pageable) PageRequest.of(page, size, sort);
  }
}
