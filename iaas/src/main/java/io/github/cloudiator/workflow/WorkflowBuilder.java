package io.github.cloudiator.workflow;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by daniel on 03.02.17.
 */
public class WorkflowBuilder {

    private List<Activity> activities = new LinkedList<>();

    public WorkflowBuilder addActivity(Activity activity) {
        this.activities.add(activity);
        return this;
    }

    public Activity build() {
        return new Workflow(activities);
    }

}
