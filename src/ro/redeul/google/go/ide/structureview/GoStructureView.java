package ro.redeul.google.go.ide.structureview;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

/**
 * User: jhonny
 * Date: 06/07/11
 */
public class GoStructureView implements PsiStructureViewFactory
{
	@Override
	public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile)
	{
		return new TreeBasedStructureViewBuilder()
		{
			@Override
			@NotNull
			public StructureViewModel createStructureViewModel(Editor editor)
			{
				return new TextEditorBasedStructureViewModel(psiFile)
				{
					@NotNull
					@Override
					public StructureViewTreeElement getRoot()
					{
						return new GoStructureViewElement(getPsiFile());
					}
				};
			}

			@Override
			public boolean isRootNodeShown()
			{
				return false;
			}
		};
	}
}
