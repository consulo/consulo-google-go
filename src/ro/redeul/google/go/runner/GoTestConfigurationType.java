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
import consulo.module.extension.ModuleExtensionHelper;
import ro.redeul.google.go.GoIcons;
import ro.redeul.google.go.module.extension.GoModuleExtension;

public class GoTestConfigurationType implements ConfigurationType
{

	private final GoFactory myConfigurationFactory;

	public GoTestConfigurationType()
	{
		myConfigurationFactory = new GoFactory(this);
	}

	@Override
	public String getDisplayName()
	{
		return "Go Test";
	}

	@Override
	public String getConfigurationTypeDescription()
	{
		return "Go Test";
	}

	@Override
	public Icon getIcon()
	{
		return GoIcons.Go;
	}

	@Override
	@NonNls
	@NotNull
	public String getId()
	{
		return "GoTestConfiguration";
	}

	@Override
	public ConfigurationFactory[] getConfigurationFactories()
	{
		return new ConfigurationFactory[]{myConfigurationFactory};
	}

	public static GoTestConfigurationType getInstance()
	{
		return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), GoTestConfigurationType.class);
	}

	public static class GoFactory extends ConfigurationFactory
	{

		public GoFactory(ConfigurationType type)
		{
			super(type);
		}

		@Override
		public boolean isApplicable(@NotNull Project project)
		{
			return ModuleExtensionHelper.getInstance(project).hasModuleExtension(GoModuleExtension.class);
		}

		@Override
		public RunConfiguration createTemplateConfiguration(Project project)
		{
			return new GoTestConfiguration("Go Test", project, getInstance());
		}
	}
}
