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
package com.google.gwt.i18n.rebind;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.i18n.shared.GwtLocaleFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for dealing with locales.
 */
public class LocaleUtils {
  // TODO(jat): rewrite to avoid statics

  private static GwtLocaleFactory factory = new GwtLocaleFactoryImpl();

  /**
   * The token representing the locale property controlling Localization.
   */
  private static final String PROP_LOCALE = "locale";

  /**
   * The token representing the runtime.locales configuration property.
   */
  private static final String PROP_RUNTIME_LOCALES = "runtime.locales";

  private static GwtLocale compileLocale;

  private static Set<GwtLocale> allLocales = new HashSet<GwtLocale>();

  private static Set<GwtLocale> allCompileLocales = new HashSet<GwtLocale>();
  
  private static Set<GwtLocale> runtimeLocales = new HashSet<GwtLocale>();

  /**
   * Returns the set of all compile-time locales.
   * 
   * @return unmodifiable set of all compile-time locales
   */
  public static Set<GwtLocale> getAllCompileLocales() {
    return Collections.unmodifiableSet(allCompileLocales);
  }

  /**
   * Returns the set of all available locales, whether compile-time locales or
   * runtime locales.
   * 
   * @return unmodifiable set of all locales
   */
  public static Set<GwtLocale> getAllLocales() {
    return Collections.unmodifiableSet(allLocales);
  }

  /**
   * @return the static compile-time locale for this permutation.
   */
  public static GwtLocale getCompileLocale() {
    return compileLocale;
  }

  /**
   * Get a shared GwtLocale factory so instances are cached between all uses.
   * 
   * @return singleton GwtLocaleFactory instance.
   */
  public static GwtLocaleFactory getLocaleFactory() {
    return factory;
  }

  /**
   * Returns a list of locales which are children of the current compile-time
   * locale.
   * 
   * @return unmodifiable list of matching locales
   */
  public static Set<GwtLocale> getRuntimeLocales() {
    return Collections.unmodifiableSet(runtimeLocales);
  }

  /**
   * Initialize from properties. Only needs to be called once, before any other
   * calls.
   * 
   * @param logger
   * @param propertyOracle
   */
  public static void init(TreeLogger logger, PropertyOracle propertyOracle) {
    try {
      String localeName = propertyOracle.getPropertyValue(logger, PROP_LOCALE);
      GwtLocale newCompileLocale = factory.fromString(localeName);
      if (newCompileLocale.equals(compileLocale)) {
        return;
      }
      compileLocale = newCompileLocale;
      allLocales = new HashSet<GwtLocale>();
      allCompileLocales = new HashSet<GwtLocale>();
      runtimeLocales = new HashSet<GwtLocale>();
      String[] localeValues = propertyOracle.getPropertyValueSet(logger,
          PROP_LOCALE);
      String rtLocaleNames = propertyOracle.getPropertyValue(logger,
          PROP_RUNTIME_LOCALES);
      for (String localeValue : localeValues) {
        allCompileLocales.add(factory.fromString(localeValue));
      }
      allLocales.addAll(allCompileLocales);
      if (rtLocaleNames != null && rtLocaleNames.length() > 0) {
        String[] rtLocales = rtLocaleNames.split(",");
        for (String rtLocale : rtLocales) {
          GwtLocale locale = factory.fromString(rtLocale);
          // TODO(jat): remove use of labels
          existingLocales:
          for (GwtLocale existing : allCompileLocales) {
            for (GwtLocale alias : existing.getAliases()) {
              if (!alias.isDefault() && locale.inheritsFrom(alias)
                  && locale.usesSameScript(alias)) {
                allLocales.add(locale);
                break existingLocales;
              }
            }
          }
          if (!compileLocale.isDefault()
              && locale.inheritsFrom(compileLocale)
              && locale.usesSameScript(compileLocale)) {
            // TODO(jat): don't include runtime locales which also inherit
            // from a more-specific compile locale than this one
            runtimeLocales.add(locale);
          }
        }
      }
    } catch (BadPropertyValueException e) {
      logger.log(TreeLogger.TRACE,
          "Unable to get locale properties, using defaults", e);
      compileLocale = factory.fromString("default");
      allLocales.add(compileLocale);
      return;
    }
  }
}