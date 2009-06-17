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
package com.google.gwt.i18n.rebind;

import static com.google.gwt.i18n.rebind.AnnotationUtil.getClassAnnotation;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.dev.util.collect.IdentityHashSet;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.rebind.AbstractResource.ResourceList;
import com.google.gwt.i18n.rebind.AnnotationsResource.AnnotationsError;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.i18n.shared.GwtLocaleFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates resources.
 */
public abstract class ResourceFactory {

  /**
   * Pair of JClassType and GwtLocale.
   */
  private static class ClassLocale {
    
    private final JClassType clazz;
    private final GwtLocale locale;
    
    public ClassLocale(JClassType clazz, GwtLocale locale) {
      assert clazz != null;
      assert locale != null;
      this.clazz = clazz;
      this.locale = locale;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      ClassLocale other = (ClassLocale) obj;
      return clazz.equals(other.clazz) && locale.equals(other.locale); 
    }

    public JClassType getJClass() {
      return clazz;
    }

    public GwtLocale getLocale() {
      return locale;
    }

    @Override
    public int hashCode() {
      return clazz.hashCode() + locale.hashCode() * 53;
    }
    
    @Override
    public String toString() {
      return clazz.getQualifiedSourceName() + "/" + locale.toString();
    }
  }

  /**
   * Separator between class name and locale in resource files.  Should not
   * appear in valid localizable class names.
   */
  public static final char LOCALE_SEPARATOR = '_';

  private static Map<ClassLocale, ResourceList> cache = new HashMap<
      ClassLocale, ResourceList>();
  private static List<ResourceFactory> loaders = new ArrayList<ResourceFactory>();

  static {
    loaders.add(new LocalizedPropertiesResource.Factory());
  }

  /**
   * Clears the resource cache.
   */
  public static void clearCache() {
    cache.clear();
  }

  /**
   * 
   * @param logger
   * @param topClass
   * @param bundleLocale
   * @param isConstants
   * @return resource list
   */
  public static ResourceList getBundle(TreeLogger logger, JClassType topClass,
      GwtLocale bundleLocale, boolean isConstants) {
    List<GwtLocale> locales = bundleLocale.getCompleteSearchList();
    List<JClassType> classes = new ArrayList<JClassType>();
    Set<JClassType> seenClasses = new IdentityHashSet<JClassType>();
    Map<ClassLocale, AnnotationsResource> annotations = new HashMap<ClassLocale,
        AnnotationsResource>();
    GwtLocaleFactory factory = LocaleUtils.getLocaleFactory();
    GwtLocale defaultLocale = factory.getDefault();
    walkInheritanceTree(logger, topClass, factory, defaultLocale, classes,
        annotations, seenClasses, isConstants);
    // TODO(jat): handle explicit subinterface with other locales -- ie:
    //   public interface Foo_es_MX extends Foo { ... }
    ResourceList allResources = new ResourceList();
    for (GwtLocale locale : locales) {
      for (JClassType clazz : classes) {
        ClassLocale key = new ClassLocale(clazz, locale);
        ResourceList resources;
        if (cache.containsKey(key)) {
          resources = cache.get(key);
        } else {
          cache.put(key, null);
          resources = new ResourceList();
          addFileResources(clazz, locale, resources);
          AnnotationsResource annotationsResource = annotations.get(key);
          if (annotationsResource != null) {
            resources.add(annotationsResource);
          }
          cache.put(key, resources);
        }
        if (resources != null) {
          allResources.addAll(resources);
        }
      }
    }
    String className = topClass.getQualifiedSourceName();
    TreeLogger branch = logger.branch(TreeLogger.SPAM,
        "Resource search order for " + className + ", locale " + bundleLocale);
    for (AbstractResource resource : allResources) {
      branch.log(TreeLogger.SPAM, resource.toString());
    }
    return allResources;
  }

  public static String getResourceName(JClassType targetClass) {
    String name = targetClass.getName();
    if (targetClass.isMemberType()) {
      name = name.replace('.', '$');
    }
    return name;
  }

  private static void addFileResources(JClassType clazz, GwtLocale locale,
      ResourceList resources) {
    // TODO: handle classes in the default package?
    String targetPath = clazz.getPackage().getName() + '.'
        + getResourceName(clazz);
    String localizedPath = targetPath;
    if (!locale.isDefault()) {
      localizedPath = targetPath + LOCALE_SEPARATOR + locale.getAsString();
    }
    // Check for file-based resources.
    ClassLoader loader = ResourceFactory.class.getClassLoader();
    String partialPath = localizedPath.replace('.', '/');
    for (int i = 0; i < loaders.size(); i++) {
      ResourceFactory element = loaders.get(i);
      String ext = "." + element.getExt();
      String path = partialPath + ext;
      InputStream m = loader.getResourceAsStream(path);
      if (m == null && partialPath.contains("$")) {
        // Also look for A_B for inner classes, as $ in path names
        // can cause issues for some build tools.
        path = partialPath.replace('$', '_') + ext;
        m = loader.getResourceAsStream(path);
      }
      if (m != null) {
        AbstractResource found = element.load(m, locale);
        found.setPath(path);
        resources.add(found);
      }
    }
  }

  private static void walkInheritanceTree(TreeLogger logger, JClassType clazz,
      GwtLocaleFactory factory, GwtLocale defaultLocale,
      List<JClassType> classes,
      Map<ClassLocale, AnnotationsResource> annotations,
      Set<JClassType> seenClasses, boolean isConstants) {
    if (seenClasses.contains(clazz)) {
      return;
    }
    seenClasses.add(clazz);
    classes.add(clazz);
    AnnotationsResource resource;
    try {
      resource = new AnnotationsResource(logger, clazz, defaultLocale,
          isConstants);
      if (resource.notEmpty()) {
        resource.setPath(clazz.getQualifiedSourceName());
        ClassLocale key = new ClassLocale(clazz, defaultLocale);
        annotations.put(key, resource);
        String defLocaleValue = null;
        
        // If the class has an embedded locale in it, use that for the default
        String className = clazz.getSimpleSourceName();
        int underscore = className.indexOf('_');
        if (underscore >= 0) {
          defLocaleValue = className.substring(underscore + 1);
        }
        
        // If there is an annotation declaring the default locale, use that
        DefaultLocale defLocaleAnnot = getClassAnnotation(clazz,
            DefaultLocale.class);
        if (defLocaleAnnot != null) {
          defLocaleValue = defLocaleAnnot.value();
        }
        GwtLocale defLocale = LocaleUtils.getLocaleFactory().fromString(
            defLocaleValue);
        if (!defLocale.isDefault()) {
          key = new ClassLocale(clazz, defLocale);
          annotations.put(key, resource);
        }
      }
    } catch (AnnotationsError e) {
      logger.log(TreeLogger.ERROR, e.getMessage(), e);
    }
    if (clazz.getSuperclass() != null) {
      walkInheritanceTree(logger, clazz.getSuperclass(), factory, defaultLocale,
          classes, annotations, seenClasses, isConstants);
    }
    for (JClassType intf : clazz.getImplementedInterfaces()) {
      walkInheritanceTree(logger, intf, factory, defaultLocale, classes,
          annotations, seenClasses, isConstants);
    }   
  }

  abstract String getExt();

  abstract AbstractResource load(InputStream m, GwtLocale locale);
}
