package ro.redeul.google.go;

import javax.swing.Icon;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;

public class GoFileType extends LanguageFileType
{
	public static final GoFileType INSTANCE = new GoFileType();

	@NonNls
	public static final String DEFAULT_EXTENSION = "go";

	private GoFileType()
	{
		super(GoLanguage.INSTANCE);
	}

	@Override
	@NotNull
	@NonNls
	public String getName()
	{
		return "Google Go";
	}

	@Override
	@NonNls
	@NotNull
	public String getDescription()
	{
		return "Google Go files";
	}

	@Override
	@NotNull
	@NonNls
	public String getDefaultExtension()
	{
		return DEFAULT_EXTENSION;
	}

	@Override
	public Icon getIcon()
	{
		return GoIcons.Go;
	}

	@Override
	public String getCharset(@NotNull VirtualFile file, byte[] content)
	{
		return CharsetToolkit.UTF8;
	}
}
