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
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JGwtCreate;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReboundEntryPoint;
import com.google.gwt.dev.jjs.ast.JVisitor;

import java.util.Set;

/**
 * Records all live rebinds.
 */
public class RecordRebinds {

  private class RebindVisitor extends JVisitor {
    @Override
    public void endVisit(JGwtCreate x, Context ctx) {
      String reqType = x.getSourceType().getName().replace('$', '.');
      liveRebindRequests.add(reqType);
    }

    @Override
    public void endVisit(JReboundEntryPoint x, Context ctx) {
      String reqType = x.getSourceType().getName().replace('$', '.');
      liveRebindRequests.add(reqType);
    }
  }

  public static void exec(JProgram program, Set<String> liveRebindRequests) {
    new RecordRebinds(program, liveRebindRequests).execImpl();
  }

  private final JProgram program;
  private final Set<String> liveRebindRequests;

  private RecordRebinds(JProgram program, Set<String> liveRebindRequests) {
    this.program = program;
    this.liveRebindRequests = liveRebindRequests;
  }

  private void execImpl() {
    RebindVisitor rebinder = new RebindVisitor();
    rebinder.accept(program);
  }

}
