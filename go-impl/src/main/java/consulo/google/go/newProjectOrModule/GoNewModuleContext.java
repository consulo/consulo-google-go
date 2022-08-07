package consulo.google.go.newProjectOrModule;

import consulo.content.bundle.Sdk;
import consulo.ide.newModule.NewModuleWizardContextBase;

/**
 * @author VISTALL
 * @since 2019-09-06
 */
public class GoNewModuleContext extends NewModuleWizardContextBase {
  private Sdk mySdk;

  public GoNewModuleContext(boolean isNewProject) {
    super(isNewProject);
  }

  public Sdk getSdk() {
    return mySdk;
  }

  public void setSdk(Sdk sdk) {
    mySdk = sdk;
  }
}
