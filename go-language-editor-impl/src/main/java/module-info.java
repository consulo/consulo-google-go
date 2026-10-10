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
 * Editor support of Go: highlighting, completion, inspections and quick fixes, intentions, refactorings,
 * formatting, navigation and live templates.
 */
module consulo.go.language.editor.impl {
    requires transitive consulo.go.language.impl;

    requires consulo.ide.api;
    requires consulo.language.impl;
    requires consulo.language.editor.impl;
    requires consulo.language.editor.refactoring.api;
    requires consulo.language.code.style.ui.api;
    requires consulo.external.service.api;
    requires consulo.find.api;

    requires com.intellij.regexp;

    // TODO remove in future;
    requires java.desktop;

    exports com.goide.actions.file;
    exports com.goide.appengine;
    exports com.goide.codeInsight;
    exports com.goide.codeInsight.imports;
    exports com.goide.codeInsight.unwrap;
    exports com.goide.completion;
    exports com.goide.editor;
    exports com.goide.editor.marker;
    exports com.goide.editor.smart;
    exports com.goide.editor.surround;
    exports com.goide.formatter;
    exports com.goide.formatter.settings;
    exports com.goide.go;
    exports com.goide.highlighting;
    exports com.goide.highlighting.exitpoint;
    exports com.goide.inspections;
    exports com.goide.inspections.suppression;
    exports com.goide.inspections.unresolved;
    exports com.goide.intentions;
    exports com.goide.marker;
    exports com.goide.quickfix;
    exports com.goide.refactor;
    exports com.goide.regexp;
    exports com.goide.template;
    exports com.goide.tree;
    exports com.goide.usages;
    exports consulo.go.highlighting;
    exports consulo.go.inspection;
    exports consulo.go.template;

    opens com.goide.codeInsight.imports to consulo.util.xml.serializer;
    opens com.goide.inspections to consulo.util.xml.serializer;
}
