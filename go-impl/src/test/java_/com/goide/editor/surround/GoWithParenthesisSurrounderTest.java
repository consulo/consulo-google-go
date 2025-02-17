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

package com.goide.editor.surround;

public abstract class GoWithParenthesisSurrounderTest extends GoSurrounderTestBase {
  private static final String PARENTHESIS_SURROUNDER = new GoWithParenthesisSurrounder().getTemplateDescription();

  public void testParenthesis() {
    doTest("a := <selection>1 + 2</selection>", "a := (1 + 2)\n", PARENTHESIS_SURROUNDER, true);
  }

  public void testNoParenthesis() {
    doTest("<selection>a := 1 + 2</selection>}", "<selection>a := 1 + 2</selection>", PARENTHESIS_SURROUNDER, false);
  }
}
