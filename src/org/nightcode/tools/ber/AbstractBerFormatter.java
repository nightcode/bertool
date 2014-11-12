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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for BerFormatter implementations.
 */
public abstract class AbstractBerFormatter implements BerFormatter {

  @Override public byte[] lineFeed() {
    return LINE_FEED;
  }

  @Override public byte[] nextPrefix(boolean node) {
    return node ? NODE_NEXT_PREFIX : LEAF_NEXT_PREFIX;
  }

  void writeToStream(OutputStream stream, byte[] buffer, int offset, int length)
      throws IOException {
    int size = offset + length;
    for (int i = offset; i < size; i++) {
      stream.write((byte) BerUtil.UPPER_HEX_DIGITS[(buffer[i] & 0xF0) >> 4]);
      stream.write((byte) BerUtil.UPPER_HEX_DIGITS[buffer[i] & 0x0F]);
    }
  }

  void writeToStream(OutputStream stream, BerBuffer buffer, int offset, int length)
      throws IOException {
    int size = offset + length;
    buffer.checkLimit(size);
    for (int i = offset; i < size; i++) {
      stream.write((byte) BerUtil.UPPER_HEX_DIGITS[(buffer.getByte(i) & 0xF0) >> 4]);
      stream.write((byte) BerUtil.UPPER_HEX_DIGITS[buffer.getByte(i) & 0x0F]);
    }
  }

  void writeToStreamWithSpaces(OutputStream stream, BerBuffer buffer, int offset, int length)
      throws IOException {
    int size = offset + length;
    buffer.checkLimit(size);
    stream.write((byte) BerUtil.UPPER_HEX_DIGITS[(buffer.getByte(offset) & 0xF0) >> 4]);
    stream.write((byte) BerUtil.UPPER_HEX_DIGITS[buffer.getByte(offset) & 0x0F]);
    for (int i = offset + 1; i < size; i++) {
      stream.write(SPACE);
      if ((i - offset) % 8 == 0) {
        stream.write(SPACE);
      }
      stream.write((byte) BerUtil.UPPER_HEX_DIGITS[(buffer.getByte(i) & 0xF0) >> 4]);
      stream.write((byte) BerUtil.UPPER_HEX_DIGITS[buffer.getByte(i) & 0x0F]);
    }
  }
}
