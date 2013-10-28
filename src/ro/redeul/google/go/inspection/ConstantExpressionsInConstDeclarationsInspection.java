package ro.redeul.google.go.inspection;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInspection.ProblemHighlightType;
import ro.redeul.google.go.GoBundle;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.lang.psi.declarations.GoConstDeclaration;
import ro.redeul.google.go.lang.psi.expressions.GoExpr;
import ro.redeul.google.go.lang.psi.visitors.GoRecursiveElementVisitor;

public class ConstantExpressionsInConstDeclarationsInspection
    extends AbstractWholeGoFileInspection{

    @Override
    protected void doCheckFile(@NotNull GoFile file, @NotNull final InspectionResult result, boolean isOnTheFly) {
            new GoRecursiveElementVisitor() {
                @Override
                public void visitConstDeclaration(GoConstDeclaration declaration) {
                    checkConstDeclaration(declaration, result);
                }
            }.visitFile(file);
        }

    private void checkConstDeclaration(GoConstDeclaration declaration, InspectionResult result) {
        GoExpr []expressions = declaration.getExpressions();
        for (GoExpr expression : expressions) {
            if (!expression.isConstantExpression()) {
                result.addProblem(expression,
						GoBundle.message("error.non.constant.expression"),
                                  ProblemHighlightType.WEAK_WARNING);

            }
        }
    }
}
