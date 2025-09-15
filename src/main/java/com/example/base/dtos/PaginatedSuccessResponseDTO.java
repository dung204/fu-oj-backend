package com.example.base.dtos;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * A specialized response DTO for paginated API responses that extends the base {@link ResponseDTO}.
 * This class encapsulates paginated data along with comprehensive metadata including
 * pagination details, sorting information, and applied filters.
 *
 * @param <T> the type of data elements contained in the paginated response
 *
 * @see ResponseDTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaginatedSuccessResponseDTO<T> extends ResponseDTO {

  private List<T> data;
  private Metadata metadata;

  @Builder
  private PaginatedSuccessResponseDTO(
    Integer status,
    String message,
    Page<T> page,
    Map<String, Object> filters
  ) {
    super(status == null ? 200 : validateSuccessStatus(status), message);
    this.data = page.getContent();
    this.metadata = new Metadata(page, filters);
  }

  public PaginatedSuccessResponseDTO() {
    super(200, null);
    this.data = null;
    this.metadata = null;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Metadata {

    private Pagination pagination;
    private List<Order> order;
    private Map<String, Object> filters;

    public Metadata(Page<?> page) {
      this.pagination = new Pagination(page);
      this.order = extractSortOrders(page.getSort());
      this.filters = new HashMap<>();
    }

    public Metadata(Page<?> page, Map<String, Object> filters) {
      this.pagination = new Pagination(page);
      this.order = extractSortOrders(page.getSort());
      this.filters = filters != null ? new HashMap<>(filters) : new HashMap<>();
    }

    private List<Order> extractSortOrders(Sort sort) {
      if (sort.isUnsorted()) {
        return Collections.emptyList();
      }

      return sort
        .stream()
        .map(order ->
          Order.builder()
            .field(order.getProperty())
            .direction(order.getDirection().name().toLowerCase())
            .build()
        )
        .toList();
    }
  }

  @Data
  @NoArgsConstructor
  public static class Pagination {

    private int currentPage;
    private int pageSize;
    private long total;
    private int totalPages;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    public Pagination(Page<?> page) {
      this.currentPage = page.getNumber() + 1;
      this.pageSize = page.getSize();
      this.total = page.getTotalElements();
      this.totalPages = page.getTotalPages();
      this.hasNextPage = page.hasNext();
      this.hasPreviousPage = page.hasPrevious();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Order {

    private String field;
    private String direction;
  }
}
