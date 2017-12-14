package org.jetbrains.rpc;

import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.ThrowableComputable;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public class RequestPromise<SUCCESS_RESPONSE, RESULT> extends AsyncResult<RESULT> implements RequestCallback<SUCCESS_RESPONSE> {
  private String methodName;

  public RequestPromise(String methodName) {
    this.methodName = methodName;
  }

  @Override
  public void onSuccess(SUCCESS_RESPONSE response, ResultReader<SUCCESS_RESPONSE> resultReader) {
    catchError(this, () -> {
      if (resultReader == null || response == null) {
        //noinspection unchecked
        setDone((RESULT)response);
      }
      else {
        if (methodName == null) {
          setDone(null);
        }
        else {
          setDone(resultReader.readResult(methodName, response));
        }
      }

      return null;
    });
  }

  @Override
  public void onError(Throwable throwable) {
    rejectWithThrowable(throwable);
  }

  static <T> T catchError(AsyncResult<?> asyncPromise, ThrowableComputable<T, Throwable> func) {
    try {
      return func.compute();
    }
    catch (Throwable e) {
      asyncPromise.rejectWithThrowable(e);
      return null;
    }
  }
}
