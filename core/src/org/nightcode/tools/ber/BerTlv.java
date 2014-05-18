/*
 * Copyright (C) 2014 The NightCode Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nightcode.tools.ber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BerTlv {

  private final int identifierPosition;
  private final int identifierLength;
  private final boolean constructed;
  private final int contentPosition;
  private final int contentLength;
  private final List<BerTlv> children;

  public BerTlv(final int identifierPosition, final int identifierLength, final boolean constructed,
      final int contentPosition, final int contentLength) {
    this.identifierPosition = identifierPosition;
    this.identifierLength = identifierLength;
    this.constructed = constructed;
    this.contentPosition = contentPosition;
    this.contentLength = contentLength;
    if (constructed) {
      this.children = new ArrayList<>();
    } else {
      this.children = Collections.emptyList();
    }
  }

  public List<BerTlv> children() {
    return children;
  }

  public int contentLength() {
    return contentLength;
  }

  public int contentPosition() {
    return contentPosition;
  }

  public int identifierLength() {
    return identifierLength;
  }

  public int identifierPosition() {
    return identifierPosition;
  }

  public boolean isConstructed() {
    return constructed;
  }
}
