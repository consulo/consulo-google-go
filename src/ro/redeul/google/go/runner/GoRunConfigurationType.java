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

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: Aug 19, 2010
 * Time: 2:49:26 PM
 */
public class GoRunConfigurationType implements ConfigurationType {

    private final GoFactory myConfigurationFactory;

    public GoRunConfigurationType() {
        myConfigurationFactory = new GoFactory(this);
    }

    public String getDisplayName() {
        return "Go Application";
    }

    public String getConfigurationTypeDescription() {
        return "Go Application";
    }

    public Icon getIcon() {
        return GoIcons.Go;
    }

    @NonNls
    @NotNull
    public String getId() {
        return "GoApplicationRunConfiguration";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myConfigurationFactory};
    }

    public static GoRunConfigurationType getInstance() {
        return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), GoRunConfigurationType.class);
    }

    public static class GoFactory extends ConfigurationFactory {

        public GoFactory(ConfigurationType type) {
            super(type);
        }

        public RunConfiguration createTemplateConfiguration(Project project) {
            return new GoApplicationConfiguration("Go application", project, getInstance());
        }        
    }
}
