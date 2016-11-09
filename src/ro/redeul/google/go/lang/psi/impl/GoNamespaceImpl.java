package ro.redeul.google.go.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.Language;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiPackageBase;
import com.intellij.util.ArrayFactory;
import consulo.annotations.RequiredReadAction;
import consulo.module.extension.ModuleExtension;
import consulo.psi.PsiPackage;
import consulo.psi.PsiPackageManager;
import ro.redeul.google.go.GoLanguage;
import ro.redeul.google.go.lang.psi.GoNamespace;

/**
 * @author VISTALL
 * @since 12.09.13.
 */
public class GoNamespaceImpl extends PsiPackageBase implements GoNamespace
{
	public GoNamespaceImpl(PsiManager manager, PsiPackageManager packageManager, Class<? extends ModuleExtension> extensionClass, String qualifiedName)
	{
		super(manager, packageManager, extensionClass, qualifiedName);
	}

	@Override
	protected ArrayFactory<? extends PsiPackage> getPackageArrayFactory()
	{
		return GoNamespace.ARRAY_FACTORY;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public Language getLanguage()
	{
		return GoLanguage.INSTANCE;
	}
}
