package com.example.modules.certificates.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.courses.entities.Course;
import com.example.modules.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certifications")
public class Certificate extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(nullable = false)
  private String name; // Tên chứng chỉ

  @Column(
    nullable = false,
    columnDefinition = "TEXT DEFAULT 'Completed all exercises in the course'"
  )
  private String condition; // Điều kiện để được cấp chứng chỉ

  @Column(columnDefinition = "TEXT")
  private String reason; // Lý do cấp chứng chỉ (hữu dụng khi ADMIN cấp chứng chỉ bằng tay)
}
