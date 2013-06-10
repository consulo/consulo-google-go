package ro.redeul.google.go.compilation;

import com.intellij.openapi.project.Project;

public class GoCompilerFactory/* implements CompilerFactory*/ {

    private Project myProject;

    public GoCompilerFactory(Project project) {
        myProject = project;
    }
 /*
    public Compiler[] createCompilers(CompilerManager compilerManager) {
        return new Compiler[]{
            new GoCompiler(myProject),
            new GoMakefileCompiler(myProject)
        };
    }  */
}
