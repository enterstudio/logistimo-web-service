package com.logistimo.approvals.builders;

import com.logistimo.approvals.client.models.RestResponsePage;

import org.springframework.data.domain.Sort;

import java.util.List;

public class RestResponsePageBuilder<T> {
  private int offset;
  private int size;
  private long totalElements;
  private List<T> content;
  private List<Sort.Order> sort;

  public RestResponsePageBuilder<T> withOffset(int offset) {
    this.offset = offset;
    return this;
  }

  public RestResponsePageBuilder<T> withSize(int size) {
    this.size = size;
    return this;
  }

  public RestResponsePageBuilder<T> withTotalElements(long totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  public RestResponsePageBuilder<T> withContent(List<T> content) {
    this.content = content;
    return this;
  }

  public RestResponsePageBuilder<T> withSort(List<Sort.Order> sort) {
    this.sort = sort;
    return this;
  }

  /**
   * Builds a new response using another, skips the content
   *
   * @param another - Build from another
   * @return builder.
   */
  public RestResponsePageBuilder<T> withRestResponsePage(RestResponsePage<?> another) {
    this.withOffset(another.getOffset())
        .withSize(another.getSize())
        .withSort(another.getSort())
        .withTotalElements(another.getTotalElements());
    return this;
  }

  public RestResponsePage<T> build() {
    return new RestResponsePage<>(offset, size, totalElements,
        content, sort);
  }
}
