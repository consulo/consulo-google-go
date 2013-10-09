package ro.redeul.google.go.ide.exception;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.filters.ConsoleFilterProviderEx;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

public class GoExceptionFilterFactory implements ConsoleFilterProviderEx {
	@Override
	public Filter[] getDefaultFilters(@NotNull Project project, @NotNull GlobalSearchScope searchScope)
	{
		return new Filter[] {new GoExceptionFilter(project)};
	}

	@NotNull
	@Override
	public Filter[] getDefaultFilters(@NotNull Project project)
	{
		return new Filter[] {new GoExceptionFilter(project)};
	}
}