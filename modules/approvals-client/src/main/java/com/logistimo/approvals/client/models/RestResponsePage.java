/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.approvals.client.models;

import com.google.gson.annotations.SerializedName;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naveensnair on 19/06/17.
 */

public class RestResponsePage<T> {

  @SerializedName(value = "offset", alternate = {"number"})
  private int offset;
  private int size;
  @SerializedName(value = "total", alternate = {"total_elements"})
  private long totalElements;
  private List<T> content = new ArrayList<>(1);
  private Sort sort;

  public RestResponsePage(int offset, int size,
                          long totalElements,
                          List<T> content, Sort sort) {
    this.offset = offset;
    this.size = size;
    this.totalElements = totalElements;
    this.content = content;
    this.sort = sort;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int number) {
    this.offset = number;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public List<T> getContent() {
    return content;
  }

  public void setContent(List<T> content) {
    this.content = content;
  }

  public Sort getSort() {
    return sort;
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }
}
