package ro.redeul.google.go.template;

import org.jetbrains.annotations.PropertyKey;
import com.intellij.AbstractBundle;

public class TemplateBundle extends AbstractBundle
{
	private static final String BUNDLE = "ro.redeul.google.go.template.TemplateBundle";
	private static final TemplateBundle ourInstance = new TemplateBundle();

	private TemplateBundle()
	{
		super(BUNDLE);
	}

	public static String message(@PropertyKey(resourceBundle = BUNDLE) String key)
	{
		return ourInstance.getMessage(key);
	}

	public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params)
	{
		return ourInstance.getMessage(key, params);
	}
}
