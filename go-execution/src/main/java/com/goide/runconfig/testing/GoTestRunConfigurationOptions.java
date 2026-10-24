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

package com.goide.runconfig.testing;

import com.dslplatform.json.CompiledJson;
import com.goide.runconfig.GoRunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class GoTestRunConfigurationOptions extends GoRunConfigurationOptions {
    private @Nullable String myFramework;
    private @Nullable String myKind;
    private @Nullable String myPackage;
    private @Nullable String myDirectoryPath;
    private @Nullable String myFilePath;
    private @Nullable String myPattern;

    @Attribute("framework")
    public @Nullable String getFramework() {
        return myFramework;
    }

    public void setFramework(@Nullable String framework) {
        myFramework = StringUtil.nullize(framework, true);
    }

    @Attribute("kind")
    public @Nullable String getKind() {
        return myKind;
    }

    public void setKind(@Nullable String kind) {
        myKind = StringUtil.nullize(kind, true);
    }

    @Attribute("package")
    public @Nullable String getPackage() {
        return myPackage;
    }

    public void setPackage(@Nullable String aPackage) {
        myPackage = StringUtil.nullize(aPackage, true);
    }

    @Attribute("directory")
    public @Nullable String getDirectoryPath() {
        return myDirectoryPath;
    }

    public void setDirectoryPath(@Nullable String directoryPath) {
        myDirectoryPath = StringUtil.nullize(directoryPath, true);
    }

    @Attribute("filePath")
    public @Nullable String getFilePath() {
        return myFilePath;
    }

    public void setFilePath(@Nullable String filePath) {
        myFilePath = StringUtil.nullize(filePath, true);
    }

    @Attribute("pattern")
    public @Nullable String getPattern() {
        return myPattern;
    }

    public void setPattern(@Nullable String pattern) {
        myPattern = StringUtil.nullize(pattern, true);
    }
}
