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

package com.goide.util;

import consulo.process.ProcessHandler;
import consulo.process.event.ProcessAdapter;
import consulo.process.event.ProcessEvent;
import consulo.util.dataholder.Key;
import consulo.util.lang.Pair;

import java.util.concurrent.ConcurrentLinkedQueue;

public class GoHistoryProcessListener extends ProcessAdapter {
  private final ConcurrentLinkedQueue<Pair<ProcessEvent, Key>> myHistory = new ConcurrentLinkedQueue<>();

  @Override
  public void onTextAvailable(ProcessEvent event, Key outputType) {
    myHistory.add(Pair.create(event, outputType));
  }

  public void apply(ProcessHandler listener) {
    for (Pair<ProcessEvent, Key> pair : myHistory) {
      listener.notifyTextAvailable(pair.getFirst().getText(), pair.getSecond());
    }
  }
}
