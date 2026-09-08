/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nightcode.tools.ber;

import java.util.Arrays;
import java.util.HexFormat;

public record BerTlvRef(byte[] tag, int offset, int length, boolean constructed, int depth) {

  @Override public byte[] tag() {
    return Arrays.copyOf(tag, tag.length);
  }

  @Override public boolean equals(Object obj) {
    if (!(obj instanceof BerTlvRef other)) {
      return false;
    }

    return offset == other.offset
           && length == other.length
           && constructed == other.constructed
           && depth == other.depth
           && Arrays.equals(tag, other.tag);
  }

  @Override public int hashCode() {
    int result = 17;
    result = 31 * result + Arrays.hashCode(tag);
    result = 31 * result + Boolean.hashCode(constructed);
    result = 31 * result + offset;
    result = 31 * result + length;
    return 31 * result + depth;
  }

  @Override public String toString() {
    return "BerTlvRef{"
           + "tag=" + HexFormat.of().withUpperCase().formatHex(tag)
           + ", offset=" + offset
           + ", length=" + length
           + ", constructed=" + constructed
           + ", depth=" + depth
           + '}';
  }
}
