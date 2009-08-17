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
package com.google.gwt.uibinder.sample.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Strict;

/**
 * Sample resources used by {@link WidgetBasedUi}. This would more typically be
 * nested in {@code WidgetBasedUi}, but it's top level to ensure that the ui
 * code generator is able to deal with that kind of thing.
 */
public interface WidgetBasedUiResources extends ClientBundle {
  /**
   * Sample CssResource.
   */
  public interface MyCss extends CssResource {
    String menuBar();

    String prettyText();

    String tmText();
  }

  @Source("WidgetBasedUi.css")
  @Strict
  MyCss style();
}