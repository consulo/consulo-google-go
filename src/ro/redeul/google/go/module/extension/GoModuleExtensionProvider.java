package ro.redeul.google.go.module.extension;

import javax.swing.Icon;

import org.consulo.module.extension.ModuleExtensionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import ro.redeul.google.go.GoIcons;

/**
 * @author VISTALL
 * @since 12:47/30.05.13
 */
public class GoModuleExtensionProvider implements ModuleExtensionProvider<GoModuleExtension, GoMutableModuleExtension>
{
	@Nullable
	@Override
	public Icon getIcon()
	{
		return GoIcons.Go;
	}

	@NotNull
	@Override
	public String getName()
	{
		return "Google Go";
	}

	@NotNull
	@Override
	public GoModuleExtension createImmutable(@NotNull String s, @NotNull Module module)
	{
		return new GoModuleExtension(s, module);
	}

	@NotNull
	@Override
	public GoMutableModuleExtension createMutable(@NotNull String s, @NotNull Module module, @NotNull GoModuleExtension moduleExtension)
	{
		return new GoMutableModuleExtension(s, module, moduleExtension);
	}
}
