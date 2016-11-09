package ro.redeul.google.go.module.extension;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.projectRoots.SdkType;
import consulo.extension.impl.ModuleExtensionWithSdkImpl;
import consulo.roots.ModuleRootLayer;
import ro.redeul.google.go.config.sdk.GoSdkType;

/**
 * @author VISTALL
 * @since 12:42/30.05.13
 */
public class GoModuleExtension extends ModuleExtensionWithSdkImpl<GoModuleExtension>
{
	public GoModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return GoSdkType.class;
	}
}
