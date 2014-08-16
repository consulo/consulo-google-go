package ro.redeul.google.go.imports;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.options.BeanConfigurable;
import com.intellij.openapi.options.Configurable;
import ro.redeul.google.go.GoBundle;
import ro.redeul.google.go.options.GoSettings;

public class GoAutoImportConfigurable extends BeanConfigurable<GoSettings> implements Configurable
{
	public GoAutoImportConfigurable()
	{
		super(GoSettings.getInstance());
		checkBox("OPTIMIZE_IMPORTS_ON_THE_FLY", GoBundle.message("checkbox.optimize.imports.on.the.fly"));
		checkBox("SHOW_IMPORT_POPUP", GoBundle.message("checkbox.show.auto.import.popup"));
	}

	@Nls
	@Override
	public String getDisplayName()
	{
		return null;
	}

	@Nullable
	@Override
	public String getHelpTopic()
	{
		return null;
	}
}
