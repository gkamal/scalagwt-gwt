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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Tests {@link DateBox}.
 */
public class DateBoxTest extends WidgetTestBase {
  @Override
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  @DoNotRunWith(Platform.HtmlUnitBug)
  public void testAccessors() {
    DateBox db = new DateBox();
    assertFalse(db.isDatePickerShowing());
    db.showDatePicker();
    assertTrue(db.isDatePickerShowing());
    db.hideDatePicker();
    assertFalse(db.isDatePickerShowing());
  }

  @DoNotRunWith({Platform.HtmlUnitBug})
  public void testValueChangeEvent() {

    // Checks setValue(date, true);
    DateBox db = new DateBox();
    RootPanel.get().add(db);
    new DateValueChangeTester(db).run();

    // Check setting the text directly in the text box.
    final DateBox db2 = new DateBox();
    RootPanel.get().add(db2);
    new DateValueChangeTester(db2) {
      @Override
      protected void fire(java.util.Date d) {
        db2.getTextBox().setText(db2.getFormat().format(db2, d));
        NativeEvent e = Document.get().createBlurEvent();
        db2.getTextBox().getElement().dispatchEvent(e);
      }
    }.run();

    // Checks that setting the date picker's date works correctly.
    final DateBox db3 = new DateBox();
    RootPanel.get().add(db3);
    new DateValueChangeTester(db3) {
      @Override
      protected void fire(java.util.Date d) {
        db3.getDatePicker().setValue(d, true);
      }
    }.run();
  }
}
