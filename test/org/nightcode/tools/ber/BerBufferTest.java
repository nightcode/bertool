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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BerBufferTest {

  private static final int BUFFER_CAPACITY = 1024 * 4;
  private static final int INDEX = 7;
  private static final byte BYTE_VALUE = 5;
  private static final byte[] BYTE_ARRAY_VALUE = "BER Tool".getBytes();
  private static final int INT_VALUE = 256;

  static Stream<BerBuffer> berBuffers() {
    return Stream.of(
        new MemorySegmentBerBuffer(new byte[BUFFER_CAPACITY])
        , new MemorySegmentBerBuffer(ByteBuffer.allocate(BUFFER_CAPACITY))
        , new MemorySegmentBerBuffer(ByteBuffer.allocateDirect(BUFFER_CAPACITY))
        , new HeapBerBuffer(new byte[BUFFER_CAPACITY])
        , new DirectBerBuffer(ByteBuffer.allocateDirect(BUFFER_CAPACITY)));
  }

  @ParameterizedTest @MethodSource("berBuffers")
  public void shouldGetCapacity(final BerBuffer buffer) {
    assertEquals(BUFFER_CAPACITY, buffer.capacity());
  }

  @ParameterizedTest @MethodSource("berBuffers")
  public void shouldThrowExceptionForLimitAboveCapacity(final BerBuffer berBuffer) {
    assertThrows(IndexOutOfBoundsException.class, () -> berBuffer.checkLimit(BUFFER_CAPACITY + 1));
  }

  @ParameterizedTest @MethodSource("berBuffers")
  public void shouldNotThrowExceptionForLimitAtCapacity(final BerBuffer berBuffer) {
    berBuffer.checkLimit(BUFFER_CAPACITY);
  }

  @ParameterizedTest @MethodSource("berBuffers")
  public void shouldThrowExceptionForIndexAtCapacity(final BerBuffer berBuffer) {
    assertThrows(IndexOutOfBoundsException.class, () -> berBuffer.checkIndex(BUFFER_CAPACITY));
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldNotThrowExceptionForIndexLessCapacity(final BerBuffer berBuffer) {
    berBuffer.checkIndex(BUFFER_CAPACITY - 1);
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldGetByteFromBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    duplicateBuffer.put(INDEX, BYTE_VALUE);

    assertEquals(BYTE_VALUE, berBuffer.getByte(INDEX));
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldGetBytesFromBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final byte[] actualBuffer = new byte[BYTE_ARRAY_VALUE.length];
    berBuffer.getBytes(INDEX, actualBuffer);

    assertArrayEquals(BYTE_ARRAY_VALUE, actualBuffer);
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldGetBytesFromBufferToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final ByteBuffer dstBuffer = ByteBuffer.allocate(BYTE_ARRAY_VALUE.length);
    berBuffer.getBytes(INDEX, dstBuffer, BYTE_ARRAY_VALUE.length);

    assertArrayEquals(BYTE_ARRAY_VALUE, dstBuffer.array());
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldGetBytesFromBufferToDirectBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();
    putBytes(duplicateBuffer);

    final ByteBuffer dstBuffer = ByteBuffer.allocateDirect(BYTE_ARRAY_VALUE.length);
    berBuffer.getBytes(INDEX, dstBuffer, BYTE_ARRAY_VALUE.length);

    final byte[] result = new byte[BYTE_ARRAY_VALUE.length];
    dstBuffer.flip();
    dstBuffer.get(result);

    assertArrayEquals(BYTE_ARRAY_VALUE, result);
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldPutByteToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putByte(INDEX, BYTE_VALUE);

    assertEquals(BYTE_VALUE, duplicateBuffer.get(INDEX));
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldPutBytesToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putBytes(INDEX, BYTE_ARRAY_VALUE);

    final byte[] buffer = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buffer);

    assertArrayEquals(BYTE_ARRAY_VALUE, buffer);
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldPutBytesToBufferFromBuffer(final BerBuffer buffer) {
    final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
    final ByteBuffer srcBuffer = ByteBuffer.wrap(BYTE_ARRAY_VALUE);

    buffer.putBytes(INDEX, srcBuffer, BYTE_ARRAY_VALUE.length);

    final byte[] buff = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buff);

    assertArrayEquals(BYTE_ARRAY_VALUE, buff);
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldPutBytesToBufferFromDirectBuffer(final BerBuffer buffer) {
    final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
    final ByteBuffer srcBuffer = ByteBuffer.allocateDirect(BYTE_ARRAY_VALUE.length);
    srcBuffer.put(BYTE_ARRAY_VALUE);
    srcBuffer.flip();

    buffer.putBytes(INDEX, srcBuffer, BYTE_ARRAY_VALUE.length);

    final byte[] buff = new byte[BYTE_ARRAY_VALUE.length];
    getBytes(duplicateBuffer, buff);

    assertArrayEquals(BYTE_ARRAY_VALUE, buff);
  }

  @ParameterizedTest @MethodSource("berBuffers")
  void shouldPutIntToBuffer(final BerBuffer berBuffer) {
    final ByteBuffer duplicateBuffer = berBuffer.duplicateByteBuffer();

    berBuffer.putInt(INDEX, INT_VALUE);

    assertEquals(INT_VALUE, duplicateBuffer.getInt(INDEX));
  }

  @Test void testIntTobByteArray() {
    byte[] expected = new byte[] {0, 0, 1, 0};
    byte[] actual = HeapBerBuffer.intTobByteArray(256);
    assertArrayEquals(expected, actual);
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
