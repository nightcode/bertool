/*
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

  private final int          identifierPosition;
  private final int          identifierLength;
  private final boolean      constructed;
  private final int          contentPosition;
  private final int          contentLength;
  private final int          depth;
  private final List<BerTlv> children;

  BerTlv(int identifierPosition, int identifierLength, boolean constructed, int contentPosition, int contentLength, int depth) {
    this.identifierPosition = identifierPosition;
    this.identifierLength = identifierLength;
    this.constructed = constructed;
    this.contentPosition = contentPosition;
    this.contentLength = contentLength;
    this.depth = depth;
    if (constructed) {
      this.children = new ArrayList<>();
    } else {
      this.children = Collections.emptyList();
    }
  }

  List<BerTlv> children() {
    return children;
  }

  int contentLength() {
    return contentLength;
  }

  int contentPosition() {
    return contentPosition;
  }

  int depth() {
    return depth;
  }

  int identifierLength() {
    return identifierLength;
  }

  int identifierPosition() {
    return identifierPosition;
  }

  boolean isConstructed() {
    return constructed;
  }

  @Override public String toString() {
    return "BerTlv{"
           + "identifierPosition=" + identifierPosition
           + ", identifierLength=" + identifierLength
           + ", constructed=" + constructed
           + ", contentPosition=" + contentPosition
           + ", contentLength=" + contentLength
           + ", depth=" + depth
           + ", children=" + children
           + '}';
  }
}
