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

package com.goide.sdk;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.content.OrderRootType;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkType;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.google.go.localize.GoLocalize;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Collection;

@ExtensionImpl
public class GoSdkType extends SdkType {
    public static GoSdkType getInstance() {
        return Application.get().getExtensionPoint(SdkType.class).findExtensionOrFail(GoSdkType.class);
    }

    public GoSdkType() {
        super("GO_SDK", GoLocalize.goLanguageDisplayName(), GoogleGoIconGroup.go());
    }

    @Override
    public Collection<String> suggestHomePaths() {
        return GoSdkUtil.suggestSdkDirectory();
    }

    @Override
    public boolean canCreatePredefinedSdks() {
        return true;
    }

    @Override
    public boolean isValidSdkHome(String path) {
        GoSdkService.LOG.debug("Validating sdk path: " + path);
        String executablePath = GoSdkService.getGoExecutablePath(path);
        if (executablePath == null) {
            GoSdkService.LOG.debug("Go executable is not found: ");
            return false;
        }
        if (!new File(executablePath).canExecute()) {
            GoSdkService.LOG.debug("Go binary cannot be executed: " + path);
            return false;
        }
        if (getVersionString(path) != null) {
            GoSdkService.LOG.debug("Cannot retrieve version for sdk: " + path);
            return true;
        }
        return false;
    }

    @Override
    public String adjustSelectedSdkHome(String homePath) {
        return GoSdkUtil.adjustSdkPath(homePath);
    }

    @Nullable
    @Override
    public String getVersionString(String sdkHome) {
        return GoSdkUtil.retrieveGoVersion(sdkHome);
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
        return type == SourcesOrderRootType.getInstance() || type == BinariesOrderRootType.getInstance();
    }

    @Override
    public void setupSdkPaths(Sdk sdk) {
        String versionString = sdk.getVersionString();
        if (versionString == null) {
            throw new RuntimeException("SDK version is not defined");
        }
        SdkModificator modificator = sdk.getSdkModificator();
        String path = sdk.getHomePath();
        if (path == null) {
            return;
        }
        modificator.setHomePath(path);

        for (VirtualFile file : GoSdkUtil.getSdkDirectoriesToAttach(path, versionString)) {
            modificator.addRoot(file, BinariesOrderRootType.getInstance());
            modificator.addRoot(file, SourcesOrderRootType.getInstance());
        }
        modificator.commitChanges();
    }
}
