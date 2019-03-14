/*
 * Copyright (C) 2019 The NightCode Open Source Project
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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;

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
  public static final BerBuffer UNSAFE_BYTE_ARRAY_BACKED
      = new UnsafeBerBuffer(new byte[BUFFER_CAPACITY]);

  @DataPoint
  public static final BerBuffer UNSAFE_HEAP_BYTE_BUFFER
      = new UnsafeBerBuffer(ByteBuffer.allocate(BUFFER_CAPACITY));

  @DataPoint
  public static final BerBuffer UNSAFE_DIRECT_BYTE_BUFFER
      = new UnsafeBerBuffer(ByteBuffer.allocateDirect(BUFFER_CAPACITY));

  @DataPoint
  public static final BerBuffer HEAP_BYTE_ARRAY = new HeapBerBuffer(new byte[BUFFER_CAPACITY]);

  @DataPoint
  public static final BerBuffer DIRECT_BYTE_BUFFER
      = new DirectBerBuffer(ByteBuffer.allocateDirect(BUFFER_CAPACITY));


  @Theory
  public void shouldGetCapacity(final BerBuffer buffer) {
    Assert.assertThat(Integer.valueOf(buffer.capacity()), is(Integer.valueOf(BUFFER_CAPACITY)));
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
  public void shouldThrowExceptionForIndexAtCapacity(final BerBuffer berBuffer) {
    exceptionRule.expect(IndexOutOfBoundsException.class);
    berBuffer.checkIndex(BUFFER_CAPACITY);
  }

  @Theory
  public void shouldNotThrowExceptionForIndexLessCapacity(final BerBuffer berBuffer) {
    berBuffer.checkIndex(BUFFER_CAPACITY - 1);
  }

  @Theory
  public void shouldGetByteFromBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    duplicateBuffer.put(INDEX, BYTE_VALUE);

    Assert.assertThat(Byte.valueOf(berBuffer.getByte(INDEX)), is(Byte.valueOf((BYTE_VALUE))));
  }

  @Theory
  public void shouldGetBytesFromBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final byte[] actualBuffer = new byte[BYTE_ARRAY_VALUE.length];
    berBuffer.getBytes(INDEX, actualBuffer);

    Assert.assertThat(actualBuffer, is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldGetBytesFromBufferToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final ByteBuffer dstBuffer = ByteBuffer.allocate(BYTE_ARRAY_VALUE.length);
    berBuffer.getBytes(INDEX, dstBuffer, BYTE_ARRAY_VALUE.length);

    Assert.assertThat(dstBuffer.array(), is(BYTE_ARRAY_VALUE));
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

    Assert.assertThat(result, is(BYTE_ARRAY_VALUE));
  }

  @Theory 
  public void shouldPutByteToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putByte(INDEX, BYTE_VALUE);

    Assert.assertThat(Byte.valueOf(duplicateBuffer.get(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
  }

  @Theory
  public void shouldPutBytesToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putBytes(INDEX, BYTE_ARRAY_VALUE);

    final byte[] buffer = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buffer);

    Assert.assertThat(buffer, is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldPutBytesToBufferFromBuffer(final BerBuffer buffer) {
    final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
    final ByteBuffer srcBuffer = ByteBuffer.wrap(BYTE_ARRAY_VALUE);

    buffer.putBytes(INDEX, srcBuffer, BYTE_ARRAY_VALUE.length);

    final byte[] buff = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buff);

    Assert.assertThat(buff, is(BYTE_ARRAY_VALUE));
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

    Assert.assertThat(buff, is(BYTE_ARRAY_VALUE));
  }

  @Theory
  public void shouldPutIntToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putInt(INDEX, INT_VALUE);

    Assert.assertThat(Integer.valueOf(duplicateBuffer.getInt(INDEX)), is(Integer.valueOf(INT_VALUE)));
  }

  @Test
  public void testIntTobByteArray() {
    byte[] expected = new byte[] {0, 0, 1, 0};
    byte[] actual = HeapBerBuffer.intTobByteArray(256);
    Assert.assertArrayEquals(expected, actual);
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
