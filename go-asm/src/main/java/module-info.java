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
 * Go and its Plan 9 assembly: the dialect of package {@code .s} files and navigation between
 * body-less Go functions and their {@code TEXT ·f(SB)} implementations.
 */
@NullMarked
module consulo.go.asm {
    requires consulo.go.language.impl;
    requires consulo.asm.language.api;
    requires consulo.asm.language.impl;
    requires consulo.asm.plan9.impl;
    requires consulo.language.editor.api;
    requires consulo.language.editor.ui.api;
    requires consulo.localize.api;
    requires consulo.ui.api;

    exports consulo.go.asm;
}
