/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.junit.viewer.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;
import java.util.Date;

/**
 * A data object for Report.
 */
public class Report implements IsSerializable {

  /**
   * @gwt.typeArgs <com.google.gwt.junit.viewer.client.Category>
   */
  private List/*<Category>*/ categories;

  private Date date;

  private String dateString; // Temporary addition until we get better date

  // formatting in GWT
  private String gwtVersion;

  private String id;

  public List/*<Category>*/ getCategories() {
    return categories;
  }

  public Date getDate() {
    return date;
  }

  public String getDateString() {
    return dateString;
  }

  public String getGwtVersion() {
    return gwtVersion;
  }

  public String getId() {
    return id;
  }

  public ReportSummary getSummary() {
    int numTests = 0;
    boolean testsPassed = true;

    for (int i = 0; i < categories.size(); ++i) {
      Category c = (Category) categories.get(i);
      List benchmarks = c.getBenchmarks();
      numTests += benchmarks.size();
    }

    return new ReportSummary(id, date, dateString, numTests, testsPassed);
  }

  public void setCategories(List categories) {
    this.categories = categories;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setDateString(String dateString) {
    this.dateString = dateString;
  }

  public void setGwtVersion(String gwtVersion) {
    this.gwtVersion = gwtVersion;
  }

  public void setId(String id) {
    this.id = id;
  }
}

