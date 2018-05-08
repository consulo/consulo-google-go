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

package com.goide.dlv.protocol;

import com.google.gson.stream.JsonWriter;
import com.intellij.xdebugger.frame.XValueNode;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.jsonProtocol.OutMessage;
import org.jetbrains.jsonProtocol.Request;

import java.io.IOException;

/**
 * Please add your requests as a subclasses, otherwise reflection won't work.
 *
 * @param <T> type of callback
 * @see com.goide.dlv.DlvCommandProcessor#getResultType(String)
 */
public abstract class DlvRequest<T> extends OutMessage implements Request<T> {
  private static final String BREAKPOINT = "Breakpoint";
  private static final String SCOPE = "Scope";
  private static final String PARAMS = "params";
  private static final String ID = "id";
  private boolean argumentsObjectStarted;

  private DlvRequest() {
    try {
      getWriter().name("method").value(getMethodName());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  @Override
  public String getMethodName() {
    return "RPCServer." + getClass().getSimpleName();
  }

  @Override
  public final void beginArguments() {
    if (!argumentsObjectStarted) {
      argumentsObjectStarted = true;
      if (needObject()) {
        try {
          getWriter().name(PARAMS);
          getWriter().beginArray();
          getWriter().beginObject();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  protected boolean needObject() {
    return true;
  }

  @Override
  public final void finalize(int id) {
    try {
      if (argumentsObjectStarted) {
        if (needObject()) {
          getWriter().endObject();
          getWriter().endArray();
        }
      }
      getWriter().name(ID).value(id);
      getWriter().endObject();
      getWriter().close();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public final static class ClearBreakpoint extends DlvRequest<DlvApi.Breakpoint> {
    public ClearBreakpoint(int id) {
      writeSingletonIntArray(PARAMS, id);
    }

    @Override
    protected boolean needObject() {
      return false;
    }
  }

 /* public final static class CreateBreakpoint extends DlvRequest<DlvApi.Breakpoint> {
    public CreateBreakpoint(String path, int line) {
      try {
        beginArguments();
        getWriter().name(BREAKPOINT).beginObject().name("file").value(path).name("line").value(line).endObject();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }*/

  /*public final static class Stacktrace extends DlvRequest<DlvApi.StacktraceOut> {
    public Stacktrace() {
      writeLong("Id", -1);
      writeLong("Depth", 100);
    }
  }  */

  private abstract static class Locals<T> extends DlvRequest<T> {
    Locals(int frameId, int goroutineId) {
      try {
        beginArguments();
        writeScope(frameId, goroutineId, getWriter());
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public final static class ListLocalVars extends Locals<DlvApi.LocalVariablesOut> {
    public ListLocalVars(int frameId, int goroutineId) {
      super(frameId, goroutineId);
    }
  }

  public final static class ListFunctionArgs extends Locals<DlvApi.LocalFunctionArgsOut> {
    public ListFunctionArgs(int frameId, int goroutineId) {
      super(frameId, goroutineId);
    }
  }

  public final static class Command extends DlvRequest<DlvApi.CommandOut> {
    public Command(@Nullable String command) {
      writeString("Name", command);
    }
  }

  public final static class Detach extends DlvRequest<Object> {
    public Detach(boolean kill) {
      try {
        beginArguments();
        getWriter().
                name("DetachIn").beginObject().name("Kill").value(kill).endObject();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public final static class Eval extends DlvRequest<DlvApi.EvalVariableOut> {
    public Eval(@Nonnull String expr, int frameId, int goroutineId) {
      try {
        beginArguments();
        writeScope(frameId, goroutineId, getWriter()).name("Expr").value(expr);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Nonnull
  private static JsonWriter writeScope(int frameId, int goroutineId, @Nonnull JsonWriter writer) throws IOException {
    // todo: ask vladimir how to simplify this
    return writer.name("Cfg").beginObject().name("FollowPointers").value(true).name("MaxStringLen").value(XValueNode.MAX_VALUE_LENGTH)
            .name("MaxVariableRecurse").value(1).name("MaxArrayValues").value(64).name("MaxStructFields").value(-1).endObject().name("Scope").beginObject()
            .name("GoroutineID").value(goroutineId).name("Frame").value(frameId).endObject();
  }

  public final static class Set extends DlvRequest<Object> {
    public Set(@Nonnull String symbol, @Nonnull String value, int frameId, int goroutineId) {
      try {
        beginArguments();
        writeScope(frameId, goroutineId, getWriter()).name("Symbol").value(symbol).name("Value").value(value);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}