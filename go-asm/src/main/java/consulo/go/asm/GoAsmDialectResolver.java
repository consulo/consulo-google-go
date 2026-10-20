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
package consulo.go.asm;

import consulo.annotation.component.ExtensionImpl;
import consulo.asm.language.AsmDefaultVersionResolver;
import consulo.asm.language.AsmLanguage;
import consulo.asm.plan9.Plan9LanguageVersion;
import consulo.go.module.extension.GoModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.language.version.LanguageVersion;
import consulo.module.Module;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * The go tool assembles the {@code .s} files of a Go module with its Plan 9 assembler. {@code .S} files are not
 * listed here: cgo hands them to the C compiler.
 *
 * @author VISTALL
 * @since 2026-10-20
 */
@ExtensionImpl
public class GoAsmDialectResolver implements AsmDefaultVersionResolver {
    private final ProjectFileIndex myFileIndex;

    @Inject
    public GoAsmDialectResolver(ProjectFileIndex fileIndex) {
        myFileIndex = fileIndex;
    }

    @Override
    public @Nullable LanguageVersion tryResolveDefaultVersion(VirtualFile file) {
        if (!"s".equals(file.getExtension()) || !isUsedByGoModule(file)) {
            return null;
        }
        return AsmLanguage.INSTANCE.findVersionByClass(Plan9LanguageVersion.class);
    }

    private boolean isUsedByGoModule(VirtualFile file) {
        Module module = myFileIndex.getModuleForFile(file);
        if (module != null) {
            return hasGo(module);
        }
        // GOROOT sources and the module cache belong to no module; they are libraries of the modules using them
        for (OrderEntry entry : myFileIndex.getOrderEntriesForFile(file)) {
            if (hasGo(entry.getOwnerModule())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasGo(Module module) {
        return ModuleUtilCore.getExtension(module, GoModuleExtension.class) != null;
    }
}
