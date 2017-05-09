package org.jetbrains.rpc;

import com.intellij.openapi.util.ThrowableComputable;
import org.jetbrains.concurrency.AsyncPromise;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public class RequestPromise<SUCCESS_RESPONSE, RESULT> extends AsyncPromise<RESULT> implements RequestCallback<SUCCESS_RESPONSE> {
  private String methodName;

  public RequestPromise(String methodName) {
    this.methodName = methodName;
  }

  @Override
  public void onSuccess(SUCCESS_RESPONSE response, ResultReader<SUCCESS_RESPONSE> resultReader) {
    catchError(this, () -> {
      if (resultReader == null || response == null) {
        //noinspection unchecked
        setResult((RESULT)response);
      }
      else {
        if (methodName == null) {
          setResult(null);
        }
        else {
          setResult(resultReader.readResult(methodName, response));
        }
      }

      return null;
    });
  }

  @Override
  public void onError(Throwable throwable) {
    setError(throwable);
  }

  static <T> T catchError(AsyncPromise<?> asyncPromise, ThrowableComputable<T, Throwable> func) {
    try {
      return func.compute();
    }
    catch (Throwable e) {
      asyncPromise.setError(e);
      return null;
    }
  }
}
