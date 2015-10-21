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

import java.nio.ByteBuffer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class BerBufferTest {

  private static final int BUFFER_CAPACITY = 1024 * 4;
  private static final int INDEX = 7;
  private static final byte BYTE_VALUE = 5;
  private static final byte[] BYTE_ARRAY_VALUE = "BER Tool".getBytes();
  private static final int INT_VALUE = 256;

  @Rule
  public final ExpectedException exceptionRule = ExpectedException.none();

  @DataPoint
  public static final BerBuffer BYTE_ARRAY_BACKED = new BerBuffer(new byte[BUFFER_CAPACITY]);

  @DataPoint
  public static final BerBuffer HEAP_BYTE_BUFFER
      = new BerBuffer(ByteBuffer.allocate(BUFFER_CAPACITY));

  @DataPoint
  public static final BerBuffer DIRECT_BYTE_BUFFER
      = new BerBuffer(ByteBuffer.allocateDirect(BUFFER_CAPACITY));
  
  @Test
  public void testHasArray() {
    assertTrue(BYTE_ARRAY_BACKED.hasArray());
    assertTrue(HEAP_BYTE_BUFFER.hasArray());
    assertFalse(DIRECT_BYTE_BUFFER.hasArray());
  }

  @Test
  public void testArray() {
    assertNotNull(BYTE_ARRAY_BACKED.array());
    assertNotNull(HEAP_BYTE_BUFFER.array());
    assertNull(DIRECT_BYTE_BUFFER.array());
  }

  @Theory
  public void shouldGetCapacity(final BerBuffer buffer) {
    assertThat(Integer.valueOf(buffer.capacity()), is(Integer.valueOf(BUFFER_CAPACITY)));
  }

  @Theory
  public void shouldThrowExceptionForLimitAboveCapacity(final BerBuffer berBuffer) {
    exceptionRule.expect(IndexOutOfBoundsException.class);
    berBuffer.checkLimit(BUFFER_CAPACITY + 1);
  }

  @Theory
  public void shouldNotThrowExceptionForLimitAtCapacity(final BerBuffer berBuffer) {
    berBuffer.checkLimit(BUFFER_CAPACITY);
  }

  @Theory
  public void shouldGetByteFromBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    duplicateBuffer.put(INDEX, BYTE_VALUE);

    assertThat(Byte.valueOf(berBuffer.getByte(INDEX)), is(Byte.valueOf((BYTE_VALUE))));
  }

  @Theory
  public void shouldGetBytesFromBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final byte[] actualBuffer = new byte[BYTE_ARRAY_VALUE.length];
    berBuffer.getBytes(INDEX, actualBuffer);

    assertThat(actualBuffer, is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldGetBytesFromBufferToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final ByteBuffer dstBuffer = ByteBuffer.allocate(BYTE_ARRAY_VALUE.length);
    berBuffer.getBytes(INDEX, dstBuffer, BYTE_ARRAY_VALUE.length);

    assertThat(dstBuffer.array(), is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldGetBytesFromBufferToDirectBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final ByteBuffer dstBuffer = ByteBuffer.allocateDirect(BYTE_ARRAY_VALUE.length);
    berBuffer.getBytes(INDEX, dstBuffer, BYTE_ARRAY_VALUE.length);

    final byte[] result = new byte[BYTE_ARRAY_VALUE.length];
    dstBuffer.flip();
    dstBuffer.get(result);
    
    assertThat(result, is(BYTE_ARRAY_VALUE));
  }

  @Theory 
  public void shouldPutByteToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putByte(INDEX, BYTE_VALUE);

    assertThat(Byte.valueOf(duplicateBuffer.get(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
  }

  @Theory
  public void shouldPutBytesToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putBytes(INDEX, BYTE_ARRAY_VALUE);

    final byte[] buffer = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buffer);

    assertThat(buffer, is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldPutBytesToBufferFromBuffer(final BerBuffer buffer) {
    final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
    final ByteBuffer srcBuffer = ByteBuffer.wrap(BYTE_ARRAY_VALUE);

    buffer.putBytes(INDEX, srcBuffer, BYTE_ARRAY_VALUE.length);

    final byte[] buff = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buff);

    assertThat(buff, is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldPutBytesToBufferFromDirectBuffer(final BerBuffer buffer) {
    final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
    final ByteBuffer srcBuffer = ByteBuffer.allocateDirect(BYTE_ARRAY_VALUE.length);
    srcBuffer.put(BYTE_ARRAY_VALUE);
    srcBuffer.flip();

    buffer.putBytes(INDEX, srcBuffer, BYTE_ARRAY_VALUE.length);

    final byte[] buff = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buff);

    assertThat(buff, is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldPutIntToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putInt(INDEX, INT_VALUE);

    assertThat(Integer.valueOf(duplicateBuffer.getInt(INDEX)), is(Integer.valueOf(INT_VALUE)));
  }

  private void getBytes(final ByteBuffer buffer, final byte[] dstBuffer) {
    buffer.position(INDEX);
    buffer.get(dstBuffer);
  }

  private void putBytes(final ByteBuffer buffer) {
    buffer.position(INDEX);
    buffer.put(BYTE_ARRAY_VALUE);
  }
}
