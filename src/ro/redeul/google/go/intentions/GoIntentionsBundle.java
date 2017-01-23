package ro.redeul.google.go.intentions;

import org.jetbrains.annotations.PropertyKey;
import com.intellij.AbstractBundle;

public class GoIntentionsBundle extends AbstractBundle
{
	private static final String BUNDLE = "ro.redeul.google.go.intentions.GoIntentionsBundle";
	private static final GoIntentionsBundle ourInstance = new GoIntentionsBundle();

	private GoIntentionsBundle()
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
