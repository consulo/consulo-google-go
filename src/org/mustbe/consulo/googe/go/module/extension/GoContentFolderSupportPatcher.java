package org.mustbe.consulo.googe.go.module.extension;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.roots.ContentFolderSupportPatcher;
import org.mustbe.consulo.roots.ContentFolderTypeProvider;
import org.mustbe.consulo.roots.impl.ProductionContentFolderTypeProvider;
import org.mustbe.consulo.roots.impl.TestContentFolderTypeProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import ro.redeul.google.go.module.extension.GoModuleExtension;

/**
 * @author VISTALL
 * @since 13.02.15
 */
public class GoContentFolderSupportPatcher implements ContentFolderSupportPatcher
{
	@Override
	public void patch(@NotNull ModifiableRootModel model, @NotNull Set<ContentFolderTypeProvider> set)
	{
		GoModuleExtension extension = model.getExtension(GoModuleExtension.class);
		if(extension != null)
		{
			set.add(ProductionContentFolderTypeProvider.getInstance());
			set.add(TestContentFolderTypeProvider.getInstance());
		}
	}
}
