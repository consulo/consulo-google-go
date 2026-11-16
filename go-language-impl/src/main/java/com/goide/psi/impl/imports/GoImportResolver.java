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

package com.goide.psi.impl.imports;

import com.goide.psi.impl.GoPackage;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.Application;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.ThreeState;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Resolves Go import paths to packages, for build systems that define packages by an import path and a list of source files
 * (Bazel `importpath`) instead of by a directory.
 * <p>
 * Resolvers are asked in their {@code order}. A {@code null} result (or {@link ThreeState#UNSURE}) means "not mine": the next
 * resolver, and finally the built-in GOPATH / go.mod directory resolution, is used.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface GoImportResolver {
    /**
     * @return the packages of the import path, or {@code null} if this resolver does not handle it
     */
    @RequiredReadAction
    @Nullable Collection<? extends GoPackage> resolve(
        String importPath,
        Project project,
        @Nullable Module module,
        @Nullable ResolveState resolveState
    );

    default ThreeState supportsRelativeImportPaths(Project project, @Nullable Module module) {
        return ThreeState.UNSURE;
    }

    default ThreeState supportsInternalPackages(Project project, @Nullable Module module) {
        return ThreeState.UNSURE;
    }

    default ThreeState supportsSdkInternalPackages(Project project, @Nullable Module module) {
        return ThreeState.UNSURE;
    }

    /**
     * Resolves one path segment of an import string. The results must be {@link consulo.language.psi.PsiFileSystemItem}s.
     *
     * @return the resolve results, or {@code null} if this resolver does not handle the import
     */
    @RequiredReadAction
    default ResolveResult @Nullable [] resolve(GoImportReference reference) {
        return null;
    }

    /**
     * @return the packages of the first resolver that handles the import path, or {@code null} if none does
     */
    @RequiredReadAction
    static @Nullable Collection<? extends GoPackage> resolveByExtensions(
        String importPath,
        Project project,
        @Nullable Module module,
        @Nullable ResolveState resolveState
    ) {
        return Application.get().getExtensionPoint(GoImportResolver.class)
            .computeSafeIfAny(resolver -> resolver.resolve(importPath, project, module, resolveState));
    }

    /**
     * @return the resolve results of the first resolver that handles the reference, or {@code null} if none does
     */
    @RequiredReadAction
    static ResolveResult @Nullable [] resolveByExtensions(GoImportReference reference) {
        return Application.get().getExtensionPoint(GoImportResolver.class)
            .computeSafeIfAny(resolver -> resolver.resolve(reference));
    }

    static boolean isRelativeImportPathsSupported(Project project, @Nullable Module module, boolean defaultValue) {
        return toBoolean(
            Application.get().getExtensionPoint(GoImportResolver.class)
                .computeSafeIfAny(resolver -> known(resolver.supportsRelativeImportPaths(project, module))),
            defaultValue
        );
    }

    static boolean isInternalPackagesSupported(Project project, @Nullable Module module, boolean defaultValue) {
        return toBoolean(
            Application.get().getExtensionPoint(GoImportResolver.class)
                .computeSafeIfAny(resolver -> known(resolver.supportsInternalPackages(project, module))),
            defaultValue
        );
    }

    static boolean isSdkInternalPackagesSupported(Project project, @Nullable Module module, boolean defaultValue) {
        return toBoolean(
            Application.get().getExtensionPoint(GoImportResolver.class)
                .computeSafeIfAny(resolver -> known(resolver.supportsSdkInternalPackages(project, module))),
            defaultValue
        );
    }

    private static @Nullable ThreeState known(ThreeState state) {
        return state == ThreeState.UNSURE ? null : state;
    }

    private static boolean toBoolean(@Nullable ThreeState state, boolean defaultValue) {
        return state == null ? defaultValue : state == ThreeState.YES;
    }
}
