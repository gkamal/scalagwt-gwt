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
package com.google.gwt.resources.client;

import com.google.gwt.resources.ext.ResourceGeneratorType;
import com.google.gwt.resources.rg.ExternalTextResourceGenerator;

/**
 * Identical to {@link TextResource}, except the contents of the resource are
 * not inlined into the compiled output. This is suitable for resources that are
 * not required as part of program initialization.
 */
@ResourceGeneratorType(ExternalTextResourceGenerator.class)
public interface ExternalTextResource extends ResourcePrototype {
  void getText(ResourceCallback<TextResource> callback)
      throws ResourceException;
}
