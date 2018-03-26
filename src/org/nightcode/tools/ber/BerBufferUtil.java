/*
 * Copyright (C) 2018 The NightCode Open Source Project
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

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Unsafe;

final class BerBufferUtil {

  private static final Logger LOGGER = Logger.getLogger(BerBufferUtil.class.getName());

  private static final boolean HAS_UNSAFE;

  static {
    final boolean noUnsafe = getBoolean("org.nightcode.tools.ber.noUnsafe", false);
    LOGGER.log(Level.FINE, String.format("-Dorg.nightcode.tools.ber.noUnsafe: %s", noUnsafe));

    boolean hasUnsafe;
    if (noUnsafe) {
      hasUnsafe = false;
    } else {
      final Object result = AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override public Object run() {
          try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            unsafe.getClass().getDeclaredMethod("getByte", Object.class, long.class);
            unsafe.getClass().getDeclaredMethod("putByte", Object.class, long.class, byte.class);
            unsafe.getClass().getDeclaredMethod("putInt", Object.class, long.class, int.class);
            unsafe.getClass().getDeclaredMethod("copyMemory"
                , Object.class, long.class, Object.class, long.class, long.class);
            return Boolean.TRUE;
          } catch (Exception ex) {
            return Boolean.FALSE;
          }
        }
      });
      hasUnsafe = (Boolean) result;
      LOGGER.log(Level.FINER, String.format("sun.misc.Unsafe available: %s", hasUnsafe));
    }

    HAS_UNSAFE = hasUnsafe;
  }

  static BerBuffer create(byte[] src) {
    if (HAS_UNSAFE) {
      return new UnsafeBerBuffer(src);
    }
    return new HeapBerBuffer(src);
  }

  static BerBuffer create(ByteBuffer src) {
    if (HAS_UNSAFE) {
      return new UnsafeBerBuffer(src);
    }
    if (src.hasArray()) {
      return new HeapBerBuffer(src.array());
    }
    return new DirectBerBuffer(src);
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

  private BerBufferUtil() {
    // do nothing
  }
}
