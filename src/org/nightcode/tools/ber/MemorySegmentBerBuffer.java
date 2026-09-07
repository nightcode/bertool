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

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class MemorySegmentBerBuffer implements BerBuffer {

  private static final ValueLayout.OfInt INT_BIG_ENDIAN = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);

  private final ByteBuffer    buffer;
  private final MemorySegment segment;
  private final int           capacity;

  MemorySegmentBerBuffer(byte[] src) {
    buffer   = null;
    segment  = MemorySegment.ofArray(src);
    capacity = src.length;
  }

  MemorySegmentBerBuffer(ByteBuffer src) {
    buffer   = src;
    segment  = MemorySegment.ofBuffer(src.duplicate().clear());
    capacity = src.capacity();
  }

  @Override public int capacity() {
    return capacity;
  }

  @Override public int checkIndex(final int index) {
    if (index >= capacity) {
      throw new IndexOutOfBoundsException(String.format("index is beyond bound (i=%d; b=%d)", index, capacity - 1));
    }
    return index;
  }

  @Override public int checkLimit(final int limit) {
    if (limit < 0 || limit > capacity) {
      throw new IndexOutOfBoundsException(String.format("limit is beyond capacity (l=%d; c=%d)", limit, capacity));
    }
    return limit;
  }

  @Override public ByteBuffer duplicateByteBuffer() {
    return (buffer != null) ? buffer.duplicate() : segment.asByteBuffer();
  }

  @Override public byte getByte(final int index) {
    return segment.get(ValueLayout.JAVA_BYTE, index);
  }

  @Override public int getBytes(final int index, final byte[] dst) {
    return getBytes(index, dst, 0, dst.length);
  }

  @Override public int getBytes(final int index, final byte[] dst, final int offset, final int length) {
    final int count = Math.min(length, capacity - index);
    MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, index, dst, offset, count);
    return count;
  }

  @Override public int getBytes(final int index, final ByteBuffer dstBuffer, final int length) {
    int count = Math.min(dstBuffer.remaining(), capacity - index);
    count = Math.min(count, length);

    MemorySegment.copy(segment, index, MemorySegment.ofBuffer(dstBuffer), 0, count);
    dstBuffer.position(dstBuffer.position() + count);
    return count;
  }

  @Override public void putByte(final int index, final byte value) {
    segment.set(ValueLayout.JAVA_BYTE, index, value);
  }

  @Override public int putBytes(final int index, final byte[] src) {
    return putBytes(index, src, 0, src.length);
  }

  @Override public int putBytes(final int index, final byte[] src, final int offset, final int length) {
    final int count = Math.min(length, capacity - index);
    MemorySegment.copy(src, offset, segment, ValueLayout.JAVA_BYTE, index, count);
    return count;
  }

  @Override public int putBytes(final int index, final ByteBuffer srcBuffer, final int length) {
    int count = Math.min(srcBuffer.remaining(), capacity - index);
    count = Math.min(count, length);

    MemorySegment.copy(MemorySegment.ofBuffer(srcBuffer), 0, segment, index, count);
    srcBuffer.position(srcBuffer.position() + count);
    return count;
  }

  @Override public void putInt(final int index, final int value) {
    segment.set(INT_BIG_ENDIAN, index, value);
  }
}
