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

package com.goide.runconfig.application;

import com.dslplatform.json.CompiledJson;
import com.goide.runconfig.GoRunConfigurationWithMainOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class GoApplicationConfigurationOptions extends GoRunConfigurationWithMainOptions {
    private @Nullable String myPackage;
    private @Nullable String myKind;
    private @Nullable String myOutputDirectory;

    @Attribute("package")
    public @Nullable String getPackage() {
        return myPackage;
    }

    public void setPackage(@Nullable String aPackage) {
        myPackage = StringUtil.nullize(aPackage, true);
    }

    @Attribute("kind")
    public @Nullable String getKind() {
        return myKind;
    }

    public void setKind(@Nullable String kind) {
        myKind = StringUtil.nullize(kind, true);
    }

    @Attribute("outputDirectory")
    public @Nullable String getOutputDirectory() {
        return myOutputDirectory;
    }

    public void setOutputDirectory(@Nullable String outputDirectory) {
        myOutputDirectory = StringUtil.nullize(outputDirectory, true);
    }
}
