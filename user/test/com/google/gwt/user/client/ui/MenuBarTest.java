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
import com.google.gwt.user.client.Command;

import java.util.List;

/**
 * Tests the DockPanel widget.
 */
public class MenuBarTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  /**
   * Test adding and removing {@link MenuItem}s and {@link MenuItemSeparator}s
   * from a menu.
   */
  public void testAddRemoveItems() {
    // Create a menu bar
    MenuBar bar = new MenuBar(true);

    // Create a blank command
    Command blankCommand = new Command() {
      public void execute() {
      }
    };

    // Add an item, default to text
    MenuItem item0 = bar.addItem("<b>test</b>", blankCommand);
    assertEquals("<b>test</b>", item0.getText());
    assertEquals(blankCommand, item0.getCommand());
    assertEquals(bar, item0.getParentMenu());

    // Add a separator
    MenuItemSeparator separator0 = bar.addSeparator();
    assertEquals(bar, separator0.getParentMenu());

    // Add another item, force to html
    MenuItem item1 = bar.addItem("<b>test1</b>", true, blankCommand);
    assertEquals("test1", item1.getText());
    assertEquals(blankCommand, item1.getCommand());
    assertEquals(bar, item1.getParentMenu());

    // Get all items
    List<MenuItem> items = bar.getItems();
    assertEquals(item0, items.get(0));
    assertEquals(item1, items.get(1));

    // Remove an item
    bar.removeItem(item0);
    assertEquals(item1, items.get(0));
    assertNull(item0.getParentMenu());

    // Remove the separator
    bar.removeSeparator(separator0);
    assertEquals(item1, items.get(0));
    assertNull(separator0.getParentMenu());
    
    // Add a bunch of items and clear them all
    MenuItem item2 = bar.addItem("test2", true, blankCommand);
    MenuItemSeparator separator1 = bar.addSeparator();
    MenuItem item3 = bar.addItem("test3", true, blankCommand);
    MenuItemSeparator separator2 = bar.addSeparator();
    MenuItem item4 = bar.addItem("test4", true, blankCommand);
    MenuItemSeparator separator3 = bar.addSeparator();
    bar.clearItems();
    assertEquals(0, bar.getItems().size());
    assertNull(item2.getParentMenu());
    assertNull(item3.getParentMenu());
    assertNull(item4.getParentMenu());
    assertNull(separator1.getParentMenu());
    assertNull(separator2.getParentMenu());
    assertNull(separator3.getParentMenu());
  }
}