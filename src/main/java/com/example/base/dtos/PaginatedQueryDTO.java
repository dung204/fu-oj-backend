package com.example.base.dtos;

import com.example.base.annotations.OrderParam;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Data Transfer Object for paginated query requests.
 * <p>
 * This class encapsulates pagination parameters including page number, page size,
 * and sorting criteria for database queries that return paginated results.
 * </p>
 *
 * <p>
 * The sorting parameter accepts a list of strings in the format "{field}:{order}"
 * where field is one of the allowed timestamp fields and order is typically "asc", "desc", "ASC" or "DESC".
 * </p>
 *
 * <p>
 * Default values:
 * <ul>
 *   <li>{@code page}: 1 (first page)</li>
 *   <li>{@code pageSize}: 10 (10 items per page)</li>
 *   <li>{@code order}: empty list (no sorting applied)</li>
 * </ul>
 * </p>
 *
 * @apiNote For every modules, you can extends this class to create a DTO class encapsulating pagination, sorting
 * & search params for that specific domain. Make sure to override the {@code order} properties to allow different
 * fields for sorting
 *
 * @see OrderParam
 */
@Data
@Slf4j
public class PaginatedQueryDTO {

  @Parameter(description = "The current page number", example = "1")
  @Min(value = 1)
  private Integer page = 1;

  @Parameter(description = "The number of items in a page", example = "10")
  @Min(value = 10)
  private Integer pageSize = 10;

  @Parameter(
    description = "The sorting for the query. The syntax is `{field}:{order}`. Allowed fields are: `createdTimestamp`, `updatedTimestamp`, `deletedTimestamp`."
  )
  private List<
    @OrderParam(
      allowedFields = { "createdTimestamp", "updatedTimestamp", "deletedTimestamp" }
    ) String
  > order = Collections.emptyList();

  public List<String> getOrder() {
    try {
      Class<?> currentClass = this.getClass();
      Field orderField = currentClass.getField("order");
      orderField.setAccessible(true);
      List<String> orderValue = (List<String>) orderField.get(this);
      orderField.setAccessible(false);
      return orderValue;
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public void setOrder(List<String> order) {
    try {
      Class<?> currentClass = this.getClass();
      Field orderField = currentClass.getField("order");
      orderField.setAccessible(true);
      orderField.set(this, order);
      orderField.setAccessible(false);
    } catch (Exception e) {}
  }

  /**
   * Extracts all non-null field values from this object and its superclasses to create a filters map.
   * This method uses reflection to traverse the class hierarchy and collect field values that can be
   * used for filtering operations, typically in database queries or search operations.
   *
   * <p>The following fields are automatically excluded from the filters map:
   * <ul>
   *   <li>{@code page} - pagination page number</li>
   *   <li>{@code pageSize} - pagination page size</li>
   *   <li>{@code order} - sorting order specification</li>
   *   <li>{@code log} - logging instance from {@code @Sl4fj}</li>
   * </ul>
   *
   * <p>The method traverses the entire class hierarchy starting from the current class up to
   * (but not including) {@code Object.class}, ensuring that inherited fields are also included
   * in the filters map.
   *
   * @return a {@code Map<String, Object>} containing field names as keys and their non-null values
   *         as map values. Fields with null values or excluded field names are omitted from the result.
   *         If a field name exists in multiple levels of the class hierarchy, only the first
   *         encountered value is retained due to {@code putIfAbsent} usage.
   *
   * @throws IllegalAccessException if reflection access fails for any field, though this is logged
   *                         as an error and processing continues for remaining fields
   */
  public Map<String, Object> getFilters() {
    Set<String> excludedFields = Set.of("page", "pageSize", "order", "log");
    Map<String, Object> filters = new HashMap<>();
    Class<?> currentClass = this.getClass();

    while (currentClass != null && currentClass != Object.class) {
      List<Field> fields = Arrays.asList(currentClass.getDeclaredFields());
      fields.forEach(field -> {
        try {
          field.setAccessible(true);
          String fieldName = field.getName();
          Object value = field.get(this);

          if (!excludedFields.contains(fieldName) && value != null) {
            filters.putIfAbsent(fieldName, value);
          }
        } catch (IllegalAccessException e) {
          log.error("Failed to access field: {}", field.getName(), e);
        }
      });
      currentClass = currentClass.getSuperclass();
    }

    return filters;
  }

  public PageRequest toPageRequest() {
    // This prevent field shadowing when re-declaring `order` field in subclasses
    try {
      Field orderField = this.getClass().getDeclaredField("order");
      orderField.setAccessible(true);
      List<String> orderValue = (List<String>) orderField.get(this);
      orderField.setAccessible(false);
      if (orderValue.isEmpty()) {
        return PageRequest.of(page - 1, pageSize);
      }

      Sort sort = Sort.by(
        orderValue.stream().map(this::parseOrderString).toArray(Sort.Order[]::new)
      );

      return PageRequest.of(page - 1, pageSize, sort);
    } catch (Exception e) {
      return PageRequest.of(page - 1, pageSize);
    }
  }

  private Sort.Order parseOrderString(String orderStr) {
    String[] parts = orderStr.split(":");
    String field = parts[0].trim();
    String direction = parts[1].trim().toUpperCase();

    Sort.Direction sortDirection = "DESC".equals(direction)
      ? Sort.Direction.DESC
      : Sort.Direction.ASC;

    return new Sort.Order(sortDirection, field);
  }
}
