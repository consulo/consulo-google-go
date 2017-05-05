package consulo.googe.go.newProjectOrModule;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import consulo.googe.go.module.extension.GoMutableModuleExtension;
import consulo.googe.go.module.orderEntry.GoPathOrderEntry;
import consulo.ide.impl.UnzipNewModuleBuilderProcessor;
import consulo.ide.newProject.NewModuleBuilder;
import consulo.ide.newProject.NewModuleContext;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoNewModuleBuilder implements NewModuleBuilder {
  @Override
  public void setupContext(@NotNull NewModuleContext context) {
    NewModuleContext.Group group = context.createGroup("go", "Go");

    group.add("Console Application", AllIcons.RunConfigurations.Application,
              new UnzipNewModuleBuilderProcessor<GoNewModuleBuilderPanel>("/moduleTemplates/GoHelloWorld.zip") {
                @NotNull
                @Override
                public GoNewModuleBuilderPanel createConfigurationPanel() {
                  return new GoNewModuleBuilderPanel();
                }

                @Override
                public void setupModule(@NotNull GoNewModuleBuilderPanel panel,
                                        @NotNull ContentEntry contentEntry,
                                        @NotNull ModifiableRootModel modifiableRootModel) {
                  unzip(modifiableRootModel);

                  GoMutableModuleExtension goModuleExtension = modifiableRootModel.getExtensionWithoutCheck(GoMutableModuleExtension.class);
                  assert goModuleExtension != null;

                  goModuleExtension.setEnabled(true);

                  ModifiableModuleRootLayer moduleRootLayer = goModuleExtension.getModuleRootLayer();
                  moduleRootLayer.addOrderEntry(new GoPathOrderEntry((ModuleRootLayerImpl)moduleRootLayer));
                  Sdk sdk = panel.getSdk();
                  if (sdk != null) {
                    goModuleExtension.getInheritableSdk().set(null, sdk);
                    modifiableRootModel.addModuleExtensionSdkEntry(goModuleExtension);
                  }
                }
              });
  }
}