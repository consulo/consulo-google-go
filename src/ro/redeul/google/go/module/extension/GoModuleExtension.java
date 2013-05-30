package ro.redeul.google.go.module.extension;

import org.consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.SdkType;
import ro.redeul.google.go.config.sdk.GoSdkType;

/**
 * @author VISTALL
 * @since 12:42/30.05.13
 */
public class GoModuleExtension extends ModuleExtensionWithSdkImpl<GoModuleExtension>
{
	public GoModuleExtension(@NotNull String id, @NotNull Module module)
	{
		super(id, module);
	}

	@Override
	protected Class<? extends SdkType> getSdkTypeClass()
	{
		return GoSdkType.class;
	}
}
