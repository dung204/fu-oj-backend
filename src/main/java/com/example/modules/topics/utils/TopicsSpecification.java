package com.example.modules.topics.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.topics.entities.Topic;

public class TopicsSpecification extends SpecificationBuilder<Topic> {

  public static TopicsSpecification builder() {
    return new TopicsSpecification();
  }
}
