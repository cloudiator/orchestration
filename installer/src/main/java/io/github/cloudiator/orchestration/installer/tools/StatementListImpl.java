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

package io.github.cloudiator.orchestration.installer.tools;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by daniel on 08.02.17.
 */
public class StatementListImpl implements StatementList {

  private final List<Statement> statements;

  public StatementListImpl(List<Statement> statements) {
    this.statements = ImmutableList.copyOf(statements);
  }

  @Override
  public List<Statement> statements() {
    return statements;
  }

  @Override
  public Iterator<Statement> iterator() {
    return statements.iterator();
  }
}
