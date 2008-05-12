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
package com.google.gwt.dev.javac;

import com.google.gwt.dev.util.InstalledHelpInfo;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import java.util.Stack;

/**
 * Check a compilation unit for violations of
 * {@link com.google.gwt.core.client.JavaScriptObject JavaScriptObject} (JSO)
 * restrictions. The restrictions are:
 * 
 * <ul>
 * <li> All instance methods on JSO classes must be one of: final, private, or a
 * member of a final class.
 * <li> JSO classes cannot implement interfaces that define methods.
 * <li> No instance methods on JSO classes may override another method. (This
 * catches accidents where JSO itself did not finalize some method from its
 * superclass.)
 * <li> JSO classes cannot have instance fields.
 * <li> "new" operations cannot be used with JSO classes.
 * <li> Every JSO class must have precisely one constructor, and it must be
 * protected, empty, and no-argument.
 * <li> Nested JSO classes must be static.
 * </ul>
 * 
 * Any violations found are attached as errors on the
 * CompilationUnitDeclaration.
 */
public class JSORestrictionsChecker {

  private class JSORestrictionsVisitor extends ASTVisitor implements
      ClassFileConstants {

    private final Stack<Boolean> isJsoStack = new Stack<Boolean>();

    @Override
    public void endVisit(AllocationExpression exp, BlockScope scope) {
      // Anywhere an allocation occurs is wrong.
      if (exp.type != null && isJsoSubclass(exp.type.resolveType(scope))) {
        errorOn(exp, ERR_NEW_JSO);
      }
    }

    @Override
    public void endVisit(ConstructorDeclaration meth, ClassScope scope) {
      if (!isJso()) {
        return;
      }
      if ((meth.arguments != null) && (meth.arguments.length > 0)) {
        errorOn(meth, ERR_CONSTRUCTOR_WITH_PARAMETERS);
      }
      if ((meth.modifiers & AccProtected) == 0) {
        errorOn(meth, ERR_NONPROTECTED_CONSTRUCTOR);
      }
      if (meth.statements != null && meth.statements.length > 0) {
        errorOn(meth, ERR_NONEMPTY_CONSTRUCTOR);
      }
    }

    @Override
    public void endVisit(FieldDeclaration field, MethodScope scope) {
      if (!isJso()) {
        return;
      }
      if (!field.isStatic()) {
        errorOn(field, ERR_INSTANCE_FIELD);
      }
    }

    @Override
    public void endVisit(MethodDeclaration meth, ClassScope scope) {
      if (!isJso()) {
        return;
      }
      if ((meth.modifiers & (AccFinal | AccPrivate | AccStatic)) == 0) {
        // The method's modifiers allow it to be overridden. Make
        // one final check to see if the surrounding class is final.
        if ((meth.scope == null) || !meth.scope.enclosingSourceType().isFinal()) {
          errorOn(meth, ERR_INSTANCE_METHOD_NONFINAL);
        }
      }

      // Should not have to check isStatic() here, but isOverriding() appears
      // to be set for static methods.
      if (!meth.isStatic()
          && (meth.binding != null && meth.binding.isOverriding())) {
        errorOn(meth, ERR_OVERRIDDEN_METHOD);
      }
    }

    @Override
    public void endVisit(TypeDeclaration type, BlockScope scope) {
      popIsJso();
    }

    @Override
    public void endVisit(TypeDeclaration type, ClassScope scope) {
      popIsJso();
    }

    @Override
    public void endVisit(TypeDeclaration type, CompilationUnitScope scope) {
      popIsJso();
    }

    @Override
    public boolean visit(TypeDeclaration type, BlockScope scope) {
      pushIsJso(checkType(type));
      return true;
    }

    @Override
    public boolean visit(TypeDeclaration type, ClassScope scope) {
      pushIsJso(checkType(type));
      return true;
    }

    @Override
    public boolean visit(TypeDeclaration type, CompilationUnitScope scope) {
      pushIsJso(checkType(type));
      return true;
    }

    private boolean checkType(TypeDeclaration type) {
      if (!isJsoSubclass(type.binding)) {
        return false;
      }
      if (type.enclosingType != null && !type.binding.isStatic()) {
        errorOn(type, ERR_IS_NONSTATIC_NESTED);
      }

      ReferenceBinding[] interfaces = type.binding.superInterfaces();
      if (interfaces != null) {
        for (ReferenceBinding interf : interfaces) {
          if (interf.methods() != null && interf.methods().length > 0) {
            String intfName = String.copyValueOf(interf.shortReadableName());
            errorOn(type, errInterfaceWithMethods(intfName));
          }
        }
      }
      return true;
    }

    private boolean isJso() {
      return isJsoStack.peek();
    }

    private void popIsJso() {
      isJsoStack.pop();
    }

    private void pushIsJso(boolean isJso) {
      isJsoStack.push(isJso);
    }
  }

  static final String ERR_CONSTRUCTOR_WITH_PARAMETERS = "Constructors must not have parameters in subclasses of JavaScriptObject";
  static final String ERR_INSTANCE_FIELD = "Instance fields cannot be used in subclasses of JavaScriptObject";
  static final String ERR_INSTANCE_METHOD_NONFINAL = "Instance methods must be 'final' in non-final subclasses of JavaScriptObject";
  static final String ERR_IS_NONSTATIC_NESTED = "Nested classes must be 'static' if they extend JavaScriptObject";
  static final String ERR_NEW_JSO = "'new' cannot be used to create instances of JavaScriptObject subclasses; instances must originate in JavaScript";
  static final String ERR_NONEMPTY_CONSTRUCTOR = "Constructors must be totally empty in subclasses of JavaScriptObject";
  static final String ERR_NONPROTECTED_CONSTRUCTOR = "Constructors must be 'protected' in subclasses of JavaScriptObject";
  static final String ERR_OVERRIDDEN_METHOD = "Methods cannot be overridden in JavaScriptObject subclasses";
  static final String JSO_CLASS = "com/google/gwt/core/client/JavaScriptObject";

  /**
   * Checks an entire
   * {@link org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration}.
   * 
   */
  public static void check(CompilationUnitDeclaration cud) {
    JSORestrictionsChecker checker = new JSORestrictionsChecker(cud);
    checker.check();
  }

  static String errInterfaceWithMethods(String intfName) {
    return "JavaScriptObject classes cannot implement interfaces with methods ("
        + intfName + ")";
  }

  private final CompilationUnitDeclaration cud;

  private JSORestrictionsChecker(CompilationUnitDeclaration cud) {
    this.cud = cud;
  }

  private void check() {
    cud.traverse(new JSORestrictionsVisitor(), cud.scope);
  }

  private void errorOn(ASTNode node, String error) {
    GWTProblem.recordInCud(node, cud, error, new InstalledHelpInfo(
        "jsoRestrictions.html"));
  }

  private boolean isJsoSubclass(TypeBinding typeBinding) {
    if (!(typeBinding instanceof ReferenceBinding)) {
      return false;
    }
    ReferenceBinding binding = (ReferenceBinding) typeBinding;
    while (binding.superclass() != null) {
      if (JSO_CLASS.equals(String.valueOf(binding.superclass().constantPoolName()))) {
        return true;
      }
      binding = binding.superclass();
    }
    return false;
  }
}