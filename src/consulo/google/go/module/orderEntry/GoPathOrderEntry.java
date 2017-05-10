/*
 * Copyright 2013-2017 consulo.io
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

package consulo.google.go.module.orderEntry;

import com.goide.sdk.GoEnvironmentGoPathModificationTracker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootPolicy;
import com.intellij.openapi.roots.impl.ClonableOrderEntry;
import com.intellij.openapi.roots.impl.OrderEntryBaseImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import consulo.roots.OrderEntryWithTracking;
import consulo.roots.impl.ModuleRootLayerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoPathOrderEntry extends OrderEntryBaseImpl implements OrderEntryWithTracking, ClonableOrderEntry {

  public GoPathOrderEntry(@NotNull ModuleRootLayerImpl rootLayer) {
    super(GoPathOrderEntryType.getInstance(), rootLayer);
  }

  @NotNull
  public String[] getUrls(OrderRootType orderRootType) {
    Collection<VirtualFile> goEnvironmentGoPathRoots = GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots();
    return goEnvironmentGoPathRoots.stream().map(VirtualFile::getUrl).toArray(String[]::new);
  }

  @NotNull
  @Override
  public VirtualFile[] getFiles(OrderRootType orderRootType) {
    return ContainerUtil.toArray(GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots(), VirtualFile.EMPTY_ARRAY);
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "GOPATH";
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @NotNull
  @Override
  public Module getOwnerModule() {
    return getRootModel().getModule();
  }

  @Override
  public <R> R accept(RootPolicy<R> rootPolicy, @Nullable R r) {
    return rootPolicy.visitOrderEntry(this, r);
  }

  @Override
  public boolean isEquivalentTo(@NotNull OrderEntry orderEntry) {
    return orderEntry instanceof GoPathOrderEntry;
  }

  @Override
  public boolean isSynthetic() {
    return true;
  }

  @Nullable
  @Override
  public Object getEqualObject() {
    return "GOPATH";
  }

  @Override
  public OrderEntry cloneEntry(ModuleRootLayerImpl layer) {
    return new GoPathOrderEntry(layer);
  }
}
