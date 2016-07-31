package ro.redeul.google.go.runner;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: Aug 27, 2010
 * Time: 1:51:43 PM
 */
public class GoApplicationRunner extends DefaultProgramRunner
{

	@NotNull
	public String getRunnerId()
	{
		return "GoApplicationRunner";
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile)
	{
		return executorId.equals(DefaultRunExecutor.EXECUTOR_ID) && profile instanceof GoApplicationConfiguration;
	}
}
