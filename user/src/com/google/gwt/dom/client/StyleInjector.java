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
package com.google.gwt.dom.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;

/**
 * Used to add stylesheets to the document.
 */
public class StyleInjector {

  /**
   * The DOM-compatible way of adding stylesheets. This implementation requires
   * the host HTML page to have a head element defined.
   */
  public static class StyleInjectorImpl {
    private static final StyleInjectorImpl IMPL = GWT.create(StyleInjectorImpl.class);

    private HeadElement head;

    public StyleElement injectStyleSheet(String contents) {
      StyleElement style = createElement(contents);
      getHead().appendChild(style);
      return style;
    }

    public StyleElement injectStyleSheetAtEnd(String contents) {
      return injectStyleSheet(contents);
    }

    public StyleElement injectStyleSheetAtStart(String contents) {
      StyleElement style = createElement(contents);
      getHead().insertBefore(style, head.getFirstChild());
      return style;
    }

    public void setContents(StyleElement style, String contents) {
      style.setInnerText(contents);
    }

    private StyleElement createElement(String contents) {
      StyleElement style = Document.get().createStyleElement();
      style.setPropertyString("language", "text/css");
      setContents(style, contents);
      return style;
    }

    private HeadElement getHead() {
      if (head == null) {
        Element elt = Document.get().getElementsByTagName("head").getItem(0);
        assert elt != null : "The host HTML page does not have a <head> element"
            + " which is required by StyleInjector";
        head = HeadElement.as(elt);
      }
      return head;
    }
  }

  /**
   * IE doesn't allow manipulation of a style element through DOM methods. There
   * is also a hard-coded limit on the number of times that createStyleSheet can
   * be called before IE6 starts throwing exceptions.
   */
  public static class StyleInjectorImplIE extends StyleInjectorImpl {

    /*
     * TODO(bobv) : Talk to scottb about being able to read this out of the
     * module xml file as a configuration-property to handle cases where
     * multiple GWT modules are living in the same page.
     */
    private static final int MAX_STYLE_SHEETS = 30;
    private static final JsArray<StyleElement> STYLE_ELEMENTS = JavaScriptObject.createArray().cast();
    private static final JsArrayInteger STYLE_ELEMENT_LENGTHS = JavaScriptObject.createArray().cast();

    public native void appendContents(StyleElement style, String contents) /*-{
      style.cssText += contents;
    }-*/;

    @Override
    public StyleElement injectStyleSheet(String contents) {
      int idx = STYLE_ELEMENTS.length();
      if (idx < MAX_STYLE_SHEETS) {
        // Just create a new style element and add it to the list
        StyleElement style = createElement();
        setContents(style, contents);
        STYLE_ELEMENTS.set(idx, style);
        STYLE_ELEMENT_LENGTHS.set(idx, contents.length());
        return style;
      } else {
        /*
         * Find shortest style element to minimize re-parse time in the general
         * case.
         */
        int shortestLen = Integer.MAX_VALUE;
        int shortestIdx = -1;
        for (int i = 0; i < idx; i++) {
          if (STYLE_ELEMENT_LENGTHS.get(i) < shortestLen) {
            shortestLen = STYLE_ELEMENT_LENGTHS.get(i);
            shortestIdx = i;
          }
        }
        assert shortestIdx != -1;

        StyleElement style = STYLE_ELEMENTS.get(shortestIdx);
        STYLE_ELEMENT_LENGTHS.set(shortestIdx, shortestLen + contents.length());
        appendContents(style, contents);
        return style;
      }
    }

    @Override
    public StyleElement injectStyleSheetAtEnd(String contents) {
      if (STYLE_ELEMENTS.length() == 0) {
        return injectStyleSheet(contents);
      }

      int idx = STYLE_ELEMENTS.length() - 1;
      StyleElement style = STYLE_ELEMENTS.get(idx);
      STYLE_ELEMENT_LENGTHS.set(idx, STYLE_ELEMENT_LENGTHS.get(idx)
          + contents.length());
      appendContents(style, contents);

      return style;
    }

    @Override
    public StyleElement injectStyleSheetAtStart(String contents) {
      if (STYLE_ELEMENTS.length() == 0) {
        return injectStyleSheet(contents);
      }

      StyleElement style = STYLE_ELEMENTS.get(0);
      STYLE_ELEMENT_LENGTHS.set(0, STYLE_ELEMENT_LENGTHS.get(0)
          + contents.length());
      prependContents(style, contents);

      return style;
    }

    public native void prependContents(StyleElement style, String contents) /*-{
      style.cssText = contents + style.cssText;
    }-*/;

    @Override
    public native void setContents(StyleElement style, String contents) /*-{
      style.cssText = contents;
    }-*/;

    private native StyleElement createElement() /*-{
      return $doc.createStyleSheet();
    }-*/;
  }

  /**
   * Add a stylesheet to the document. The StyleElement returned by this method
   * is not guaranteed to be unique.
   * 
   * @param contents the CSS contents of the stylesheet
   * @return the StyleElement that contains the newly-injected CSS
   */
  public static StyleElement injectStylesheet(String contents) {
    return StyleInjectorImpl.IMPL.injectStyleSheet(contents);
  }

  /**
   * Add stylesheet data to the document as though it were declared after all
   * stylesheets previously created by {@link #injectStylesheet(String)}. The
   * StyleElement returned by this method is not guaranteed to be unique.
   * 
   * @param contents the CSS contents of the stylesheet
   * @return the StyleElement that contains the newly-injected CSS
   */
  public static StyleElement injectStylesheetAtEnd(String contents) {
    return StyleInjectorImpl.IMPL.injectStyleSheetAtEnd(contents);
  }

  /**
   * Add stylesheet data to the document as though it were declared before any
   * stylesheet previously created by {@link #injectStylesheet(String)}. The
   * StyleElement returned by this method is not guaranteed to be unique.
   * 
   * @param contents the CSS contents of the stylesheet
   * @return the StyleElement that contains the newly-injected CSS
   */
  public static StyleElement injectStylesheetAtStart(String contents) {
    return StyleInjectorImpl.IMPL.injectStyleSheetAtStart(contents);
  }

  /**
   * Replace the contents of a previously-injected stylesheet. Updating the
   * stylesheet in-place is typically more efficient than removing a
   * previously-created element and adding a new one. This method should be used
   * with some caution as StyleInjector may recycle StyleElements on certain
   * browsers.
   * 
   * @param style a StyleElement previously-returned from
   *          {@link #injectStylesheet(String)}.
   * @param contents the new contents of the stylesheet.
   */
  public static void setContents(StyleElement style, String contents) {
    StyleInjectorImpl.IMPL.setContents(style, contents);
  }

  /**
   * Utility class.
   */
  private StyleInjector() {
  }
}