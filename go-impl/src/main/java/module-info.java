/**
 * @author VISTALL
 * @since 06-Aug-22
 */
open module consulo.google.go {
  requires consulo.ide.api;

  requires consulo.language.impl;
  requires consulo.language.editor.impl;

  // TODO remove in future
  requires java.desktop;
  requires forms.rt;

  requires com.google.gson;
  requires build.serviceMessages;

  requires consulo.util.netty;
  requires consulo.util.socket.connection;
  
  requires io.netty.buffer;
  requires io.netty.transport;
  requires io.netty.codec;

  requires static com.intellij.regexp;
  requires static com.intellij.spellchecker;

  exports com.goide;
  exports com.goide.actions.file;
  exports com.goide.actions.tool;
  exports com.goide.appengine;
  exports com.goide.codeInsight;
  exports com.goide.codeInsight.imports;
  exports com.goide.codeInsight.unwrap;
  exports com.goide.completion;
  exports com.goide.configuration;
  exports com.goide.dlv;
  exports com.goide.dlv.breakpoint;
  exports com.goide.dlv.protocol;
  exports com.goide.editor;
  exports com.goide.editor.marker;
  exports com.goide.editor.smart;
  exports com.goide.editor.surround;
  exports com.goide.formatter;
  exports com.goide.formatter.settings;
  exports com.goide.generate;
  exports com.goide.go;
  exports com.goide.highlighting;
  exports com.goide.highlighting.exitpoint;
  exports com.goide.inspections;
  exports com.goide.inspections.suppression;
  exports com.goide.inspections.unresolved;
  exports com.goide.intentions;
  exports com.goide.lexer;
  exports com.goide.marker;
  exports com.goide.parser;
  exports com.goide.project;
  exports com.goide.psi;
  exports com.goide.psi.impl;
  exports com.goide.psi.impl.imports;
  exports com.goide.psi.impl.manipulator;
  exports com.goide.quickfix;
  exports com.goide.refactor;
  exports com.goide.regexp;
  exports com.goide.runconfig;
  exports com.goide.runconfig.application;
  exports com.goide.runconfig.before;
  exports com.goide.runconfig.file;
  exports com.goide.runconfig.testing;
  exports com.goide.runconfig.testing.coverage;
  exports com.goide.runconfig.testing.frameworks.gobench;
  exports com.goide.runconfig.testing.frameworks.gocheck;
  exports com.goide.runconfig.testing.frameworks.gotest;
  exports com.goide.runconfig.testing.ui;
  exports com.goide.runconfig.ui;
  exports com.goide.sdk;
  exports com.goide.stubs;
  exports com.goide.stubs.index;
  exports com.goide.stubs.types;
  exports com.goide.template;
  exports com.goide.tree;
  exports com.goide.usages;
  exports com.goide.util;
  exports consulo.google.go;
  exports consulo.google.go.icon;
  exports consulo.google.go.module.extension;
  exports consulo.google.go.module.extension.ui;
  exports consulo.google.go.module.orderEntry;
  exports consulo.google.go.newProjectOrModule;
  exports consulo.google.go.run.dlv;
  exports consulo.google.go.run.dlv.api;
  exports consulo.google.go.run.dlv.breakpoint;
  exports org.jetbrains.debugger;
  exports org.jetbrains.debugger.connection;
  exports org.jetbrains.debugger.values;
  exports org.jetbrains.jsonProtocol;
  exports org.jetbrains.rpc;
}