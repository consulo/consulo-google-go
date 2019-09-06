package consulo.google.go.newProjectOrModule;

import com.intellij.openapi.projectRoots.Sdk;
import consulo.ide.wizard.newModule.NewModuleWizardContextBase;

/**
 * @author VISTALL
 * @since 2019-09-06
 */
public class GoNewModuleContext extends NewModuleWizardContextBase
{
	private Sdk mySdk;

	public GoNewModuleContext(boolean isNewProject)
	{
		super(isNewProject);
	}

	public Sdk getSdk()
	{
		return mySdk;
	}

	public void setSdk(Sdk sdk)
	{
		mySdk = sdk;
	}
}
