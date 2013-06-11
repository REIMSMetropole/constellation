/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.gui;

import juzu.Path;
import juzu.Route;
import juzu.View;
import juzu.Response;
import juzu.template.Template;

import javax.inject.Inject;
import java.io.IOException;

public class CommonController {

  @Inject
  @Path("commonIndex.gtmpl")
  Template index;

  @View
  @Route("/common")
  public Response index() throws IOException {
    return index.ok();
  }
}