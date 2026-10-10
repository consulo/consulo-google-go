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
 * The Go language: lexer, parser, PSI, stubs and indexes, resolve, and the runner of {@code go} commands.
 */
module consulo.go.language.impl {
    requires transitive consulo.go.toolchain;
    requires transitive consulo.go.modules;

    requires consulo.language.impl;
    requires consulo.language.editor.refactoring.api;
    requires consulo.execution.api;

    exports com.goide;
    exports com.goide.lexer;
    exports com.goide.parser;
    exports com.goide.psi;
    exports com.goide.psi.impl;
    exports com.goide.psi.impl.imports;
    exports com.goide.psi.impl.manipulator;
    exports com.goide.stubs;
    exports com.goide.stubs.index;
    exports com.goide.stubs.types;
    exports com.goide.util;
    exports consulo.go.language;
    exports consulo.go.language.psi;
    exports consulo.go.language.testing;
}
