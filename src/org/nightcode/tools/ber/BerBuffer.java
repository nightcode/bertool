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

interface BerBuffer {

  int capacity();

  @CheckReturnValue(when = When.NEVER)
  int checkIndex(final int index);

  @CheckReturnValue(when = When.NEVER)
  int checkLimit(final int limit);

  ByteBuffer duplicateByteBuffer();

  byte getByte(final int index);

  int getBytes(final int index, final byte[] dst);

  int getBytes(final int index, final byte[] dst, final int offset, final int length);

  int getBytes(final int index, final ByteBuffer dstBuffer, final int length);

  void putByte(final int index, final byte value);

  int putBytes(final int index, final byte[] src);

  int putBytes(final int index, final byte[] src, final int offset, final int length);

  int putBytes(final int index, final ByteBuffer srcBuffer, final int length);

  void putInt(final int index, final int value);
}
