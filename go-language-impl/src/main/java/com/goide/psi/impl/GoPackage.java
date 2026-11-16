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

package com.goide.psi.impl;

import com.goide.GoIcons;
import com.goide.project.GoVendoringUtil;
import com.goide.psi.GoFile;
import com.goide.util.GoPackageUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.language.impl.psi.FakePsiElement;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiDirectoryContainer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A Go package: a package name and the directories that contain its files.
 * <p>
 * The default implementation is backed by directories: its files are the {@link GoFile}s of its directories whose package
 * name is {@link #getName()}. Build systems ({@link GoPackageFactory}, {@link com.goide.psi.impl.imports.GoImportResolver})
 * may subclass it to define the exact list of files, which may live in several directories, while one directory may hold
 * several packages.
 */
public class GoPackage extends FakePsiElement implements PsiDirectoryContainer {
    private final Project myProject;
    private final String myName;
    private final VirtualFile[] myDirectories;

    public GoPackage(Project project, String name, VirtualFile... directories) {
        myProject = project;
        myName = name;
        myDirectories = directories;
    }

    public GoPackage(Project project, String name, PsiDirectory... directories) {
        this(project, name, Arrays.stream(directories).map(PsiDirectory::getVirtualFile).toArray(VirtualFile[]::new));
    }

    /**
     * @return the package of the file: the one of the first {@link GoPackageFactory} that answers, otherwise the files of its
     * directory with the same package name; {@code null} if the file has no directory or no package name
     */
    @RequiredReadAction
    public static @Nullable GoPackage of(GoFile file) {
        GoFile goFile = file.getOriginalFile() instanceof GoFile originalFile ? originalFile : file;
        GoPackage goPackage = Application.get().getExtensionPoint(GoPackageFactory.class)
            .computeSafeIfAny(factory -> factory.createPackage(goFile));
        if (goPackage != null) {
            return goPackage;
        }
        PsiDirectory directory = goFile.getParent();
        String packageName = file.getPackageName();
        if (directory == null || StringUtil.isEmpty(packageName)) {
            return null;
        }
        return in(directory, packageName);
    }

    /**
     * @return the package of the given name in the directory: the one of the first {@link GoPackageFactory} that answers,
     * otherwise the directory-backed package
     */
    @RequiredReadAction
    public static GoPackage in(PsiDirectory directory, String packageName) {
        GoPackage goPackage = Application.get().getExtensionPoint(GoPackageFactory.class)
            .computeSafeIfAny(factory -> factory.createPackage(packageName, directory));
        if (goPackage != null) {
            return goPackage;
        }
        return new GoPackage(directory.getProject(), packageName, directory);
    }

    /**
     * @return one package per package name declared by the Go files of the directory, in the order of the files
     */
    @RequiredReadAction
    public static List<GoPackage> allIn(@Nullable PsiDirectory directory) {
        if (directory == null) {
            return List.of();
        }
        Set<String> packageNames = new LinkedHashSet<>();
        for (PsiFile file : directory.getFiles()) {
            if (file instanceof GoFile goFile) {
                String packageName = goFile.getPackageName();
                if (StringUtil.isNotEmpty(packageName)) {
                    packageNames.add(packageName);
                }
            }
        }
        List<GoPackage> result = new ArrayList<>(packageNames.size());
        for (String packageName : packageNames) {
            result.add(in(directory, packageName));
        }
        return result;
    }

    @Override
    public String getName() {
        return myName;
    }

    /**
     * @return the directories of the package files
     */
    public VirtualFile[] getVirtualDirectories() {
        return myDirectories;
    }

    @Override
    @RequiredReadAction
    public PsiDirectory[] getDirectories() {
        return getDirectories(null);
    }

    @Override
    @RequiredReadAction
    public PsiDirectory[] getDirectories(@Nullable GlobalSearchScope scope) {
        PsiManager psiManager = getManager();
        List<PsiDirectory> result = new ArrayList<>(myDirectories.length);
        for (VirtualFile directory : myDirectories) {
            if (!directory.isValid() || scope != null && !scope.contains(directory)) {
                continue;
            }
            PsiDirectory psiDirectory = psiManager.findDirectory(directory);
            if (psiDirectory != null) {
                result.add(psiDirectory);
            }
        }
        return result.toArray(PsiDirectory.EMPTY_ARRAY);
    }

    /**
     * @return the files of the package, unfiltered by build constraints
     */
    @RequiredReadAction
    public Collection<PsiFile> files() {
        List<PsiFile> result = new ArrayList<>();
        for (PsiDirectory directory : getDirectories()) {
            for (PsiFile file : directory.getFiles()) {
                if (file instanceof GoFile goFile && myName.equals(goFile.getPackageName())) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    /**
     * Processes the {@link #files()} whose virtual file passes the filter.
     *
     * @return {@code false} if the processor stopped the processing
     */
    @RequiredReadAction
    public boolean processFiles(Predicate<? super PsiFile> processor, Predicate<VirtualFile> virtualFileFilter) {
        if (!isValid()) {
            return true;
        }
        for (PsiFile file : files()) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile != null && !virtualFileFilter.test(virtualFile)) {
                continue;
            }
            if (!processor.test(file)) {
                return false;
            }
        }
        return true;
    }

    @RequiredReadAction
    public boolean processFiles(Predicate<? super PsiFile> processor) {
        return processFiles(processor, virtualFile -> true);
    }

    @Override
    @RequiredReadAction
    public boolean isValid() {
        if (myProject.isDisposed()) {
            return false;
        }
        for (VirtualFile directory : myDirectories) {
            if (!directory.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the import path of the first directory, {@code null} if it is not under a GOPATH, SDK or Go module root
     */
    @RequiredReadAction
    public @Nullable String getImportPath(boolean withVendoring) {
        PsiDirectory[] directories = getDirectories();
        return directories.length > 0 ? GoPackageUtil.getImportPath(directories[0], withVendoring) : null;
    }

    @RequiredReadAction
    public List<String> getAllImportPaths(boolean withVendoring) {
        Set<String> result = new LinkedHashSet<>();
        for (PsiDirectory directory : getDirectories()) {
            String importPath = GoPackageUtil.getImportPath(directory, withVendoring);
            if (importPath != null) {
                result.add(importPath);
            }
        }
        return List.copyOf(result);
    }

    /**
     * @return the import path to use for this package from the context module, vendoring aware
     */
    @RequiredReadAction
    public @Nullable String getCanonicalImport(@Nullable Module contextModule) {
        return getImportPath(GoVendoringUtil.isVendoringEnabled(contextModule));
    }

    /**
     * @return the element to navigate to, the first directory by default
     */
    @RequiredReadAction
    public @Nullable PsiElement getNavigableElement() {
        PsiDirectory[] directories = getDirectories();
        return directories.length > 0 ? directories[0] : null;
    }

    @Override
    @RequiredReadAction
    public PsiElement getNavigationElement() {
        PsiElement navigable = getNavigableElement();
        return navigable != null ? navigable : this;
    }

    @Override
    @RequiredReadAction
    public void navigate(boolean requestFocus) {
        PsiElement navigable = getNavigableElement();
        if (navigable instanceof Navigatable navigatable && navigatable.canNavigate()) {
            navigatable.navigate(requestFocus);
        }
    }

    @Override
    @RequiredReadAction
    public boolean canNavigate() {
        return getNavigableElement() instanceof Navigatable navigatable && navigatable.canNavigate();
    }

    @Override
    public Project getProject() {
        return myProject;
    }

    @Override
    public PsiManager getManager() {
        return PsiManager.getInstance(myProject);
    }

    @Override
    public @Nullable PsiElement getParent() {
        return null;
    }

    @Override
    public @Nullable PsiFile getContainingFile() {
        return null;
    }

    @Override
    public @Nullable Image getIcon() {
        return GoIcons.PACKAGE;
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return equals(another);
    }

    @Override
    public String toString() {
        return "Package: " + myName;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GoPackage that = (GoPackage) o;
        return myName.equals(that.myName) && Arrays.equals(myDirectories, that.myDirectories);
    }

    @Override
    public int hashCode() {
        return 31 * myName.hashCode() + Arrays.hashCode(myDirectories);
    }
}
