package consulo.go.newProjectOrModule;

import consulo.content.bundle.Sdk;
import consulo.module.creation.NewModuleWizardContextBase;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2019-09-06
 */
public class GoNewModuleContext extends NewModuleWizardContextBase {
  private final GoProjectTemplate myTemplate;
  @Nullable
  private Sdk mySdk;
  @Nullable
  private String myModulePath;

  public GoNewModuleContext(boolean isNewProject, GoProjectTemplate template) {
    super(isNewProject);
    myTemplate = template;
  }

  public GoProjectTemplate getTemplate() {
    return myTemplate;
  }

  @Nullable
  public Sdk getSdk() {
    return mySdk;
  }

  public void setSdk(@Nullable Sdk sdk) {
    mySdk = sdk;
  }

  /**
   * @return the module path the user typed, {@code null} to derive it from the name
   */
  @Nullable
  public String getModulePath() {
    return myModulePath;
  }

  public void setModulePath(@Nullable String modulePath) {
    myModulePath = modulePath;
  }

  /**
   * @return the module path to create: the typed one, cleaned when it is not valid (the New Project dialog does not stop
   * on an invalid step), else one derived from the name
   */
  public String getModulePathOrDefault() {
    if (myModulePath == null) {
      return GoProjectGenerator.toModulePath(getName());
    }
    return GoProjectGenerator.isValidModulePath(myModulePath) ? myModulePath : GoProjectGenerator.toModulePath(myModulePath);
  }
}
