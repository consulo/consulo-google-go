/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.refactor;

import com.goide.psi.GoExpression;
import com.goide.psi.GoVarDefinition;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import org.jetbrains.annotations.TestOnly;

import jakarta.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.List;

public class GoIntroduceOperation {
  final private Project myProject;
  final private Editor myEditor;
  final private PsiFile myFile;
  private GoExpression myExpression;
  private List<PsiElement> myOccurrences;
  private LinkedHashSet<String> mySuggestedNames;
  private String myName;
  private GoVarDefinition myVar;
  private boolean myReplaceAll;

  public GoIntroduceOperation(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
    myProject = project;
    myEditor = editor;
    myFile = file;
  }

  @TestOnly
  public GoIntroduceOperation(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file, boolean replaceAll) {
    myProject = project;
    myEditor = editor;
    myFile = file;
    myReplaceAll = replaceAll;
  }

  @Nonnull
  public Project getProject() {
    return myProject;
  }

  @Nonnull
  public Editor getEditor() {
    return myEditor;
  }

  @Nonnull
  public PsiFile getFile() {
    return myFile;
  }

  @Nonnull
  public GoExpression getExpression() {
    return myExpression;
  }

  public void setExpression(@Nonnull GoExpression expression) {
    myExpression = expression;
  }

  @Nonnull
  public List<PsiElement> getOccurrences() {
    return myOccurrences;
  }

  public void setOccurrences(@Nonnull List<PsiElement> occurrences) {
    myOccurrences = occurrences;
  }

  @Nonnull
  public LinkedHashSet<String> getSuggestedNames() {
    return mySuggestedNames;
  }

  public void setSuggestedNames(@Nonnull LinkedHashSet<String> suggestedNames) {
    mySuggestedNames = suggestedNames;
  }

  @Nonnull
  public String getName() {
    return myName;
  }

  public void setName(@Nonnull String name) {
    myName = name;
  }

  @Nonnull
  public GoVarDefinition getVar() {
    return myVar;
  }

  public void setVar(@Nonnull GoVarDefinition var) {
    myVar = var;
  }

  public boolean isReplaceAll() {
    return myReplaceAll;
  }

  public void setReplaceAll(boolean replaceAll) {
    myReplaceAll = replaceAll;
  }
}
