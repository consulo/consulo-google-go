package ro.redeul.google.go.lang.psi;

import com.intellij.util.ArrayFactory;
import consulo.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 12.09.13.
 */
public interface GoNamespace extends PsiPackage
{
	GoNamespace[] EMPTY_ARRAY = new GoNamespace[0];

	ArrayFactory<GoNamespace> ARRAY_FACTORY = new ArrayFactory<GoNamespace>() {
		@NotNull
		@Override
		public GoNamespace[] create(int i) {
			return i == 0 ? EMPTY_ARRAY : new GoNamespace[i];
		}
	};
}
