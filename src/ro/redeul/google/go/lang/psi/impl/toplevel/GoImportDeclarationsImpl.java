package ro.redeul.google.go.lang.psi.impl.toplevel;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import ro.redeul.google.go.lang.psi.scope.util.GoPsiScopesUtil;
import org.jetbrains.annotations.NotNull;
import ro.redeul.google.go.lang.psi.processors.GoResolveStates;
import ro.redeul.google.go.lang.psi.visitors.GoElementVisitor;
import ro.redeul.google.go.lang.psi.impl.GoPsiElementBase;
import ro.redeul.google.go.lang.psi.toplevel.GoImportDeclarations;
import ro.redeul.google.go.lang.psi.toplevel.GoImportDeclaration;

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: Jul 24, 2010
 * Time: 11:29:41 PM
 */
public class GoImportDeclarationsImpl extends GoPsiElementBase implements GoImportDeclarations {

    public GoImportDeclarationsImpl(@NotNull ASTNode node) {
        super(node);
    }

    public GoImportDeclaration[] getDeclarations() {
        return findChildrenByClass(GoImportDeclaration.class);
    }

    public void accept(GoElementVisitor visitor) {
        visitor.visitImportDeclarations(this);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {

        // don't process recursively imported names (yet).
        if (!state.get(GoResolveStates.IsOriginalPackage) || !state.get(GoResolveStates.IsOriginalFile))
            return true;

        return GoPsiScopesUtil.walkChildrenScopes(this, processor, state, lastParent, place);
    }
}
