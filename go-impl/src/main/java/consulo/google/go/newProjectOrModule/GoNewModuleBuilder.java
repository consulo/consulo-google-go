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
package consulo.google.go.newProjectOrModule;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.google.go.module.extension.GoMutableModuleExtension;
import consulo.google.go.module.orderEntry.GoPathOrderEntryModel;
import consulo.google.go.module.orderEntry.GoPathOrderEntryType;
import consulo.ide.newModule.NewModuleBuilder;
import consulo.ide.newModule.NewModuleContext;
import consulo.ide.newModule.UnzipNewModuleBuilderProcessor;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableModuleRootLayer;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.wizard.WizardStep;


import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 05-May-17
 */
@ExtensionImpl
public class GoNewModuleBuilder implements NewModuleBuilder {
    @Override
    public void setupContext(NewModuleContext context) {
        NewModuleContext.Group group = context.createGroup("go", "Go");

        group.add(
            "Console Application",
            PlatformIconGroup.runconfigurationsApplication(),
            new UnzipNewModuleBuilderProcessor<GoNewModuleContext>("/moduleTemplates/GoHelloWorld.zip") {
                @Override
                public GoNewModuleContext createContext(boolean isNewProject) {
                    return new GoNewModuleContext(isNewProject);
                }

                @Override
                public void buildSteps(Consumer<WizardStep<GoNewModuleContext>> consumer, GoNewModuleContext context) {
                    consumer.accept(new GoNewModuleSetupStep(context));
                }

                @Override
                @RequiredReadAction
                public void process(
                    GoNewModuleContext context,
                    ContentEntry contentEntry,
                    ModifiableRootModel modifiableRootModel
                ) {
                    unzip(modifiableRootModel);

                    GoMutableModuleExtension goModuleExtension =
                        modifiableRootModel.getExtensionWithoutCheck(GoMutableModuleExtension.class);
                    assert goModuleExtension != null;

                    goModuleExtension.setEnabled(true);

                    ModifiableModuleRootLayer moduleRootLayer = (ModifiableModuleRootLayer) goModuleExtension.getModuleRootLayer();
                    moduleRootLayer.addCustomOderEntry(GoPathOrderEntryType.getInstance(), new GoPathOrderEntryModel());
                    Sdk sdk = context.getSdk();
                    if (sdk != null) {
                        goModuleExtension.getInheritableSdk().set(null, sdk);
                        modifiableRootModel.addModuleExtensionSdkEntry(goModuleExtension);
                    }
                }
            }
        );
    }
}