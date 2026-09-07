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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

interface SearchStrategy {

  static SearchStrategy def() {
    return LevelStrategy.instance();
  }

  List<byte[]> getAllContents(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs);

  byte[] getContent(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs);

  BerFrame getTag(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs, List<Undecoded> undecoded);

  default boolean contains(BerBuffer src, byte[] target, final int position, final int length) {
    if (target.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (target[i] != src.getByte(position + i)) {
        return false;
      }
    }
    return true;
  }

  default List<Undecoded> getUndecoded(int offset, int limit, List<Undecoded> undecoded) {
    if (undecoded.isEmpty()) {
      return Collections.emptyList();
    }
    List<Undecoded> subset = new ArrayList<>();
    for (Undecoded u : undecoded) {
      if (u.offset() >= offset && u.offset() + u.length() <= limit) {
        subset.add(u);
      }
    }
    return subset;
  }
}
