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

import org.jspecify.annotations.NullMarked;

/**
 * Projects of {@code go.mod} and {@code go.work} files: the external system that imports them through the
 * {@code go} binary, their language, and the New Project wizard.
 */
@NullMarked
module consulo.go.modules {
    requires transitive consulo.go.toolchain;

    requires consulo.ide.api;
    requires consulo.external.system.api;
    requires consulo.module.creation.api;
    requires consulo.module.ui.api;
    requires consulo.language.impl;

    requires com.google.gson;

    exports consulo.go.gomod;
    exports consulo.go.gomod.editor;
    exports consulo.go.gomod.highlighting;
    exports consulo.go.gomod.lexer;
    exports consulo.go.gomod.parser;
    exports consulo.go.gomod.psi;
    exports consulo.go.gomod.psi.impl;
    exports consulo.go.modules;
    exports consulo.go.modules.action;
    exports consulo.go.modules.externalSystem;
    exports consulo.go.modules.importing;
    exports consulo.go.modules.module;
    exports consulo.go.modules.notification;
    exports consulo.go.modules.orderEntry;
    exports consulo.go.modules.setting;
    exports consulo.go.modules.ui;
    exports consulo.go.newProjectOrModule;

    opens consulo.go.modules.setting to consulo.util.xml.serializer;
}
