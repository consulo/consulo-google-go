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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import consulo.google.go.module.extension.GoMutableModuleExtension;
import consulo.google.go.module.orderEntry.GoPathOrderEntry;
import consulo.ide.impl.UnzipNewModuleBuilderProcessor;
import consulo.ide.newProject.NewModuleBuilder;
import consulo.ide.newProject.NewModuleContext;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.ui.wizard.WizardStep;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoNewModuleBuilder implements NewModuleBuilder
{
	@Override
	public void setupContext(@Nonnull NewModuleContext context)
	{
		NewModuleContext.Group group = context.createGroup("go", "Go");

		group.add("Console Application", AllIcons.RunConfigurations.Application, new UnzipNewModuleBuilderProcessor<GoNewModuleContext>("/moduleTemplates/GoHelloWorld.zip")
		{
			@Nonnull
			@Override
			public GoNewModuleContext createContext(boolean isNewProject)
			{
				return new GoNewModuleContext(isNewProject);
			}

			@Override
			public void buildSteps(@Nonnull Consumer<WizardStep<GoNewModuleContext>> consumer, @Nonnull GoNewModuleContext context)
			{
				consumer.accept(new GoNewModuleSetupStep(context));
			}

			@Override
			public void process(@Nonnull GoNewModuleContext context, @Nonnull ContentEntry contentEntry, @Nonnull ModifiableRootModel modifiableRootModel)
			{
				unzip(modifiableRootModel);

				GoMutableModuleExtension goModuleExtension = modifiableRootModel.getExtensionWithoutCheck(GoMutableModuleExtension.class);
				assert goModuleExtension != null;

				goModuleExtension.setEnabled(true);

				ModifiableModuleRootLayer moduleRootLayer = goModuleExtension.getModuleRootLayer();
				moduleRootLayer.addOrderEntry(new GoPathOrderEntry((ModuleRootLayerImpl) moduleRootLayer));
				Sdk sdk = context.getSdk();
				if(sdk != null)
				{
					goModuleExtension.getInheritableSdk().set(null, sdk);
					modifiableRootModel.addModuleExtensionSdkEntry(goModuleExtension);
				}
			}
		});
	}
}