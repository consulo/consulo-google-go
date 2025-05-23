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

package com.goide.editor;

import jakarta.annotation.Nonnull;

import com.goide.GoCodeInsightFixtureTestCase;
import com.goide.SdkAware;
import com.intellij.testFramework.TestDataPath;

@SdkAware
@TestDataPath("$PROJECT_ROOT/testData/colorHighlighting")
public abstract class GoHighlightingAnnotatorTest extends GoCodeInsightFixtureTestCase {

  public void testSimple() {
    doTest();
  }

  public void testLabel() {
    doTest();
  }

  public void testReceiver() {
    doTest();
  }

  public void testFuncAndMethod() {
    doTest();
  }

  public void testOctAndHex() {
    doTest();
  }

  public void testTypes() {
    doTest();
  }

  public void testStructFields() {
    doTest();
  }

  public void testBuiltinFunctions() {
    doTest();
  }

  private void doTest() {
    myFixture.testHighlighting(false, true, false, getTestName(true) + ".go");
  }

  @Nonnull
  @Override
  protected String getBasePath() {
    return "colorHighlighting";
  }
}

