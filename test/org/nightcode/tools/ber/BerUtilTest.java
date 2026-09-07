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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BerUtilTest {

  @Test void shouldThrowExceptionForIllegalIdentifier() {
    Throwable th = assertThrows(IllegalStateException.class, () -> BerUtil.checkIdentifier(new byte[] {0x1E, 0x01 }));
    assertEquals("wrong identifier leading octet value: 0x1e", th.getMessage());

    th = assertThrows(IllegalStateException.class, () -> BerUtil.checkIdentifier(new byte[] {(byte) 0x9F, 0x1A, (byte) 0xFF }));
    assertEquals("wrong identifier value", th.getMessage());

    th = assertThrows(IllegalStateException.class, () -> BerUtil.checkIdentifier(new byte[] {0x1F, (byte) 0x80, 0x12 }));
    assertEquals("identifier has a non-minimal subsequent octet: 0x80", th.getMessage());
  }

  @Test void shouldThrowExceptionForIllegalHexString() {
    Throwable th = assertThrows(IllegalStateException.class, () -> BerUtil.hexToByteArray("012"));
    assertEquals("hexadecimal string <012> must have an even number of characters.", th.getMessage());
  }

  @Test void shouldThrowExceptionForInstanceCreation() throws ReflectiveOperationException {
    Constructor<BerUtil> constructor = BerUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }
}
