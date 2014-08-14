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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Rule;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class BerUtilTest {

  @Rule
  public final ExpectedException exceptionRule = ExpectedException.none();

  @Theory
  public void shouldThrowExceptionForIllegalIdentifier() {
    exceptionRule.expectMessage("Wrong identifier leading octet value: 0x1e");
    BerUtil.checkIdentifier(new byte[] { 0x1E, 0x01 });
  }
  
  @Theory
  public void shouldThrowExceptionForIllegalHexString() {
    exceptionRule.expectMessage("hexadecimal string <012> must have an even number of characters.");
    BerUtil.hexToByteArray("012");
  }

  @Theory
  public void shouldThrowExceptionForInstanceCreation() throws ReflectiveOperationException {
    exceptionRule.expect(InvocationTargetException.class);
    Constructor<BerUtil> constructor = BerUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();
  }
}
