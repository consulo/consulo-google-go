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

package com.goide.runconfig;

import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

/**
 * A labeled form row whose label and input are shown or hidden together.
 */
public record GoFormRow(Component label, Component field) {
    @RequiredUIAccess
    public static GoFormRow add(FormBuilder builder, LocalizeValue label, Component field) {
        Label labelComponent = Label.create(label);
        builder.addLabeled(labelComponent, field);
        return new GoFormRow(labelComponent, field);
    }

    @RequiredUIAccess
    public void setVisible(boolean visible) {
        label.setVisible(visible);
        field.setVisible(visible);
    }
}
