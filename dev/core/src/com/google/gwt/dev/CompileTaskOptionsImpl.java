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
package com.google.gwt.dev;

import com.google.gwt.core.ext.TreeLogger.Type;

import java.io.File;

/**
 * Concrete class to implement compiler task options.
 */
class CompileTaskOptionsImpl implements CompileTaskOptions {

  private Type logLevel;
  private String moduleName;
  private boolean useGuiLogger;
  private File workDir;

  public CompileTaskOptionsImpl() {
  }

  public CompileTaskOptionsImpl(CompileTaskOptions other) {
    copyFrom(other);
  }

  public void copyFrom(CompileTaskOptions other) {
    setLogLevel(other.getLogLevel());
    setModuleName(other.getModuleName());
    setUseGuiLogger(other.isUseGuiLogger());
    setWorkDir(other.getWorkDir());
  }

  public File getCompilerWorkDir() {
    return new File(new File(getWorkDir(), getModuleName()), "compiler");
  }

  public Type getLogLevel() {
    return logLevel;
  }

  public String getModuleName() {
    return moduleName;
  }

  public File getWorkDir() {
    return workDir;
  }

  public boolean isUseGuiLogger() {
    return useGuiLogger;
  }

  public void setLogLevel(Type logLevel) {
    this.logLevel = logLevel;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public void setUseGuiLogger(boolean useGuiLogger) {
    this.useGuiLogger = useGuiLogger;
  }

  public void setWorkDir(File workDir) {
    this.workDir = workDir;
  }
}