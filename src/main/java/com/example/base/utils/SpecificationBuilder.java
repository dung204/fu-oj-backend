package com.example.base.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * A builder class for constructing JPA Specifications using the builder pattern.
 * This class allows for dynamic creation of database query specifications by
 * combining multiple criteria using logical AND operations.
 *
 * <p>The builder maintains a list of specifications that can be combined into
 * a single specification for use with Spring Data JPA repositories.</p>
 *
 * @apiNote When extending this class, please remember to add a static method
 * called {@code builder()} to initialize an instance of the builder
 *
 * @param <T> the entity type that this specification builder operates on
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpecificationBuilder<T> {

  protected final List<Specification<T>> specifications = new ArrayList<>();

  public SpecificationBuilder<T> addSpecification(Specification<T> spec) {
    if (spec != null) {
      specifications.add(spec);
    }
    return this;
  }

  /**
   * Conditionally applies a specification function to this builder based on a boolean condition.
   * This method allows for conditional chaining of specifications, enabling dynamic query building
   * where certain criteria are only applied when specific conditions are met.
   *
   * @param condition the boolean condition that determines whether to apply the specification function
   * @param specFunction a function that takes this SpecificationBuilder and returns a modified SpecificationBuilder
   * @return this SpecificationBuilder instance if condition is false, or the result of applying
   *         the specFunction if condition is true
   *
   * @example
   * <pre>
   * specBuilder
   *   .conditionally(includeInactive, builder -> builder.equal("status", "INACTIVE"))
   *   .conditionally(!searchTerm.isEmpty(), builder -> builder.like("name", "%" + searchTerm + "%"));
   * </pre>
   */
  public <S extends SpecificationBuilder<T>> S conditionally(
    boolean condition,
    Function<S, S> specFunction
  ) {
    if (condition) {
      return specFunction.apply((S) this);
    }
    return (S) this;
  }

  /**
   * Conditionally applies one of two specification functions based on a boolean condition.
   * This method provides a fluent way to build different specifications depending on runtime conditions.
   *
   * @param condition the boolean condition to evaluate
   * @param specFunctionIfTrue the function to apply to this builder if the condition is true
   * @param specFunctionIfFalse the function to apply to this builder if the condition is false
   * @return a new SpecificationBuilder with the appropriate function applied based on the condition
   */
  public <S extends SpecificationBuilder<T>> S conditionally(
    boolean condition,
    Function<S, S> specFunctionIfTrue,
    Function<S, S> specFunctionIfFalse
  ) {
    if (condition) {
      return specFunctionIfTrue.apply((S) this);
    }
    return specFunctionIfFalse.apply((S) this);
  }

  public SpecificationBuilder<T> withId(String id) {
    specifications.add((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id));
    return this;
  }

  public <S extends SpecificationBuilder<T>> S notDeleted() {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.isNull(root.get("deletedTimestamp"))
    );
    return (S) this;
  }

  public <S extends SpecificationBuilder<T>> S deletedOnly() {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.isNotNull(root.get("deletedTimestamp"))
    );
    return (S) this;
  }

  /**
   * Builds and returns a composite Specification by combining all added specifications with AND logic.
   *
   * @return A Specification that represents the logical AND of all specifications in the builder.
   *         Returns an unrestricted Specification if no specifications have been added.
   */
  public Specification<T> build() {
    if (specifications.isEmpty()) {
      return Specification.unrestricted();
    }

    return specifications.stream().reduce(Specification.unrestricted(), Specification::and);
  }
}
