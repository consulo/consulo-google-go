package ro.redeul.google.go.lang.psi.resolve.references;

import static ro.redeul.google.go.util.LookupElementUtil.createLookupElement;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.scope.util.PsiScopesUtilCore;
import com.intellij.psi.util.PsiUtilCore;
import ro.redeul.google.go.lang.psi.GoPsiElement;
import ro.redeul.google.go.lang.psi.expressions.primary.GoLiteralExpression;
import ro.redeul.google.go.lang.psi.processors.GoResolveStates;
import ro.redeul.google.go.lang.psi.resolve.GoResolveResult;
import ro.redeul.google.go.lang.psi.resolve.MethodOrTypeNameResolver;

public class CallOrConversionReference extends AbstractCallOrConversionReference<CallOrConversionReference> {

    public CallOrConversionReference(GoLiteralExpression expression) {
        super(expression, RESOLVER);
    }

    private static final ResolveCache.AbstractResolver<CallOrConversionReference, GoResolveResult> RESOLVER =
        new ResolveCache.AbstractResolver<CallOrConversionReference, GoResolveResult>() {
            @Override
            public GoResolveResult resolve(CallOrConversionReference psiReference, boolean incompleteCode) {
                MethodOrTypeNameResolver processor =
                    new MethodOrTypeNameResolver(psiReference);

                GoLiteralExpression expression = psiReference.getElement();
				PsiScopesUtilCore.treeWalkUp(processor, expression, expression.getContainingFile(), GoResolveStates.initial());

                PsiElement declaration = processor.getChildDeclaration();
                return declaration != null ? new GoResolveResult(declaration) : GoResolveResult.NULL;
            }
        };

    @Override
    protected CallOrConversionReference self() {
        return this;
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        GoLiteralExpression expression = getElement();

        final List<LookupElementBuilder> variants = new ArrayList<LookupElementBuilder>();

        MethodOrTypeNameResolver processor =
            new MethodOrTypeNameResolver(this) {
                @Override
                protected boolean addDeclaration(PsiElement declaration, PsiElement child) {
                    String name = PsiUtilCore.getName(child);

                    String visiblePackageName =
                        getState().get(GoResolveStates.VisiblePackageName);

                    if ( visiblePackageName != null ) {
                        name = visiblePackageName + "." + name;
                    }
                    if (name == null) {
                        return true;
                    }

                    GoPsiElement goPsi = (GoPsiElement) declaration;
                    GoPsiElement goChildPsi = (GoPsiElement) child;
                    variants.add(createLookupElement(goPsi, name, goChildPsi));
                    return true;
                }
            };

		PsiScopesUtilCore.treeWalkUp(
				processor,
				expression, expression.getContainingFile(),
				GoResolveStates.initial());

        return variants.toArray();
    }
}
