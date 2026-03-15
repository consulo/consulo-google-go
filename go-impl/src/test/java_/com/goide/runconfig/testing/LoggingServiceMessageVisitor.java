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

package com.goide.runconfig.testing;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import jetbrains.buildServer.messages.serviceMessages.*;

import java.util.Map;

public class LoggingServiceMessageVisitor implements ServiceMessageVisitor {
  private static final String MY_INDENT = "  ";
  private final StringBuilder myLog = new StringBuilder();
  private String myIndent = "";

  public String getLog() {
    return myLog.toString();
  }

  private void increaseIndent() {
    myIndent += MY_INDENT;
  }

  private void decreaseIndent() {
    myIndent = StringUtil.trimEnd(myIndent, MY_INDENT);
  }

  private void append(MessageWithAttributes message) {
    myLog.append(myIndent).append(message.getClass().getSimpleName()).append('\n');
    increaseIndent();
    increaseIndent();
    for (Map.Entry<String, String> entry : ContainerUtil.newTreeMap(message.getAttributes()).entrySet()) {
      String key = entry.getKey();
      String value = "duration".equals(key) ? "42" : entry.getValue();
      myLog.append(myIndent).append("- ").append(key).append("=")
        .append(value.replace("\n", "\\n")).append('\n');
    }
    decreaseIndent();
    decreaseIndent();
  }

  @Override
  public void visitTestSuiteStarted(TestSuiteStarted testSuiteStarted) {
    append(testSuiteStarted);
    increaseIndent();
  }

  @Override
  public void visitTestSuiteFinished(TestSuiteFinished testSuiteFinished) {
    decreaseIndent();
    append(testSuiteFinished);
  }

  @Override
  public void visitTestStarted(TestStarted testStarted) {
    append(testStarted);
    increaseIndent();
  }

  @Override
  public void visitTestFinished(TestFinished testFinished) {
    decreaseIndent();
    append(testFinished);
  }

  @Override
  public void visitTestIgnored(TestIgnored testIgnored) {
    append(testIgnored);
  }

  @Override
  public void visitTestStdOut(TestStdOut testStdOut) {
    append(testStdOut);
  }

  @Override
  public void visitTestStdErr(TestStdErr testStdErr) {
    append(testStdErr);
  }

  @Override
  public void visitTestFailed(TestFailed testFailed) {
    append(testFailed);
  }

  @Override
  public void visitPublishArtifacts(PublishArtifacts artifacts) {
  }

  @Override
  public void visitProgressMessage(ProgressMessage message) {
  }

  @Override
  public void visitProgressStart(ProgressStart start) {
  }

  @Override
  public void visitProgressFinish(ProgressFinish finish) {
  }

  @Override
  public void visitBuildStatus(BuildStatus status) {
  }

  @Override
  public void visitBuildNumber(BuildNumber number) {
  }

  @Override
  public void visitBuildStatisticValue(BuildStatisticValue value) {
  }

  @Override
  public void visitMessageWithStatus(Message message) {
  }

  @Override
  public void visitBlockOpened(BlockOpened opened) {
  }

  @Override
  public void visitBlockClosed(BlockClosed closed) {
  }

  @Override
  public void visitCompilationStarted(CompilationStarted started) {
  }

  @Override
  public void visitCompilationFinished(CompilationFinished finished) {
  }

  @Override
  public void visitServiceMessage(ServiceMessage message) {
  }
}
