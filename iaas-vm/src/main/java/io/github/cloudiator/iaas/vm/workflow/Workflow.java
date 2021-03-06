/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
