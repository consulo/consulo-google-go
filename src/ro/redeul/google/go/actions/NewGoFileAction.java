package ro.redeul.google.go.actions;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateTemplateInPackageAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import ro.redeul.google.go.GoBundle;
import ro.redeul.google.go.GoIcons;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.module.extension.GoModuleExtension;

/**
 * @author Mihai Claudiu Toader <mtoader@gmail.com>
 *         Date: Jun 2, 2012
 */
public class NewGoFileAction extends CreateTemplateInPackageAction<GoFile>
    implements DumbAware {

    public NewGoFileAction() {
        super(GoBundle.message("new.go.file"),
              GoBundle.message("new.go.file.description"),
              GoIcons.GO_ICON_16x16, true);
    }

    @Override
    protected PsiElement getNavigationElement(@NotNull GoFile file) {
        return file;
    }

    @Override
    protected String getErrorTitle() {
        return "New Go file creation";
    }

    //    @Override
    protected boolean checkPackageExists(PsiDirectory directory) {
        return true;
    }

	@Override
	public void update(AnActionEvent e)
	{
		super.update(e);
		final Module data = e.getData(LangDataKeys.MODULE);
		if(data == null || ModuleUtilCore.getExtension(data, GoModuleExtension.class) == null) {
			e.getPresentation().setEnabledAndVisible(false);
		}
	}

    protected void doCheckCreate(PsiDirectory dir, String parameterName,
                                 String typeName)
        throws IncorrectOperationException {
        // check to see if a file with the same name already exists

        String fileName = fileNameFromTypeName(typeName, parameterName);

        VirtualFile targetFile = dir.getVirtualFile()
                                    .findFileByRelativePath(fileName);
        if (targetFile != null) {
            throw new IncorrectOperationException(
                GoBundle.message("target.file.exists", targetFile.getPath()));
        }
    }

    @Override
    protected GoFile doCreate(PsiDirectory dir, String parameterName,
                              String typeName)
        throws IncorrectOperationException {
        GoTemplatesFactory.Template template = GoTemplatesFactory.Template.GoFile;

        String fileName = fileNameFromTypeName(typeName, parameterName);
        String packageName = packageNameFromTypeName(typeName, parameterName);

        if (typeName.equals("multiple")) {
            if (dir.findSubdirectory(parameterName) == null) {
                dir = dir.createSubdirectory(parameterName);
            } else {
                dir = dir.findSubdirectory(parameterName);
            }

            fileName = fileName.replaceFirst(parameterName + "/", "");
        }

        return GoTemplatesFactory.createFromTemplate(dir, packageName, fileName,
                                                     template);
    }

    String fileNameFromTypeName(String typeName, String parameterName) {
        if (typeName.startsWith("lib.")) {
            return parameterName + ".go";
        }

        if (typeName.equals("multiple")) {
            return parameterName + "/" + parameterName + ".go";
        }

        return parameterName + ".go";
    }

    String packageNameFromTypeName(String typeName, String parameterName) {

        if (typeName.startsWith("lib.")) {
            return typeName.replaceFirst("^lib\\.", "");
        }

        return parameterName;
    }

    //    @Override
    protected void buildDialog(Project project, PsiDirectory directory,
                               CreateFileFromTemplateDialog.Builder builder) {

        PsiFile childs[] = directory.getFiles();

        Set<String> packages = new HashSet<String>();

        for (PsiFile child : childs) {
            if (child instanceof GoFile) {
                GoFile goFile = (GoFile) child;

                if (!goFile.getPackage().isMainPackage()) {
                    packages.add(goFile.getPackage().getPackageName());
                }
            }
        }

        builder.addKind("New file", GoIcons.GO_ICON_16x16, "single");

        for (String packageName : packages) {
            builder.addKind("New file in library: " + packageName,
                            GoIcons.GO_ICON_16x16, "lib." + packageName);
        }
    }

    private boolean isLibraryFolder(PsiDirectory directory) {
        return false;
    }

    private boolean isApplicationFolder(PsiDirectory directory) {
        return false;
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName,
                                   String templateName) {
        return GoBundle.message("new.go.lib.action.text");
    }
}
