package ro.redeul.google.go.inspection;

import java.util.Collection;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.ui.UIUtil;
import ro.redeul.google.go.GoBundle;
import ro.redeul.google.go.inspection.fix.RemoveImportFix;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.lang.psi.expressions.literals.GoLiteralString;
import ro.redeul.google.go.lang.psi.toplevel.GoImportDeclaration;
import ro.redeul.google.go.lang.psi.visitors.GoRecursiveElementVisitor;
import ro.redeul.google.go.services.GoCodeManager;

public class ImportDeclarationInspection extends AbstractWholeGoFileInspection
{
	@Nls
	@NotNull
	@Override
	public String getDisplayName()
	{
		return "Import Declaration";
	}

	@Override
	protected void doCheckFile(@NotNull GoFile file, @NotNull final InspectionResult result, boolean onTheFly)
	{
		new GoRecursiveElementVisitor()
		{
			@Override
			public void visitFile(GoFile file)
			{
				super.visitFile(file);
				checkUnusedImport(file, result);
			}

			@Override
			public void visitImportDeclaration(GoImportDeclaration declaration)
			{
				super.visitImportDeclaration(declaration);
				checkImportPath(declaration, result);
			}
		}.visitFile(file);
	}

	private static void checkImportPath(GoImportDeclaration declaration, InspectionResult result)
	{
		String importPathValue = null;
		GoLiteralString importPath = declaration.getImportPath();
		if(importPath != null)
		{
			importPathValue = importPath.getValue();
		}

		if(importPathValue == null)
		{
			return;
		}

		if(importPathValue.isEmpty())
		{
			result.addProblem(declaration, GoBundle.message("error.import.path.is.empty"));
		}

		if(importPathValue.contains(" ") || importPathValue.contains("\t"))
		{
			result.addProblem(declaration, GoBundle.message("error.import.path.contains.space"));
		}

		if(importPathValue.contains("\\"))
		{
			result.addProblem(declaration, GoBundle.message("error.import.path.contains.backslash"));
		}
	}

	private static void checkUnusedImport(GoFile file, InspectionResult result)
	{
		Project project = file.getProject();

		UIUtil.invokeAndWaitIfNeeded((Runnable) () ->
		{
			PsiDocumentManager pdm = PsiDocumentManager.getInstance(project);
			Document document = pdm.getDocument(file);
			if(document != null)
			{
				pdm.commitDocument(document);
			}

		});

		Collection<GoImportDeclaration> unusedImports = GoCodeManager.getInstance(project).findUnusedImports(file);

		for(GoImportDeclaration unused : unusedImports)
		{
			if(!isValidImport(unused))
			{
				continue;
			}

			result.addProblem(unused, GoBundle.message("warning.unused.import", unused.getImportPath().getValue()), ProblemHighlightType.LIKE_UNUSED_SYMBOL, new RemoveImportFix(unused));
		}
	}

	public static boolean isValidImport(GoImportDeclaration declaration)
	{
		if(declaration == null)
		{
			return false;
		}

		String importPathValue = null;
		GoLiteralString importPath = declaration.getImportPath();
		if(importPath != null)
		{
			importPathValue = importPath.getValue();
		}

		if(importPathValue == null || importPathValue.isEmpty())
		{
			return false;
		}

		if(importPathValue.contains(" ") || importPathValue.contains("\t"))
		{
			return false;
		}

		if(importPathValue.contains("\\"))
		{
			return false;
		}

		return true;
	}
}
