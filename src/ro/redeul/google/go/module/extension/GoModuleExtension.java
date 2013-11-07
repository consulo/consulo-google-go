package ro.redeul.google.go.module.extension;

import org.consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.roots.ContentFoldersSupport;
import org.mustbe.consulo.roots.impl.ProductionContentFolderTypeProvider;
import org.mustbe.consulo.roots.impl.TestContentFolderTypeProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.SdkType;
import ro.redeul.google.go.config.sdk.GoSdkType;

/**
 * @author VISTALL
 * @since 12:42/30.05.13
 */
@ContentFoldersSupport(value = {
		ProductionContentFolderTypeProvider.class,
		TestContentFolderTypeProvider.class
})
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
