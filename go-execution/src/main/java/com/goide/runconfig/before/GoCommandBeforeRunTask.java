/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.runconfig.before;

import com.dslplatform.json.CompiledJson;
import consulo.execution.BeforeRunTask;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class GoCommandBeforeRunTask extends BeforeRunTask {
  private @Nullable String myCommand;

  public GoCommandBeforeRunTask() {
  }

  @Attribute("command")
  public @Nullable String getCommand() {
    return myCommand;
  }

  public void setCommand(@Nullable String command) {
    myCommand = StringUtil.nullize(command, true);
  }

  @Override
  public String toString() {
    return "go " + StringUtil.notNullize(myCommand);
  }
}
