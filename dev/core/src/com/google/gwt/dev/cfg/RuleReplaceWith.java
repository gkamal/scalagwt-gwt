/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.dev.cfg;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.StandardGeneratorContext;

/**
 * A rule to replace the type being rebound with an explicitly named class.
 */
public class RuleReplaceWith extends Rule {

  private final String replacementTypeName;

  public RuleReplaceWith(String typeName) {
    this.replacementTypeName = typeName;
  }

  public String getReplacementTypeName() {
    return replacementTypeName;
  }

  public String realize(TreeLogger logger, StandardGeneratorContext context,
      String typeName) throws UnableToCompleteException {
    return replacementTypeName;
  }

  public String toString() {
    return "<replace-with class='" + replacementTypeName + "'/>";
  }
}
