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

package com.goide.stubs.index;

import com.goide.runconfig.testing.GoTestFinder;
import consulo.application.progress.ProgressManager;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.content.ContentIterator;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.stub.IdFilter;
import consulo.logging.Logger;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.util.lang.function.Condition;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.VirtualFileWithId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;

public class GoIdFilter extends IdFilter {
  public static final Logger LOG = Logger.getInstance(GoIdFilter.class);
  private static final Key<CachedValue<IdFilter>> PRODUCTION_FILTER = Key.create("PRODUCTION");
  private static final Key<CachedValue<IdFilter>> TESTS_FILTER = Key.create("TESTS");

  private final BitSet myIdSet;

  private GoIdFilter(@Nonnull BitSet idSet) {
    myIdSet = idSet;
  }

  @Override
  public boolean containsFileId(int id) {
    return id >= 0 && myIdSet.get(id);
  }

  public static IdFilter getProductionFilter(@Nonnull Project project) {
    return createIdFilter(project, PRODUCTION_FILTER, file -> !file.isDirectory() && !GoTestFinder.isTestFile(file));
  }

  public static IdFilter getTestsFilter(@Nonnull Project project) {
    return createIdFilter(project, TESTS_FILTER, file -> !file.isDirectory() && GoTestFinder.isTestFile(file));
  }

  private static IdFilter createIdFilter(@Nonnull Project project,
                                         @Nonnull Key<CachedValue<IdFilter>> cacheKey,
                                         @Nonnull Condition<VirtualFile> filterCondition) {
    return CachedValuesManager.getManager(project).getCachedValue(project, cacheKey, () -> {
      BitSet bitSet = new BitSet();
      ContentIterator iterator = fileOrDir -> {
        if (filterCondition.value(fileOrDir)) {
          addToBitSet(bitSet, fileOrDir);
        }
        ProgressManager.checkCanceled();
        return true;
      };
      FileBasedIndex.getInstance().iterateIndexableFiles(iterator, project, null);
      return CachedValueProvider.Result.create(new GoIdFilter(bitSet), ProjectRootManager.getInstance(project),
                                               VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
    }, false);
  }

  @Nullable
  public static IdFilter getFilesFilter(@Nonnull GlobalSearchScope scope) {
    if (scope instanceof GlobalSearchScope.FilesScope) {
      BitSet bitSet = new BitSet();
      for (VirtualFile file : (GlobalSearchScope.FilesScope)scope) {
        addToBitSet(bitSet, file);
      }
      return new GoIdFilter(bitSet);
    }
    return null;
  }

  private static void addToBitSet(@Nonnull BitSet set, @Nonnull VirtualFile file) {
    if (file instanceof VirtualFileWithId) {
      int id = ((VirtualFileWithId)file).getId();
      if (id < 0) id = -id; // workaround for encountering invalid files, see EA-49915, EA-50599
      set.set(id);
    }
  }
}
