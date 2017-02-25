package ro.redeul.google.go.ide;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@State(name = "GoProjectSettings", storages = @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/go.xml"))
public class GoProjectSettings implements PersistentStateComponent<GoProjectSettings.GoProjectSettingsBean> {

    public enum BuildSystemType {
        Internal, Makefile
    }

    public static class GoProjectSettingsBean {
        public BuildSystemType BUILD_SYSTEM_TYPE = BuildSystemType.Internal;
    }

    GoProjectSettingsBean bean;

    @Override
    @NotNull
    public GoProjectSettingsBean getState() {
        return bean != null ? bean : new GoProjectSettingsBean();
    }

    @Override
    public void loadState(GoProjectSettingsBean settingsBean) {
        this.bean = settingsBean;
    }

    @NotNull
    public static GoProjectSettings getInstance(Project project) {
        return ServiceManager.getService(project, GoProjectSettings.class);
    }
}
