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

import java.nio.ByteBuffer;

enum BerBufferUtil {
  ;

  private static final boolean USE_HEAP = getBoolean("org.nightcode.tools.ber.UseHeap", false);

  static BerBuffer create(byte[] src) {
    if (USE_HEAP) {
      return new HeapBerBuffer(src);
    }
    return new MemorySegmentBerBuffer(src);
  }

  static BerBuffer create(ByteBuffer src) {
    if (USE_HEAP) {
      if (src.hasArray()) {
        return new HeapBerBuffer(src.array());
      }
      return new DirectBerBuffer(src);
    }
    return new MemorySegmentBerBuffer(src);
  }

  static boolean getBoolean(String key, boolean def) {
    String value = System.getProperty(key);
    if (value == null) {
      return def;
    }
    value = value.trim().toLowerCase();

    if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
      return true;
    }
    if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
      return false;
    }

    return def;
  }
}
