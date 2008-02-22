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
package com.google.gwt.dev.linker;

import java.util.SortedSet;

/**
 * Represents a deferred binding property. The deferred binding property may or
 * may not have a single value applied across all permutations.
 * 
 * SelectionProperty implementations must support object identity comparisons.
 */
public interface SelectionProperty {
  /**
   * Returns the name of the deferred binding property.
   */
  String getName();

  /**
   * Returns all possible values for this deferred binding property.
   */
  SortedSet<String> getPossibleValues();

  /**
   * Returns a raw function body that provides the runtime value to be used for
   * a deferred binding property.
   */
  String getPropertyProvider();

  /**
   * Returns the defined value for the deferred binding property or
   * <code>null</code> if the value of the property is not constant.
   * 
   * @see CompilationResult#getPropertyMap()
   */
  String tryGetValue();
}