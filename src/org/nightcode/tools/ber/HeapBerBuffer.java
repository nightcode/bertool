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

import java.nio.ByteBuffer;

import javax.annotation.CheckReturnValue;
import javax.annotation.meta.When;

final class HeapBerBuffer implements BerBuffer {

  static byte[] intTobByteArray(final int src) {
    byte[] buffer = new byte[4];
    buffer[0] = (byte) (src >>> 24);
    buffer[1] = (byte) (src >>> 16);
    buffer[2] = (byte) (src >>>  8);
    buffer[3] = (byte) (src >>>  0);
    return buffer;
  }

  private final byte[] array;
  private final int capacity;

  HeapBerBuffer(byte[] src) {
    array = src;
    capacity = array.length;
  }

  @Override public int capacity() {
    return capacity;
  }

  @CheckReturnValue(when = When.NEVER)
  @Override public int checkIndex(final int index) {
    if (index >= capacity) {
      throw new IndexOutOfBoundsException(String.format("index is beyond bound (i=%d; b=%d)"
          , index, capacity - 1));
    }
    return index;
  }

  @CheckReturnValue(when = When.NEVER)
  @Override public int checkLimit(final int limit) {
    if (limit > capacity) {
      throw new IndexOutOfBoundsException(String.format("limit is beyond capacity (l=%d; c=%d)"
          , limit, capacity));
    }
    return limit;
  }

  @Override public ByteBuffer duplicateByteBuffer() {
    return ByteBuffer.wrap(array);
  }

  @Override public byte getByte(final int index) {
    return array[index];
  }

  @Override public int getBytes(final int index, final byte[] dst) {
    return getBytes(index, dst, 0, dst.length);
  }

  @Override public int getBytes(final int index, final byte[] dst, final int offset,
      final int length) {
    final int count = Math.min(length, capacity - index);
    System.arraycopy(array, index, dst, offset, length);
    return count;
  }

  @Override public int getBytes(final int index, final ByteBuffer dstBuffer, final int length) {
    int count = Math.min(dstBuffer.remaining(), capacity - index);
    count = Math.min(count, length);
    dstBuffer.put(array, index, count);
    return count;
  }

  @Override public void putByte(final int index, final byte value) {
    array[index] = value;
  }

  @Override public int putBytes(final int index, final byte[] src) {
    return putBytes(index, src, 0, src.length);
  }

  @Override public int putBytes(final int index, final byte[] src, final int offset,
      final int length) {
    final int count = Math.min(length, capacity - index);
    System.arraycopy(src, offset, array, index, count);
    return count;
  }

  @Override public int putBytes(final int index, final ByteBuffer srcBuffer, final int length) {
    int count = Math.min(srcBuffer.remaining(), capacity - index);
    count = Math.min(count, length);
    srcBuffer.get(array, index, count);
    return count;
  }

  @Override public void putInt(final int index, final int value) {
    byte[] src = intTobByteArray(value);
    System.arraycopy(src, 0, array, index, 4);
  }
}
