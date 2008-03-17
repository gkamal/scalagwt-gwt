/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.i18n.rebind;

import com.google.gwt.i18n.client.Messages;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;

/**
 * Creates a MessagesInterface from a Resource file.
 */
public class MessagesInterfaceCreator extends
    AbstractLocalizableInterfaceCreator {

  /**
   * Calculates the number of arguments <code>MessageFormat</code> expects to
   * see in a template.
   * 
   * @param template template to parse
   * @return number of args
   * @throws ParseException
   */
  public static int numberOfMessageArgs(String template) throws ParseException {
    /*
     * As parse, unlike format, cannot deal with single quotes, we have to remove
     * them. First, we remove doubled quotes (which go into the output as single
     * quotes.  Then, we remove any quoted strings.
     *
     * If sub-formats are supported in the future, this code will
     * have to change.
     */
    // strip doubled single-quotes
    template = template.replace("''", "");
    
    // delete quoted sections
    template = template.replaceAll("'[^']+'", "");
    
    if (template.length() == 0) {
      // special case empty strings since MessageFormat.parse chokes on them.
      return 0;
    }
    int numArgs = new MessageFormat(template).parse(template).length;
    return numArgs;
  }

  /**
   * Constructor for <code>MessagesInterfaceCreator</code>.
   * 
   * @param className class name
   * @param packageName package name
   * @param resourceBundle resource bundle
   * @param targetLocation target location
   * @throws IOException
   */
  public MessagesInterfaceCreator(String className, String packageName,
      File resourceBundle, File targetLocation) throws IOException {
    super(className, packageName, resourceBundle, targetLocation,
      Messages.class);
  }

  @Override
  protected void genMethodArgs(String defaultValue) {
    try {
      int numArgs = numberOfMessageArgs(defaultValue);
      for (int i = 0; i < numArgs; i++) {
        if (i > 0) {
          composer.print(",  ");
        }
        composer.print("String arg" + i);
      }
    } catch (ParseException e) {
      throw new RuntimeException(defaultValue
        + " could not be parsed as a MessageFormat string.", e);
    }
  }

  @Override
  protected String javaDocComment(String path) {
    return "Interface to represent the messages contained in resource  bundle:\n\t"
      + path + "'.";
  }
}