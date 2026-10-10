/*
 * Copyright 2013-2017 consulo.io
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
package consulo.go.newProjectOrModule;

import consulo.annotation.component.ExtensionImpl;
import consulo.go.icon.GoIconGroup;
import consulo.go.localize.GoLocalize;
import consulo.module.creation.scratch.NewModuleBuilder;
import consulo.module.creation.scratch.NewModuleContext;
import consulo.module.creation.scratch.NewModuleContextGroup;

/**
 * The Go group of the New Project wizard: one entry per {@link GoProjectTemplate}.
 *
 * @author VISTALL
 * @since 05-May-17
 */
@ExtensionImpl(id = "go")
public class GoNewModuleBuilder implements NewModuleBuilder {
    @Override
    public void setupContext(NewModuleContext context) {
        NewModuleContextGroup group = context.addGroup("go", GoLocalize.goNewProjectGroup(), GoIconGroup.go());
        for (GoProjectTemplate template : GoProjectTemplate.values()) {
            group.add(template.getDisplayName(), template.getIcon(), new GoNewModuleBuilderProcessor(template));
        }
    }
}
