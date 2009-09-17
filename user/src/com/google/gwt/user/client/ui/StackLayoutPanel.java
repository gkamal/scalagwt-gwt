/*
 * Copyright 2009 Google Inc.
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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.user.client.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A panel that stacks its children vertically, displaying only one at a time,
 * with a header for each child which the user can click to display.
 * 
 * <p>
 * This widget will <em>only</em> work in standards mode, which requires
 * that the HTML page in which it is run have an explicit &lt;!DOCTYPE&gt;
 * declaration.
 * </p>
 * 
 * <p>
 * NOTE: This class is still very new, and its interface may change without
 * warning. Use at your own risk.
 * </p>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.StackLayoutPanelExample}
 * </p>
 * 
 * TODO(jgw):
 * - implement insert().
 * - add() methods with default widgets for headers.
 * - some way to get the header widget associated with a child.
 * - make animation configurable (with {@link HasAnimation}).
 * - default style.
 */
public class StackLayoutPanel extends Composite implements HasWidgets,
    RequiresLayout, RequiresResize, ProvidesResize {

  private class ClickWrapper extends Composite {
    private Widget target;

    public ClickWrapper(Widget target, Widget wrappee) {
      this.target = target;
      initWidget(wrappee);
      sinkEvents(Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
      if (event.getTypeInt() == Event.ONCLICK) {
        showWidget(target);
      }
    }
  }

  private static class LayoutData {
    public double headerSize;
    public Widget header;
    public Widget widget;
    public Layer widgetLayer;
    public Layer headerLayer;

    public LayoutData(Widget widget, Widget header, double headerSize,
        Layer widgetLayer, Layer headerLayer) {
      this.widget = widget;
      this.header = header;
      this.headerSize = headerSize;
      this.widgetLayer = widgetLayer;
      this.headerLayer = headerLayer;
    }
  }

  private static final int ANIMATION_TIME = 250;

  private LayoutPanel layoutPanel;
  private Unit unit;
  private ArrayList<LayoutData> layoutData = new ArrayList<LayoutData>();
  private Widget visibleWidget;

  /**
   * Creates an empty stack panel.
   * 
   * @param unit the unit to be used for layout
   */
  public StackLayoutPanel(Unit unit) {
    this.unit = unit;
    initWidget(layoutPanel = new LayoutPanel());
  }

  public void add(Widget w) {
    assert false : "Single-argument add() is not supported for this widget";
  }

  /**
   * Adds a child widget to this stack, along with a widget representing the
   * stack header.
   * 
   * @param widget the child widget to be added
   * @param header the header widget
   * @param headerSize the size of the header widget
   */
  public void add(Widget widget, Widget header, double headerSize) {
    ClickWrapper wrapper = new ClickWrapper(widget, header);
    layoutPanel.add(wrapper);
    layoutPanel.add(widget);

    Layer headerLayer = layoutPanel.getLayer(wrapper);
    headerLayer.setLeftRight(0, Unit.PX, 0, Unit.PX);

    Layer widgetLayer = layoutPanel.getLayer(widget);
    widgetLayer.setLeftRight(0, Unit.PX, 0, Unit.PX);

    LayoutData data = new LayoutData(widget, wrapper, headerSize, widgetLayer,
        headerLayer);
    layoutData.add(data);

    if (visibleWidget == null) {
      visibleWidget = widget;
    }
  }

  public void clear() {
    layoutPanel.clear();
    visibleWidget = null;
  }

  public Iterator<Widget> iterator() {
    return new Iterator<Widget>() {
      int i = 0, last = -1;

      public boolean hasNext() {
        return i < layoutData.size();
      }

      public Widget next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return layoutData.get(last = i++).widget;
      }

      public void remove() {
        if (last < 0) {
          throw new IllegalStateException();
        }

        StackLayoutPanel.this.remove(layoutData.get(last).widget);
        i = last;
        last = -1;
      }
    };
  }

  public void layout() {
    layout(0);
  }

  public void layout(int duration) {
    layout(duration, null);
  }

  public void layout(int duration, AnimationCallback callback) {
    int top = 0, bottom = 0;
    int i = 0, visibleIndex = -1;
    for (; i < layoutData.size(); ++i) {
      LayoutData data = layoutData.get(i);
      data.headerLayer.setTopHeight(top, unit, data.headerSize, unit);

      top += data.headerSize;

      data.widgetLayer.setTopHeight(top, unit, 0, unit);

      if (data.widget == visibleWidget) {
        visibleIndex = i;
        break;
      }
    }

    assert visibleIndex != -1;

    for (int j = layoutData.size() - 1; j > i; --j) {
      LayoutData data = layoutData.get(j);
      data.headerLayer.setBottomHeight(bottom, unit, data.headerSize, unit);
      data.widgetLayer.setBottomHeight(bottom, unit, 0, unit);
      bottom += data.headerSize;
    }

    LayoutData data = layoutData.get(visibleIndex);
    data.widgetLayer.setTopBottom(top, unit, bottom, unit);

    layoutPanel.layout(duration, callback);
  }

  public void onResize() {
    layoutPanel.onResize();
  }

  public boolean remove(Widget child) {
    if (child.getParent() != this) {
      return false;
    }

    LayoutData data = (LayoutData) child.getLayoutData();
    layoutPanel.remove(data.header);
    layoutPanel.remove(child);
    return true;
  }

  /**
   * Shows the specified widget.
   * 
   * @param widget the child widget to be shown.
   */
  public void showWidget(Widget widget) {
    visibleWidget = widget;
    layout(ANIMATION_TIME);
  }
}