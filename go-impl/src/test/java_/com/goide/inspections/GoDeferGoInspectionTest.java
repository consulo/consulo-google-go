/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.inspections;

import jakarta.annotation.Nonnull;

import com.goide.SdkAware;
import com.goide.quickfix.GoQuickFixTestBase;

@SdkAware
public abstract class GoDeferGoInspectionTest extends GoQuickFixTestBase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(GoDeferGoInspection.class);
  }

  public void testParens() {
    doTest(GoDeferGoInspection.UNWRAP_PARENTHESES_QUICK_FIX_NAME, true);
  }

  public void testTwiceParens() {
    doTest(GoDeferGoInspection.UNWRAP_PARENTHESES_QUICK_FIX_NAME, true);
  }

  public void testParensFunctionType() {
    doTest(GoDeferGoInspection.ADD_CALL_QUICK_FIX_NAME, true);
  }

  public void testLiteral() {
    doTestNoFix(GoDeferGoInspection.ADD_CALL_QUICK_FIX_NAME, true);
  }

  public void testFuncLiteral() {
    doTest(GoDeferGoInspection.ADD_CALL_QUICK_FIX_NAME, true);
  }

  public void testConversions() {
    myFixture.testHighlighting(getTestName(true) + ".go");
  }

  public void testValid() {
    myFixture.testHighlighting(getTestName(true) + ".go");
  }

  public void testDeferRecover() { doTest(GoDeferGoInspection.REPLACE_WITH_CORRECT_DEFER_RECOVER, true); }

  @Nonnull
  @Override
  protected String getBasePath() {
    return "inspections/go-defer-function-call";
  }
}
