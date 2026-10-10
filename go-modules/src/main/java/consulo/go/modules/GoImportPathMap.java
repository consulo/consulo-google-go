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

package consulo.go.modules;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.go.modules.orderEntry.GoDependencyEntries;
import consulo.go.modules.orderEntry.GoDependencyOrderEntryModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.orderEntry.ModuleOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Import path to directory, as the {@code go} command sees it from one Go module: an import path belongs to the module
 * with the longest matching module path, and a vendored module wins over the module cache.
 * <p>
 * The map is built from the project model the import produced: the module itself, the modules of its workspace or the
 * modules it depends on, and its {@link GoDependencyOrderEntryModel Go dependencies}. The standard library is not in it,
 * it resolves through the SDK as before.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoImportPathMap {
    private record Root(String importPath, VirtualFile directory, boolean main) {
    }

    private final List<Root> myRoots;
    private final List<VirtualFile> myVendorDirectories;

    private GoImportPathMap(List<Root> roots, List<VirtualFile> vendorDirectories) {
        myRoots = roots;
        myVendorDirectories = vendorDirectories;
    }

    /**
     * @return the map of a module imported from a Go module, {@code null} for any other module
     */
    @RequiredReadAction
    public static @Nullable GoImportPathMap forModule(@Nullable Module module) {
        if (module == null || module.isDisposed()) {
            return null;
        }
        Project project = module.getProject();
        return CachedValuesManager.getManager(project).getCachedValue(module, () -> CachedValueProvider.Result.create(compute(module),
            ProjectRootManager.getInstance(project),
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS));
    }

    /**
     * Like {@link #forModule(Module)}, also for a file outside every module: a file of a dependency resolves its imports
     * like the modules having that dependency do.
     */
    @RequiredReadAction
    public static @Nullable GoImportPathMap forFile(Project project, @Nullable Module module, @Nullable VirtualFile file) {
        GoImportPathMap map = forModule(module);
        if (map != null || file == null || project.isDisposed()) {
            return map;
        }
        for (OrderEntry orderEntry : ProjectFileIndex.getInstance(project).getOrderEntriesForFile(file)) {
            map = forModule(orderEntry.getOwnerModule());
            if (map != null) {
                return map;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static @Nullable GoImportPathMap compute(Module module) {
        GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
        if (extension == null) {
            return null;
        }

        List<Root> roots = new ArrayList<>();
        Set<Module> mainModules = new LinkedHashSet<>();
        mainModules.add(module);
        String workFile = extension.getWorkFile();
        if (workFile != null) {
            // every module of a workspace is a main module, visible to every other one
            for (Module other : ModuleManager.getInstance(module.getProject()).getModules()) {
                GoModulesModuleExtension otherExtension = GoModulesModuleExtension.find(other);
                if (otherExtension != null && workFile.equals(otherExtension.getWorkFile())) {
                    mainModules.add(other);
                }
            }
        }

        for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
            if (orderEntry instanceof ModuleOrderEntry moduleOrderEntry) {
                Module dependency = moduleOrderEntry.getModule();
                if (dependency != null) {
                    mainModules.add(dependency);
                }
                continue;
            }
            GoDependencyOrderEntryModel model = GoDependencyEntries.getModel(orderEntry);
            if (model == null) {
                continue;
            }
            VirtualFile directory = model.findDirectory();
            if (directory != null) {
                roots.add(new Root(model.getModulePath(), directory, false));
            }
        }

        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        for (Module mainModule : mainModules) {
            GoModulesModuleExtension mainExtension = GoModulesModuleExtension.find(mainModule);
            String modulePath = mainExtension != null ? mainExtension.getModulePath() : null;
            Path moduleDirectory = mainExtension != null ? mainExtension.getModuleDirectory() : null;
            VirtualFile directory = moduleDirectory != null ? fileSystem.findFileByNioFile(moduleDirectory) : null;
            if (modulePath != null && directory != null) {
                roots.add(new Root(modulePath, directory, true));
            }
        }
        roots.sort(Comparator.comparingInt((Root root) -> root.importPath().length()).reversed());

        List<VirtualFile> vendorDirectories = new ArrayList<>();
        Path ownDirectory = extension.getModuleDirectory();
        // in workspace mode the vendor directory sits next to go.work, otherwise next to go.mod
        Path vendorOwner = workFile != null ? Path.of(workFile).getParent() : ownDirectory;
        VirtualFile vendorOwnerDirectory = vendorOwner != null ? fileSystem.findFileByNioFile(vendorOwner) : null;
        VirtualFile vendor = vendorOwnerDirectory != null ? vendorOwnerDirectory.findChild(GoModulesConstants.VENDOR) : null;
        if (vendor != null && vendor.isDirectory() && vendor.findChild(GoModulesConstants.VENDOR_MODULES_TXT) != null) {
            vendorDirectories.add(vendor);
        }
        return new GoImportPathMap(List.copyOf(roots), List.copyOf(vendorDirectories));
    }

    /**
     * @return the package directory of {@code importPath}, {@code null} when no module provides it
     */
    public @Nullable VirtualFile findDirectory(String importPath) {
        Root root = findRoot(importPath);
        if (root != null && root.main()) {
            return resolve(root, importPath);
        }
        for (VirtualFile vendor : myVendorDirectories) {
            VirtualFile vendored = vendor.findFileByRelativePath(importPath);
            if (vendored != null && vendored.isDirectory()) {
                return vendored;
            }
        }
        return root != null ? resolve(root, importPath) : null;
    }

    /**
     * @return the roots of the modules whose module path continues {@code partialImportPath}, e.g. every
     * {@code github.com/...} module for {@code github.com}
     */
    public Collection<VirtualFile> findPrefixDirectories(String partialImportPath) {
        String prefix = partialImportPath + "/";
        Set<VirtualFile> directories = new LinkedHashSet<>();
        for (Root root : myRoots) {
            if (root.importPath().startsWith(prefix)) {
                directories.add(root.directory());
            }
        }
        return directories;
    }

    /**
     * @return the import path of the package in {@code directory}, {@code null} when it is in none of the modules
     */
    public @Nullable String getImportPath(VirtualFile directory) {
        for (VirtualFile vendor : myVendorDirectories) {
            if (VirtualFileUtil.isAncestor(vendor, directory, true)) {
                return VirtualFileUtil.getRelativePath(directory, vendor, '/');
            }
        }

        Root best = null;
        for (Root root : myRoots) {
            if (VirtualFileUtil.isAncestor(root.directory(), directory, false)
                && (best == null || root.directory().getPath().length() > best.directory().getPath().length())) {
                best = root;
            }
        }
        if (best == null) {
            return null;
        }
        String relativePath = VirtualFileUtil.getRelativePath(directory, best.directory(), '/');
        return relativePath == null || relativePath.isEmpty() ? best.importPath() : best.importPath() + "/" + relativePath;
    }

    /**
     * @return the directories of every module and vendor directory the map knows
     */
    public Set<VirtualFile> getRootDirectories() {
        Set<VirtualFile> directories = new LinkedHashSet<>();
        for (Root root : myRoots) {
            directories.add(root.directory());
        }
        directories.addAll(myVendorDirectories);
        return directories;
    }

    private @Nullable Root findRoot(String importPath) {
        for (Root root : myRoots) {
            String modulePath = root.importPath();
            if (importPath.equals(modulePath) || importPath.startsWith(modulePath) && importPath.charAt(modulePath.length()) == '/') {
                return root;
            }
        }
        return null;
    }

    private static @Nullable VirtualFile resolve(Root root, String importPath) {
        if (importPath.length() == root.importPath().length()) {
            return root.directory();
        }
        VirtualFile directory = root.directory().findFileByRelativePath(importPath.substring(root.importPath().length() + 1));
        return directory != null && directory.isDirectory() ? directory : null;
    }
}
