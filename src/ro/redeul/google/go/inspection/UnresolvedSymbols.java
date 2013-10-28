package ro.redeul.google.go.inspection;

import static ro.redeul.google.go.inspection.fix.CreateFunctionFix.isFunctionNameIdentifier;
import static ro.redeul.google.go.lang.psi.utils.GoPsiUtils.findParentOfType;
import static ro.redeul.google.go.lang.psi.utils.GoPsiUtils.resolveSafely;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import ro.redeul.google.go.GoBundle;
import ro.redeul.google.go.inspection.fix.CreateFunctionFix;
import ro.redeul.google.go.inspection.fix.CreateGlobalVariableFix;
import ro.redeul.google.go.inspection.fix.CreateLocalVariableFix;
import ro.redeul.google.go.inspection.fix.CreateTypeFix;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.lang.psi.declarations.GoVarDeclaration;
import ro.redeul.google.go.lang.psi.declarations.GoVarDeclarations;
import ro.redeul.google.go.lang.psi.expressions.GoExpr;
import ro.redeul.google.go.lang.psi.expressions.literals.GoLiteralIdentifier;
import ro.redeul.google.go.lang.psi.expressions.primary.GoLiteralExpression;
import ro.redeul.google.go.lang.psi.expressions.primary.GoSelectorExpression;
import ro.redeul.google.go.lang.psi.resolve.references.BuiltinCallOrConversionReference;
import ro.redeul.google.go.lang.psi.resolve.references.CallOrConversionReference;
import ro.redeul.google.go.lang.psi.statements.GoShortVarDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoFunctionDeclaration;
import ro.redeul.google.go.lang.psi.types.GoPsiTypeName;
import ro.redeul.google.go.lang.psi.utils.GoFileUtils;
import ro.redeul.google.go.lang.psi.utils.GoPsiUtils;
import ro.redeul.google.go.lang.psi.visitors.GoRecursiveElementVisitor;

public class UnresolvedSymbols extends AbstractWholeGoFileInspection {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Highlights unresolved symbols";
    }

    @Override
    protected void doCheckFile(@NotNull GoFile file,
                               @NotNull final InspectionResult result,
                               boolean isOnTheFly) {

        new GoRecursiveElementVisitor() {
            @Override
            public void visitShortVarDeclaration(GoShortVarDeclaration declaration) {
                visitVarDeclaration(declaration);
            }

            @Override
            public void visitVarDeclaration(GoVarDeclaration declaration) {
                for (GoExpr expr : declaration.getExpressions()) {
                    visitElement(expr);
                }
            }

            @Override
            public void visitLiteralIdentifier(GoLiteralIdentifier identifier) {
                if (!identifier.isIota() && !identifier.isBlank()) {
                    tryToResolveReference(identifier, identifier.getName());
                }
            }

            @Override
            public void visitLiteralExpression(GoLiteralExpression expression) {
                if (CallOrConversionReference.MATCHER.accepts(expression) ||
                    BuiltinCallOrConversionReference.MATCHER.accepts(expression)) {
                    tryToResolveReference(expression, expression.getText());
                } else {
                    super.visitLiteralExpression(expression);
                }
            }

            @Override
            public void visitTypeName(GoPsiTypeName typeName) {
                if (!typeName.isPrimitive()) {
                    tryToResolveReference(typeName, typeName.getName());
                }
            }

            private void tryToResolveReference(PsiElement element, String name) {
                if (GoPsiUtils.hasHardReferences(element) &&
                        resolveSafely(element, PsiElement.class) == null &&
                        !isCgoUsage(element)) {

                    LocalQuickFix[] fixes;
                    if (isFunctionNameIdentifier(element)) {
                        fixes = new LocalQuickFix[]{new CreateFunctionFix(element)};
                    } else if (isLocalVariableIdentifier(element)) {
                        fixes = new LocalQuickFix[]{new CreateLocalVariableFix(element),
                                new CreateGlobalVariableFix(element)};
                    } else if (isGlobalVariableIdentifier(element)) {
                        fixes = new LocalQuickFix[]{new CreateGlobalVariableFix(element)};
                    } else if (isUnqualifiedTypeName(element)) {
                        fixes = new LocalQuickFix[]{new CreateTypeFix(element)};
                    } else {
                        fixes = LocalQuickFix.EMPTY_ARRAY;
                    }

                    result.addProblem(
                        element,
							GoBundle.message("warning.unresolved.symbol", name),
                        ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, fixes);
                }
            }
        }.visitElement(file);
    }

    private boolean isCgoUsage(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (!(file instanceof GoFile)) {
            return false;
        }

        if (element instanceof GoPsiTypeName) {
            element = ((GoPsiTypeName) element).getIdentifier();
        }

        if (element instanceof GoLiteralExpression) {
            element = ((GoLiteralExpression) element).getLiteral();
        }

        if (!(element instanceof GoLiteralIdentifier)) {
            return false;
        }

        GoLiteralIdentifier identifier = (GoLiteralIdentifier) element;
        if (!"C".equals(identifier.getLocalPackageName())) {
            return false;
        }

        return GoFileUtils.isPackageNameImported((GoFile) file, "C");
    }

    private static boolean isUnqualifiedTypeName(PsiElement element) {
        return element instanceof GoPsiTypeName &&
                !((GoPsiTypeName) element).getIdentifier().isQualified();
    }

    private static boolean isGlobalVariableIdentifier(PsiElement element) {
        return element instanceof GoLiteralIdentifier &&
               findParentOfType(element, GoSelectorExpression.class) == null &&
               findParentOfType(element, GoFunctionDeclaration.class) == null &&
               findParentOfType(element, GoVarDeclarations.class) != null;
    }

    private static boolean isLocalVariableIdentifier(PsiElement element) {
        return element instanceof GoLiteralIdentifier &&
               findParentOfType(element, GoSelectorExpression.class) == null &&
               findParentOfType(element, GoFunctionDeclaration.class) != null;
    }
}
