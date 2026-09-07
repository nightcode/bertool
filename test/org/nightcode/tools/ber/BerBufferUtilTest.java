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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BerBufferUtilTest {

  private static final int BUFFER_CAPACITY = 1024 * 4;

  @Test void testCreate() throws IOException {
    System.setProperty("org.nightcode.tools.ber.noUnsafe", "true");

    Enumeration<URL> en   = Thread.currentThread().getContextClassLoader().getResources("");
    List<URL>        urls = new ArrayList<>();
    while (en.hasMoreElements()) {
      urls.add(en.nextElement());
    }

    try (URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader().getParent())) {
      Class<?> clazz       = classLoader.loadClass(BerBufferUtil.class.getName());
      Method   methodArray = clazz.getDeclaredMethod("create", byte[].class);
      methodArray.setAccessible(true);

      Method methodByteBuffer = clazz.getDeclaredMethod("create", ByteBuffer.class);
      methodByteBuffer.setAccessible(true);

      Object target = methodArray.invoke(null, (Object) new byte[BUFFER_CAPACITY]);
      assertEquals(HeapBerBuffer.class.getName(), target.getClass().getName());

      target = methodByteBuffer.invoke(null, ByteBuffer.allocate(BUFFER_CAPACITY));
      assertEquals(HeapBerBuffer.class.getName(), target.getClass().getName());

      target = methodByteBuffer.invoke(null, ByteBuffer.allocateDirect(BUFFER_CAPACITY));
      assertEquals(DirectBerBuffer.class.getName(), target.getClass().getName());

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Test void testCreateUnsafe() throws IOException {
    System.setProperty("org.nightcode.tools.ber.noUnsafe", "false");

    Enumeration<URL> en   = Thread.currentThread().getContextClassLoader().getResources("");
    List<URL>        urls = new ArrayList<>();
    while (en.hasMoreElements()) {
      urls.add(en.nextElement());
    }

    try (URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader().getParent())) {
      Class<?> clazz       = classLoader.loadClass(BerBufferUtil.class.getName());
      Method   methodArray = clazz.getDeclaredMethod("create", byte[].class);
      methodArray.setAccessible(true);

      Method methodByteBuffer = clazz.getDeclaredMethod("create", ByteBuffer.class);
      methodByteBuffer.setAccessible(true);

      Object target = methodArray.invoke(null, new byte[BUFFER_CAPACITY]);
      assertEquals(UnsafeBerBuffer.class.getName(), target.getClass().getName());

      target = methodByteBuffer.invoke(null, ByteBuffer.allocate(BUFFER_CAPACITY));
      assertEquals(UnsafeBerBuffer.class.getName(), target.getClass().getName());

      target = methodByteBuffer.invoke(null, ByteBuffer.allocateDirect(BUFFER_CAPACITY));
      assertEquals(UnsafeBerBuffer.class.getName(), target.getClass().getName());

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Test void testGetBoolean() {
    String key = "tools.ber.test";

    assertTrue(BerBufferUtil.getBoolean(key, true));
    assertFalse(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "bla-bla");
    assertTrue(BerBufferUtil.getBoolean(key, true));
    assertFalse(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "true");
    assertTrue(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "false");
    assertFalse(BerBufferUtil.getBoolean(key, true));

    System.setProperty(key, "yes");
    assertTrue(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "no");
    assertFalse(BerBufferUtil.getBoolean(key, true));

    System.setProperty(key, "1");
    assertTrue(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "0");
    assertFalse(BerBufferUtil.getBoolean(key, true));
  }
}
