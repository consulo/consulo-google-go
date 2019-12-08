/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.testFramework.LexerTestCase;
import consulo.testFramework.util.TestUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;

import static consulo.testFramework.AssertEx.assertSameLinesWithFile;

public class GoLexerTest extends LexerTestCase
{
	private static final String PATH = "lexer";

	public void testBasicTypes()
	{
		doTest();
	}

	public void testConstants()
	{
		doTest();
	}

	public void testFor()
	{
		doTest();
	}

	public void testFunctionArguments()
	{
		doTest();
	}

	public void testHelloWorld()
	{
		doTest();
	}

	public void testIf()
	{
		doTest();
	}

	public void testImports()
	{
		doTest();
	}

	public void testMultipleResult()
	{
		doTest();
	}

	public void testNamedResult()
	{
		doTest();
	}

	public void testPointers()
	{
		doTest();
	}

	public void testRangeFor()
	{
		doTest();
	}

	public void testSlices()
	{
		doTest();
	}

	public void testStructs()
	{
		doTest();
	}

	public void testVariables()
	{
		doTest();
	}

	public void testEscapedQuote()
	{
		doTest();
	}

	public void testUtf16()
	{
		doTest();
	}

	public void testCouldNotMatch()
	{
		doTest();
	}

	private void doTest()
	{
		try
		{
			String text = FileUtil.loadFile(new File(getRootPath() + "/" + PATH + "/" + TestUtil.getTestName(this, true) + ".go"), CharsetToolkit.UTF8);
			String actual = printTokens(StringUtil.convertLineSeparators(text.trim()), 0);
			assertSameLinesWithFile(new File(getRootPath() + "/" + PATH + "/" + TestUtil.getTestName(this, true) + ".txt").getAbsolutePath(), actual);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private String getRootPath()
	{
		CodeSource codeSource = GoLexerTest.class.getProtectionDomain().getCodeSource();
		if(codeSource != null)
		{
			URL location = codeSource.getLocation();
			if(location != null)
			{
				try
				{
					URI uri = location.toURI();
					return uri.getSchemeSpecificPart();
				}
				catch(URISyntaxException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}

	@Nonnull
	@Override
	protected Lexer createLexer()
	{
		return new GoLexer();
	}

	@Nonnull
	@Override
	protected String getDirPath()
	{
		return getRootPath() + "/" + PATH;
	}
}
