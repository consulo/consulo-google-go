package ro.redeul.google.go.module.extension;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.consulo.module.extension.MutableModuleExtensionWithSdk;
import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.module.extension.ui.ModuleExtensionWithSdkPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 12:44/30.05.13
 */
public class GoMutableModuleExtension extends GoModuleExtension implements MutableModuleExtensionWithSdk<GoModuleExtension>
{
	@NotNull
	private final GoModuleExtension moduleExtension;

	public GoMutableModuleExtension(@NotNull String id, @NotNull Module module, @NotNull GoModuleExtension moduleExtension)
	{
		super(id, module);
		this.moduleExtension = moduleExtension;
		commit(moduleExtension);
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@NotNull ModifiableRootModel modifiableRootModel, @Nullable Runnable runnable)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new ModuleExtensionWithSdkPanel(this, runnable), BorderLayout.NORTH);
		return panel;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified()
	{
		return isModifiedImpl(moduleExtension);
	}

	@Override
	public void commit()
	{
		moduleExtension.commit(this);
	}
}
