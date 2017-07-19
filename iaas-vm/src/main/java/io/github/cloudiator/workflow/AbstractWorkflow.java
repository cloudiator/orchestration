package io.github.cloudiator.workflow;

/**
 * Created by daniel on 30.06.17.
 */
public abstract class AbstractWorkflow implements Activity {

  private final WorkflowBuilder workflowBuilder = WorkflowBuilder.newBuilder();

  @Override
  public final Exchange execute(Exchange input) {
    return workflowBuilder.build().execute(input);
  }

  protected WorkflowBuilder builder() {
    return workflowBuilder;
  }

  protected abstract void configure();
}
