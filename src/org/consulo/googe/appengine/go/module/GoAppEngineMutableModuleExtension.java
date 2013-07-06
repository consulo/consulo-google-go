package org.consulo.googe.appengine.go.module;

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
 * @since 18:36/29.06.13
 */
public class GoAppEngineMutableModuleExtension extends GoAppEngineModuleExtension implements MutableModuleExtensionWithSdk<GoAppEngineModuleExtension>
{
	@NotNull
	private final GoAppEngineModuleExtension myOriginalModuleExtension;

	public GoAppEngineMutableModuleExtension(@NotNull String id, @NotNull Module module, @NotNull GoAppEngineModuleExtension originalModuleExtension)
	{
		super(id, module);
		myOriginalModuleExtension = originalModuleExtension;
		commit(originalModuleExtension);
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
		panel.add(BorderLayout.NORTH, new ModuleExtensionWithSdkPanel(this, runnable));
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
		return isModifiedImpl(myOriginalModuleExtension);
	}

	@Override
	public void commit()
	{
		myOriginalModuleExtension.commit(this);
	}
}
