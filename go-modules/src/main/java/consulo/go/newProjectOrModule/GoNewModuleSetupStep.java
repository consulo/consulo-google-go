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

import com.goide.sdk.GoSdkType;
import consulo.content.bundle.SdkTable;
import consulo.disposer.Disposable;
import consulo.go.localize.GoLocalize;
import consulo.module.creation.ui.UnifiedProjectOrModuleNameStep;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.ui.ComboBox;
import consulo.ui.HasValidator;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.wizard.WizardStepValidationException;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Name and location, the Go SDK, and the module path of the new project; an empty module path is derived from the name.
 *
 * @author VISTALL
 * @since 05-May-17
 */
public class GoNewModuleSetupStep extends UnifiedProjectOrModuleNameStep<GoNewModuleContext> {
    private @Nullable BundleBox myBundleBox;
    private @Nullable TextBox myModulePathBox;

    public GoNewModuleSetupStep(GoNewModuleContext context) {
        super(context);
    }

    @RequiredUIAccess
    @Override
    protected void extend(FormBuilder builder, Disposable uiDisposable) {
        super.extend(builder, uiDisposable);

        BundleBox bundleBox = BundleBoxBuilder.create(uiDisposable).withSdkTypeFilter(sdkTypeId -> sdkTypeId instanceof GoSdkType).build();
        builder.addLabeled(GoLocalize.goNewProjectSdkLabel(), bundleBox.getComponent());
        ComboBox<BundleBox.BundleBoxItem> component = bundleBox.getComponent();
        if (component.getDataModel().getSize() > 0) {
            component.setValueByIndex(0);
        }
        myBundleBox = bundleBox;

        TextBox modulePathBox = TextBox.create();
        modulePathBox.setPlaceholder(GoLocalize.goNewProjectModulePathPlaceholder());
        modulePathBox.addValidator(value -> value == null || value.isBlank() || GoProjectGenerator.isValidModulePath(value.trim())
            ? null
            : new HasValidator.ValidationInfo(GoLocalize.goNewProjectModulePathInvalid(value.trim()).get()));
        // the New Project dialog does not validate its step on Create: report a bad module path while it is typed
        modulePathBox.addValueListener(event -> modulePathBox.validate());
        builder.addLabeled(GoLocalize.goNewProjectModulePathLabel(), modulePathBox);
        myModulePathBox = modulePathBox;
    }

    @RequiredUIAccess
    @Override
    public CompletableFuture<?> validateStep(GoNewModuleContext context) {
        TextBox modulePathBox = myModulePathBox;
        if (modulePathBox != null) {
            modulePathBox.validate();
        }
        String modulePath = typedModulePath();
        if (modulePath != null && !GoProjectGenerator.isValidModulePath(modulePath)) {
            return CompletableFuture.failedFuture(new WizardStepValidationException(GoLocalize.goNewProjectModulePathInvalid(modulePath)));
        }
        return super.validateStep(context);
    }

    @Override
    public void onStepLeave(GoNewModuleContext context) {
        super.onStepLeave(context);

        BundleBox bundleBox = myBundleBox;
        String selectedBundleName = bundleBox != null ? bundleBox.getSelectedBundleName() : null;
        context.setSdk(selectedBundleName != null ? SdkTable.getInstance().findSdk(selectedBundleName) : null);
        context.setModulePath(typedModulePath());
    }

    private @Nullable String typedModulePath() {
        TextBox modulePathBox = myModulePathBox;
        String value = modulePathBox != null ? modulePathBox.getValue() : null;
        return value == null || value.isBlank() ? null : value.trim();
    }
}
