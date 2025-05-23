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

package com.goide.runconfig.testing;

import com.goide.runconfig.testing.frameworks.gotest.GotestFramework;
import jakarta.annotation.Nonnull;

public abstract class GotestEventsConverterTest extends GoEventsConverterTestCase {
  public void testSingleTestFailed() {
    doTest();
  }

  public void testSingleTestOk() {
    doTest();
  }

  public void testMultipleTestsFailed() {
    doTest();
  }

  public void testMultipleTestsOk() {
    doTest();
  }

  public void testSingleTestLeadingSpaceOk() {
    doTest();
  }

  public void testSkipTest() {
    doTest();
  }

  public void testStdOut() {
    doTest();
  }

  public void testOneLineEvents() {
    doTest();
  }

  public void testUnicodeTestName() {
    doTest();
  }

  public void testSubTests() {
    doTest();
  }

  @Nonnull
  @Override
  protected String getBasePath() {
    return "testing/gotest";
  }

  @Nonnull
  @Override
  protected GoTestFramework getTestFramework() {
    return GotestFramework.INSTANCE;
  }
}
