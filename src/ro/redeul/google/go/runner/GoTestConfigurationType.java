package ro.redeul.google.go.runner;

import javax.swing.Icon;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import ro.redeul.google.go.GoIcons;

public class GoTestConfigurationType implements ConfigurationType {

    private final GoFactory myConfigurationFactory;

    public GoTestConfigurationType() {
        myConfigurationFactory = new GoFactory(this);
    }

    public String getDisplayName() {
        return "Go Test";
    }

    public String getConfigurationTypeDescription() {
        return "Go Test";
    }

    public Icon getIcon() {
        return GoIcons.Go;
    }

    @NonNls
    @NotNull
    public String getId() {
        return "GoTestConfiguration";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myConfigurationFactory};
    }

    public static GoTestConfigurationType getInstance() {
        return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), GoTestConfigurationType.class);
    }

    public static class GoFactory extends ConfigurationFactory {

        public GoFactory(ConfigurationType type) {
            super(type);
        }

        public RunConfiguration createTemplateConfiguration(Project project) {
            return new GoTestConfiguration("Go Test", project, getInstance());
        }
    }
}
