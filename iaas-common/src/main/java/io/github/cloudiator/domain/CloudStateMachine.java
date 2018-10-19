package io.github.cloudiator.domain;

public class CloudStateMachine {

  public abstract static class Transition implements Runnable {

    private final LocalCloudState from;
    private final LocalCloudState to;

    protected Transition(LocalCloudState from, LocalCloudState to) {
      this.from = from;
      this.to = to;
    }

    public LocalCloudState from() {
      return from;
    }

    public LocalCloudState to() {
      return to;
    }
  }


}
