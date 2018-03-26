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

final class DirectBerBuffer implements BerBuffer {

  private final ByteBuffer buffer;
  private final int capacity;

  DirectBerBuffer(ByteBuffer src) {
    buffer = src;
    capacity = src.capacity();
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
    return buffer.duplicate();
  }

  @Override public byte getByte(final int index) {
    return buffer.get(index);
  }

  @Override public int getBytes(final int index, final byte[] dst) {
    return getBytes(index, dst, 0, dst.length);
  }

  @Override public int getBytes(final int index, final byte[] dst, final int offset,
      final int length) {
    final int count = Math.min(length, capacity - index);
    buffer.position(index);
    buffer.get(dst, offset, count);
    return count;
  }

  @Override public int getBytes(final int index, final ByteBuffer dstBuffer, final int length) {
    int count = Math.min(dstBuffer.remaining(), capacity - index);
    count = Math.min(count, length);
    buffer.position(index);
    buffer.limit(index + count);
    dstBuffer.put(buffer.slice());
    return count;
  }

  @Override public void putByte(final int index, final byte value) {
    buffer.put(index, value);
  }

  @Override public int putBytes(final int index, final byte[] src) {
    return putBytes(index, src, 0, src.length);
  }

  @Override public int putBytes(final int index, final byte[] src, final int offset,
      final int length) {
    final int count = Math.min(length, capacity - index);
    buffer.position(index);
    buffer.put(src, offset, count);
    return count;
  }

  @Override public int putBytes(final int index, final ByteBuffer srcBuffer, final int length) {
    int count = Math.min(srcBuffer.remaining(), capacity - index);
    count = Math.min(count, length);
    ByteBuffer src = srcBuffer.duplicate();
    src.limit(length);
    buffer.position(index);
    buffer.put(src.slice());
    srcBuffer.position(srcBuffer.position() + count);
    return count;
  }

  @Override public void putInt(final int index, final int value) {
    buffer.putInt(index, value);
  }
}
