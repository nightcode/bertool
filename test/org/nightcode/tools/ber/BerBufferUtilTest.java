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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BerBufferUtilTest {

  private static final int BUFFER_CAPACITY = 1024 * 4;

  @Test
  public void testCreate() throws IOException {
    System.setProperty("org.nightcode.tools.ber.noUnsafe", "true");

    Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources("");
    List<URL> urls = new ArrayList<>();
    while (en.hasMoreElements()) {
      urls.add(en.nextElement());
    }

    URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0])
        , ClassLoader.getSystemClassLoader().getParent());

    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override public Object run() {
        try {
          Class<?> clazz = classLoader.loadClass(BerBufferUtil.class.getName());
          Method methodArray = clazz.getDeclaredMethod("create", byte[].class);
          methodArray.setAccessible(true);

          Method methodByteBuffer = clazz.getDeclaredMethod("create", ByteBuffer.class);
          methodByteBuffer.setAccessible(true);

          Object target = methodArray.invoke(null, new byte[BUFFER_CAPACITY]);
          Assert.assertEquals(HeapBerBuffer.class.getName(), target.getClass().getName());

          target = methodByteBuffer.invoke(null, ByteBuffer.allocate(BUFFER_CAPACITY));
          Assert.assertEquals(HeapBerBuffer.class.getName(), target.getClass().getName());

          target = methodByteBuffer.invoke(null, ByteBuffer.allocateDirect(BUFFER_CAPACITY));
          Assert.assertEquals(DirectBerBuffer.class.getName(), target.getClass().getName());

        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
        return null;
      }
    });
  }

  @Test
  public void testCreateUnsafe() throws IOException {
    System.setProperty("org.nightcode.tools.ber.noUnsafe", "false");

    Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources("");
    List<URL> urls = new ArrayList<>();
    while (en.hasMoreElements()) {
      urls.add(en.nextElement());
    }

    URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0])
        , ClassLoader.getSystemClassLoader().getParent());

    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override public Object run() {
        try {
          Class<?> clazz = classLoader.loadClass(BerBufferUtil.class.getName());
          Method methodArray = clazz.getDeclaredMethod("create", byte[].class);
          methodArray.setAccessible(true);

          Method methodByteBuffer = clazz.getDeclaredMethod("create", ByteBuffer.class);
          methodByteBuffer.setAccessible(true);

          Object target = methodArray.invoke(null, new byte[BUFFER_CAPACITY]);
          Assert.assertEquals(UnsafeBerBuffer.class.getName(), target.getClass().getName());

          target = methodByteBuffer.invoke(null, ByteBuffer.allocate(BUFFER_CAPACITY));
          Assert.assertEquals(UnsafeBerBuffer.class.getName(), target.getClass().getName());

          target = methodByteBuffer.invoke(null, ByteBuffer.allocateDirect(BUFFER_CAPACITY));
          Assert.assertEquals(UnsafeBerBuffer.class.getName(), target.getClass().getName());

        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
        return null;
      }
    });
  }

  @Test
  public void testGetBoolean() {
    String key = "tools.ber.test";

    Assert.assertTrue(BerBufferUtil.getBoolean(key, true));
    Assert.assertFalse(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "bla-bla");
    Assert.assertTrue(BerBufferUtil.getBoolean(key, true));
    Assert.assertFalse(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "true");
    Assert.assertTrue(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "false");
    Assert.assertFalse(BerBufferUtil.getBoolean(key, true));

    System.setProperty(key, "yes");
    Assert.assertTrue(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "no");
    Assert.assertFalse(BerBufferUtil.getBoolean(key, true));

    System.setProperty(key, "1");
    Assert.assertTrue(BerBufferUtil.getBoolean(key, false));

    System.setProperty(key, "0");
    Assert.assertFalse(BerBufferUtil.getBoolean(key, true));
  }
}
