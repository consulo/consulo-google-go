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

package com.goide.quickfix;

import jakarta.annotation.Nonnull;

import com.goide.inspections.unresolved.GoUnresolvedReferenceInspection;

public abstract class GoCreateWrapperTypeQuickFixTest extends GoQuickFixTestBase {
  private static final String CREATE_TYPE_A = "Create type 'A'";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(GoUnresolvedReferenceInspection.class);
  }

  @Nonnull
  @Override
  protected String getBasePath() {
    return "quickfixes/create-type";
  }

  public void testSimple()      { doTest(CREATE_TYPE_A);      }
  public void testGlobal()      { doTest(CREATE_TYPE_A);      }
  public void testProhibited()  { doTestNoFix(CREATE_TYPE_A); }
}
