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
 * Running Go: run configurations, tests and coverage, the compiler, external tools, and debugging with Delve.
 */
module consulo.go.execution {
    requires transitive consulo.go.language.editor.impl;

    requires consulo.ide.api;
    requires consulo.module.ui.api;
    requires consulo.language.impl;
    requires consulo.external.system.api;
    requires consulo.compiler.api;
    requires consulo.build.ui.api;
    requires consulo.execution.coverage.api;
    requires consulo.execution.test.api;
    requires consulo.execution.test.sm.api;
    requires consulo.version.control.system.api;

    requires consulo.execution.debugger.dap;
    requires consulo.nativeDev.debugger.api;
    requires consulo.nativeDev.debugger.dap;

    requires com.intellij.regexp;
    requires build.serviceMessages;

    // TODO remove in future;
    requires java.desktop;

    exports com.goide.actions.tool;
    exports com.goide.compiler;
    exports com.goide.generate;
    exports com.goide.runconfig;
    exports com.goide.runconfig.application;
    exports com.goide.runconfig.before;
    exports com.goide.runconfig.file;
    exports com.goide.runconfig.testing;
    exports com.goide.runconfig.testing.coverage;
    exports com.goide.runconfig.testing.frameworks.gobench;
    exports com.goide.runconfig.testing.frameworks.gocheck;
    exports com.goide.runconfig.testing.frameworks.gotest;
    exports com.goide.runconfig.testing.ui;
    exports com.goide.runconfig.ui;
    exports consulo.go.debug;
}
