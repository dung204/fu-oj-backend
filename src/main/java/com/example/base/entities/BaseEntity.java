package com.example.base.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base entity class that provides common fields and audit functionality for all entities.
 * This class serves as a superclass for all JPA entities in the application and includes
 * automatic timestamping for creation, modification, and soft deletion operations.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>UUID-based primary key generation</li>
 *   <li>Automatic audit trail with creation and modification timestamps</li>
 *   <li>Soft delete support through {@code deletedTimestamp} field</li>
 * </ul>
 *
 */
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  protected String id;

  @Column(nullable = false)
  @CreatedDate
  protected Instant createdTimestamp;

  @Column(nullable = false)
  @LastModifiedDate
  protected Instant updatedTimestamp;

  @Column(nullable = true)
  protected Instant deletedTimestamp;
}
