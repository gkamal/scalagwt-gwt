/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.user.client.ui;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Tests DateBox.
 */
public class DateBoxTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  public void testAccessors() {
    DateBox db = new DateBox();
    assertFalse(db.isDatePickerShowing());
    db.showDatePicker();
    assertTrue(db.isDatePickerShowing());
    db.hideDatePicker();
    assertFalse(db.isDatePickerShowing());
  }

  public void testValueChangeEvent() {
    DateBox db = new DateBox();
    RootPanel.get().add(db);
    new DateValueChangeTester(db).run();
  }
}