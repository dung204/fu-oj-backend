package com.example.base.utils;

import java.lang.reflect.Constructor;
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
@SuppressWarnings("unchecked")
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

  /**
   * Combines multiple groups of specifications using a logical OR.
   * <p>
   * This method is used to construct complex OR conditions. Each function provided in the
   * {@code specFunctions} varargs receives a new, temporary {@code SpecificationBuilder}.
   * The specifications defined within each function are built, and the resulting
   * {@link org.springframework.data.jpa.domain.Specification} objects are then combined using a logical OR.
   * This final combined OR specification is then added to the current builder's list of specifications.
   * <p>
   * Example:
   * <pre>{@code
   * builder.and(user.name.equal("John"))
   *        .or(
   *            b -> b.and(user.age.greaterThan(20), user.status.equal("ACTIVE")),
   *            b -> b.and(user.age.lessThan(10), user.status.equal("INACTIVE"))
   *        );
   * // This will result in a query predicate like:
   * // WHERE name = 'John' AND ((age > 20 AND status = 'ACTIVE') OR (age < 10 AND status = 'INACTIVE'))
   * }</pre>
   *
   * @param specFunctions A varargs array of functions. Each function takes a new {@code SpecificationBuilder}
   *                      and is used to define a group of criteria. These groups will be joined by an OR condition.
   * @param <S>           The type of the {@code SpecificationBuilder}, allowing for method chaining on subclasses.
   * @return The current builder instance ({@code this}) for fluent chaining.
   */
  public <S extends SpecificationBuilder<T>> S or(Function<S, S>... specFunctions) {
    if (specFunctions == null || specFunctions.length == 0) {
      return (S) this;
    }

    List<Specification<T>> orSpecifications = new ArrayList<>();

    try {
      // For each function, create a new temporary builder, apply the function, and get the resulting specification.
      for (Function<S, S> specFunction : specFunctions) {
        // Create a new instance of the *actual subclass* (e.g., CommentsSpecification)
        Constructor<S> tempBuilderConstructor = (Constructor<
          S
        >) this.getClass().getDeclaredConstructor();

        tempBuilderConstructor.setAccessible(true);
        S tempBuilder = tempBuilderConstructor.newInstance();
        tempBuilderConstructor.setAccessible(false);

        specFunction.apply(tempBuilder);
        orSpecifications.add(tempBuilder.build());
      }
    } catch (Exception e) {
      throw new RuntimeException(
        "Failed to create new instance of SpecificationBuilder subclass",
        e
      );
    }

    // Combine all the generated specifications with OR logic.
    if (!orSpecifications.isEmpty()) {
      specifications.add(
        orSpecifications.stream().reduce(Specification.unrestricted(), Specification::or)
      );
    }

    return (S) this;
  }

  public <S extends SpecificationBuilder<T>> S withId(String id) {
    specifications.add((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id));
    return (S) this;
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

  public <S extends SpecificationBuilder<T>> S createdBy(String userId) {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.equal(root.get("createdBy"), userId)
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
