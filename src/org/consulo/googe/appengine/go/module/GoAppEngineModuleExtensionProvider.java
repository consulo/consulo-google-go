package org.consulo.googe.appengine.go.module;

import javax.swing.Icon;

import org.consulo.module.extension.ModuleExtensionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import ro.redeul.google.go.GoIcons;

/**
 * @author VISTALL
 * @since 18:37/29.06.13
 */
public class GoAppEngineModuleExtensionProvider implements ModuleExtensionProvider<GoAppEngineModuleExtension, GoAppEngineMutableModuleExtension>
{
	@Nullable
	@Override
	public Icon getIcon()
	{
		return GoIcons.GAE_ICON_16x16;
	}

	@NotNull
	@Override
	public String getName()
	{
		return "Google AppEngine";
	}

	@NotNull
	@Override
	public Class<GoAppEngineModuleExtension> getImmutableClass()
	{
		return GoAppEngineModuleExtension.class;
	}

	@NotNull
	@Override
	public GoAppEngineModuleExtension createImmutable(@NotNull String s, @NotNull Module module)
	{
		return new GoAppEngineModuleExtension(s, module);
	}

	@NotNull
	@Override
	public GoAppEngineMutableModuleExtension createMutable(@NotNull String s, @NotNull Module module, @NotNull GoAppEngineModuleExtension goAppEngineModuleExtension)
	{
		return new GoAppEngineMutableModuleExtension(s, module, goAppEngineModuleExtension);
	}
}
