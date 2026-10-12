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

/**
 * The Go SDK, the Go module extension and the environment of the {@code go} binary.
 */
module consulo.go.toolchain {
    requires consulo.module.ui.api;
    requires consulo.execution.api;

    exports com.goide.configuration;
    exports com.goide.project;
    exports com.goide.sdk;
    exports consulo.go;
    exports consulo.go.icon;
    exports consulo.go.localize;
    exports consulo.go.module.extension;
    exports consulo.go.module.extension.ui;

    opens com.goide.project to consulo.util.xml.serializer;
}
