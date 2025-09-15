package com.example.base.utils;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public final class ObjectUtils {

  private ObjectUtils() {}

  /**
   * Equivalent of JavaScript's <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/assign">{@code Object.assign()}</a>.
   * However, this method ignores the properties from {@code sources} that <b>does not exist in {@code target}</b>
   *
   * <p>
   * This method also ignores the properties with the value of {@link JsonNullable#undefined()}
   * </p>
   *
   * @param target
   * @param sources
   *
   * @see JsonNullable#undefined()
   */
  @SuppressWarnings("rawtypes")
  public static void assign(Object target, Object... sources) {
    if (target == null || sources == null) {
      return;
    }

    BeanWrapper targetWrapper = new BeanWrapperImpl(target);
    PropertyDescriptor[] targetProps = targetWrapper.getPropertyDescriptors();
    Set<String> targetPropertyNames = new HashSet<>();

    for (PropertyDescriptor prop : targetProps) {
      if (prop.getWriteMethod() != null) {
        targetPropertyNames.add(prop.getName());
      }
    }

    for (Object source : sources) {
      if (source == null) {
        continue;
      }

      BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
      PropertyDescriptor[] sourceProps = sourceWrapper.getPropertyDescriptors();

      for (PropertyDescriptor prop : sourceProps) {
        String propName = prop.getName();
        if (
          !"class".equals(propName) &&
          targetPropertyNames.contains(propName) &&
          prop.getReadMethod() != null
        ) {
          Object value = sourceWrapper.getPropertyValue(propName);

          // Skip JsonNullable.undefined() values
          if (value != null && value.equals(JsonNullable.undefined())) {
            continue;
          }

          if (value instanceof JsonNullable) {
            targetWrapper.setPropertyValue(propName, ((JsonNullable) value).get());
            continue;
          }

          targetWrapper.setPropertyValue(propName, value);
        }
      }
    }
  }
}
