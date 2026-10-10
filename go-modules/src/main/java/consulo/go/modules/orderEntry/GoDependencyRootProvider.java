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

import consulo.content.RootProviderBase;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.virtualFileSystem.VirtualFile;

/**
 * Serves the module directory both as sources and as binaries, the same way the Go SDK serves its {@code src}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
class GoDependencyRootProvider extends RootProviderBase {
    private final GoDependencyOrderEntryModel myModel;

    GoDependencyRootProvider(GoDependencyOrderEntryModel model) {
        myModel = model;
    }

    @Override
    public String[] getUrls(String rootType) {
        String url = myModel.getDirectoryUrl();
        return url != null && isServed(rootType) ? new String[]{url} : new String[0];
    }

    @Override
    public VirtualFile[] getFiles(String rootType) {
        VirtualFile directory = isServed(rootType) ? myModel.findDirectory() : null;
        return directory != null ? new VirtualFile[]{directory} : VirtualFile.EMPTY_ARRAY;
    }

    private static boolean isServed(String rootType) {
        return SourcesOrderRootType.ID.equals(rootType) || BinariesOrderRootType.ID.equals(rootType);
    }
}
