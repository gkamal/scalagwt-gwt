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
package com.google.gwt.dev;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.HostedMode.HostedModeOptionsImpl;
import com.google.gwt.dev.jjs.JsOutputOption;
import com.google.gwt.dev.shell.BrowserWidgetHostChecker;

import java.io.File;
import java.net.BindException;

/**
 * Test for {@link HostedMode}.
 */
public class HostedModeTest extends ArgProcessorTestBase {

  public static class MySCL extends ServletContainerLauncher {
    public ServletContainer start(TreeLogger logger, int port, File appRootDir)
        throws BindException, Exception {
      throw new UnsupportedOperationException();
    }
  }

  private final HostedMode.ArgProcessor argProcessor;
  private final HostedModeOptionsImpl options = new HostedModeOptionsImpl();

  public HostedModeTest() {
    argProcessor = new HostedMode.ArgProcessor(options);
  }

  public void testAllValidArgs() {
    assertProcessSuccess(argProcessor, "-port", "8080", "-whitelist", "white",
        "-blacklist", "black", "-logLevel", "DEBUG", "-style", "PRETTY", "-ea",
        "-XdisableAggressiveOptimization", "-noserver", "-server",
        MySCL.class.getName(), "-gen", "myGen", "-war", "myWar", "-workDir",
        "myWork", "-extra", "myExtra", "-localWorkers", "2", "-startupUrl",
        "http://www.google.com/", "-startupUrl", "foo", "c.g.g.h.H",
        "my.Module");

    assertNotNull(BrowserWidgetHostChecker.matchWhitelisted("white"));
    assertNotNull(BrowserWidgetHostChecker.matchBlacklisted("black"));

    assertEquals(new File("myGen").getAbsoluteFile(),
        options.getGenDir().getAbsoluteFile());
    assertEquals(new File("myWar"), options.getWarDir());
    assertEquals(new File("myWork"), options.getWorkDir());
    assertEquals(new File("myExtra"), options.getExtraDir());

    assertEquals(TreeLogger.DEBUG, options.getLogLevel());
    assertEquals(JsOutputOption.PRETTY, options.getOutput());
    assertTrue(options.isEnableAssertions());
    assertFalse(options.isAggressivelyOptimize());

    assertEquals(2, options.getLocalWorkers());

    assertEquals(8080, options.getPort());
    // False because -server overrides -noserver.
    assertFalse(options.isNoServer());
    assertSame(MySCL.class, options.getServletContainerLauncher().getClass());

    assertEquals(2, options.getStartupURLs().size());
    assertEquals("http://www.google.com/", options.getStartupURLs().get(0));
    assertEquals("foo", options.getStartupURLs().get(1));

    assertEquals(2, options.getModuleNames().size());
    assertEquals("c.g.g.h.H", options.getModuleNames().get(0));
    assertEquals("my.Module", options.getModuleNames().get(1));
  }

  public void testNoServer() {
    assertProcessSuccess(argProcessor, "-noserver", "c.g.g.h.H");

    assertTrue(options.isNoServer());
    assertNull(options.getServletContainerLauncher());

    assertEquals(1, options.getModuleNames().size());
    assertEquals("c.g.g.h.H", options.getModuleNames().get(0));
  }

  public void testNoServerOverridesServer() {
    assertProcessSuccess(argProcessor, "-server", MySCL.class.getName(),
        "-noserver", "c.g.g.h.H");

    assertTrue(options.isNoServer());
    assertSame(MySCL.class, options.getServletContainerLauncher().getClass());

    assertEquals(1, options.getModuleNames().size());
    assertEquals("c.g.g.h.H", options.getModuleNames().get(0));
  }

  public void testDefaultArgs() {
    assertProcessSuccess(argProcessor, "c.g.g.h.H");

    assertEquals(null, options.getGenDir());
    assertEquals(new File("war").getAbsoluteFile(),
        options.getWarDir().getAbsoluteFile());
    assertEquals(null, options.getWorkDir());
    assertEquals(null, options.getExtraDir());

    assertEquals(TreeLogger.INFO, options.getLogLevel());
    assertEquals(JsOutputOption.OBFUSCATED, options.getOutput());
    assertFalse(options.isEnableAssertions());
    assertTrue(options.isAggressivelyOptimize());

    assertEquals(1, options.getLocalWorkers());

    assertEquals(8888, options.getPort());
    assertFalse(options.isNoServer());
    assertNotNull(options.getServletContainerLauncher());

    assertEquals(0, options.getStartupURLs().size());

    assertEquals(1, options.getModuleNames().size());
    assertEquals("c.g.g.h.H", options.getModuleNames().get(0));
  }

  public void testForbiddenArgs() {
    assertProcessFailure(argProcessor, "-out", "www");
  }
}
