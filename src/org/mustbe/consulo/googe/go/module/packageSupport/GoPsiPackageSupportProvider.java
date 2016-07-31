package org.mustbe.consulo.googe.go.module.packageSupport;

import org.consulo.module.extension.ModuleExtension;
import org.consulo.psi.PsiPackage;
import org.consulo.psi.PsiPackageManager;
import org.consulo.psi.PsiPackageSupportProvider;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiManager;
import ro.redeul.google.go.lang.psi.impl.GoNamespaceImpl;
import ro.redeul.google.go.module.extension.GoModuleExtension;

/**
 * @author VISTALL
 * @since 12.09.13.
 */
public class GoPsiPackageSupportProvider implements PsiPackageSupportProvider
{
	@Override
	public boolean isSupported(@NotNull ModuleExtension moduleExtension)
	{
		return moduleExtension instanceof GoModuleExtension;
	}

	@Override
	public boolean isValidPackageName(@NotNull Module module, @NotNull String packageName)
	{
		return true;
	}

	@NotNull
	@Override
	public PsiPackage createPackage(@NotNull PsiManager psiManager, @NotNull PsiPackageManager psiPackageManager, @NotNull Class<? extends ModuleExtension> aClass, @NotNull String s)
	{
		return new GoNamespaceImpl(psiManager, psiPackageManager, aClass, s);
	}
}
