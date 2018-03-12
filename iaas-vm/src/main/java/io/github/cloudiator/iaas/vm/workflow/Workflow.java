package io.github.cloudiator.iaas.vm.workflow;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Created by daniel on 03.02.17.
 */
public class Workflow implements Activity {

  private final List<Activity> activities;

  public Workflow(List<Activity> activities) {
    checkNotNull(activities, "activities is null");
    this.activities = ImmutableList.copyOf(activities);
  }

  @Override
  public Exchange execute(Exchange input) {
    Exchange inputForNextStep = input;
    for (Activity activity : activities) {
      inputForNextStep = activity.execute(inputForNextStep);
    }
    return inputForNextStep;
  }
}
