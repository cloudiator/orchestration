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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Created by daniel on 08.02.17.
 */
public class DownloadImpl implements Download {

  private final String url;
  @Nullable
  private final String fileName;

  DownloadImpl(String url, @Nullable String fileName) {
    checkNotNull(url, "url is null.");
    if (fileName != null) {
      checkArgument(!fileName.isEmpty(), "filename is empty.");
    }
    this.url = url;
    this.fileName = fileName;
  }

  @Override
  public String url() {
    return url;
  }

  @Override
  public Optional<String> fileName() {
    return Optional.ofNullable(fileName);
  }
}
