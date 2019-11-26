package io.github.cloudiator.orchestration.installer.tools.installer;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class InstallRetryer {

  private InstallRetryer() {
    throw new AssertionError("Do not instantiate");
  }

  public static <T> T retry(int maxSecondsWait, int attempts, Callable<T> callable) {
    Retryer retryer = RetryerBuilder.newBuilder()
        .retryIfRuntimeException()
        .retryIfExceptionOfType(RemoteException.class)
        .withWaitStrategy(WaitStrategies.randomWait(maxSecondsWait, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(attempts)).build();

    try {
      return (T) retryer.call(callable);
    } catch (ExecutionException var3) {
      throw new IllegalStateException("Execution failed with cause : " + var3.getCause().getMessage(), var3.getCause());
    } catch (RetryException var4) {
      throw new IllegalStateException("Retrying finally failed.", var4);
    }
  }
}


