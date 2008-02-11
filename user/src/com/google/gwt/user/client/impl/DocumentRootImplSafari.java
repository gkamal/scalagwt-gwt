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
package com.google.gwt.user.client.impl;

import com.google.gwt.user.client.Element;

/**
 * Safari implementation of {@link DocumentRootImpl}.
 */
public class DocumentRootImplSafari extends DocumentRootImpl {
  @Override
  protected native Element getDocumentRoot() /*-{
    // Safari does not implement $doc.compatMode.
    // Use a CSS test to determine rendering mode.
    var elem = $doc.createElement('div');
    elem.style.cssText = "width:0px;width:1";
    return parseInt(elem.style.width) != 1 ? $doc.documentElement : $doc.body;
  }-*/;
}