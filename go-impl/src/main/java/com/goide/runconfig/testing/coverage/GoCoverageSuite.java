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

package com.goide.runconfig.testing.coverage;

import consulo.execution.coverage.BaseCoverageSuite;
import consulo.execution.coverage.CoverageEngine;
import consulo.execution.coverage.CoverageFileProvider;
import consulo.execution.coverage.CoverageRunner;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoCoverageSuite extends BaseCoverageSuite {
  public GoCoverageSuite() {
  }

  public GoCoverageSuite(String name,
                         @Nullable CoverageFileProvider fileProvider,
                         long lastCoverageTimeStamp,
                         CoverageRunner coverageRunner,
                         Project project) {
    super(name, fileProvider, lastCoverageTimeStamp, false, false, false, coverageRunner, project);
  }

  @Nonnull
  @Override
  public CoverageEngine getCoverageEngine() {
    return GoCoverageEngine.INSTANCE;
  }
}
