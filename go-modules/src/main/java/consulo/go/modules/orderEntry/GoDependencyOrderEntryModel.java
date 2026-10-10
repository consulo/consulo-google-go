/*
 * Copyright 2013-2026 consulo.io
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

package consulo.go.modules.orderEntry;

import consulo.content.RootProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryModel;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A module of the build list that is not part of the project: its sources live in the module cache,
 * in {@code vendor}, or in the directory a {@code replace} points to.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoDependencyOrderEntryModel implements CustomOrderEntryModel {
    private final String myModulePath;
    private final @Nullable String myVersion;
    private final @Nullable String myDirectoryUrl;
    private final @Nullable String myReplacement;
    private final boolean myIndirect;
    private final GoDependencyRootProvider myRootProvider = new GoDependencyRootProvider(this);

    public GoDependencyOrderEntryModel(String modulePath,
                                       @Nullable String version,
                                       @Nullable String directoryUrl,
                                       @Nullable String replacement,
                                       boolean indirect) {
        myModulePath = modulePath;
        myVersion = version;
        myDirectoryUrl = directoryUrl;
        myReplacement = replacement;
        myIndirect = indirect;
    }

    public String getModulePath() {
        return myModulePath;
    }

    public @Nullable String getVersion() {
        return myVersion;
    }

    /**
     * @return the root of the module sources, {@code null} while the module is not downloaded
     */
    public @Nullable String getDirectoryUrl() {
        return myDirectoryUrl;
    }

    /**
     * @return where a {@code replace} directive sends the module: a module path with a version, or a directory
     */
    public @Nullable String getReplacement() {
        return myReplacement;
    }

    public boolean isIndirect() {
        return myIndirect;
    }

    public @Nullable VirtualFile findDirectory() {
        if (myDirectoryUrl == null) {
            return null;
        }
        VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(myDirectoryUrl);
        return file != null && file.isDirectory() ? file : null;
    }

    @Override
    public void bind(ModuleRootLayer moduleRootLayer) {
    }

    @Override
    public String getPresentableName() {
        return myVersion != null ? myModulePath + " " + myVersion : myModulePath;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isSynthetic() {
        return true;
    }

    @Override
    public RootProvider getRootProvider() {
        return myRootProvider;
    }

    @Override
    public VirtualFile[] getExcludedRoots() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public Object getEqualObject() {
        return GoDependencyOrderEntryType.ID + " " + myModulePath;
    }

    @Override
    public boolean isEquivalentTo(CustomOrderEntryModel model) {
        return model instanceof GoDependencyOrderEntryModel other
            && myModulePath.equals(other.myModulePath)
            && Objects.equals(myVersion, other.myVersion)
            && Objects.equals(myDirectoryUrl, other.myDirectoryUrl)
            && Objects.equals(myReplacement, other.myReplacement)
            && myIndirect == other.myIndirect;
    }

    @Override
    public GoDependencyOrderEntryModel clone() {
        return new GoDependencyOrderEntryModel(myModulePath, myVersion, myDirectoryUrl, myReplacement, myIndirect);
    }
}
