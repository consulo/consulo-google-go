package org.consulo.googe.appengine.go.module;

import org.consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.SdkType;
import ro.redeul.google.go.config.sdk.GoAppEngineSdkType;

/**
 * @author VISTALL
 * @since 18:35/29.06.13
 */
public class GoAppEngineModuleExtension extends ModuleExtensionWithSdkImpl<GoAppEngineModuleExtension>
{
	public GoAppEngineModuleExtension(@NotNull String id, @NotNull Module module)
	{
		super(id, module);
	}

	@Override
	protected Class<? extends SdkType> getSdkTypeClass()
	{
		return GoAppEngineSdkType.class;
	}
}
