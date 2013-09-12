package org.consulo.googe.go.module.packageSupport;

import com.intellij.psi.PsiManager;
import org.consulo.module.extension.ModuleExtension;
import org.consulo.psi.PsiPackage;
import org.consulo.psi.PsiPackageManager;
import org.consulo.psi.PsiPackageSupportProvider;
import org.jetbrains.annotations.NotNull;
import ro.redeul.google.go.lang.psi.impl.GoNamespaceImpl;
import ro.redeul.google.go.module.extension.GoModuleExtension;

/**
 * @author VISTALL
 * @since 12.09.13.
 */
public class GoPsiPackageSupportProvider implements PsiPackageSupportProvider {
	@NotNull
	@Override
	public Class<? extends ModuleExtension> getSupportedModuleExtensionClass() {
		return GoModuleExtension.class;
	}

	@NotNull
	@Override
	public PsiPackage createPackage(@NotNull PsiManager psiManager, @NotNull PsiPackageManager psiPackageManager, @NotNull Class<? extends ModuleExtension> aClass, @NotNull String s) {
		return new GoNamespaceImpl(psiManager, psiPackageManager, aClass, s);
	}
}
