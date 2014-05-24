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
import java.util.ArrayList;
import java.util.List;

final class BufferBerDecoder implements BerDecoder {

  @Override public BerFrame decode(final byte[] src) {
    ByteBuffer buffer = ByteBuffer.wrap(src);
    return decode(buffer, 0, src.length);
  }

  @Override public BerFrame decode(final ByteBuffer srcBuffer) {
    return decode(srcBuffer, 0, srcBuffer.limit());
  }

  @Override public BerFrame decode(final ByteBuffer srcBuffer, final int offset, final int length) {
    BerBuffer berBuffer = new BerBuffer(srcBuffer);
    return decode(berBuffer, offset, length);
  }

  private BerFrame decode(final BerBuffer berBuffer, final int offset, final int length) {
    final int limit = berBuffer.checkLimit(offset + length);
    List<BerTlv> root = new ArrayList<>();
    getLevel(berBuffer, root, offset, limit);
    return new BerFrame(berBuffer, offset, limit, root);
  }

  private void getLevel(final BerBuffer src, final List<BerTlv> level, final int position,
      final int limit) {
    int index = position;
    while (index < limit) {
      index = getBerTlv(src, index, level, limit);
    }
  }

  private int getBerTlv(final BerBuffer src, final int identPosition, final List<BerTlv> level,
      final int limit) {
    int index = identPosition;
    byte firstIdentifier = src.getByte(index++);
    boolean constructed = (firstIdentifier & MASK_CONSTRUCTED) == MASK_CONSTRUCTED;
    if ((firstIdentifier & 0x1F) == 0x1F) {
      byte b;
      do {
        src.checkIndex(index);
        b = src.getByte(index++);
      } while ((b & 0x80) == 0x80);
    }
    final int identLength = index - identPosition;
    final int contentPos;
    int contentLength = 0;
    src.checkIndex(index);
    int firstLength = src.getByte(index++) & 0xFF;
    if ((firstLength ^ MASK_INDEFINITE_FORM) == 0) {
      throw new IllegalStateException("Indefinite form is not supported yet.");
    }
    if ((firstLength & MASK_DEFINITE_LONG_FORM) == MASK_DEFINITE_LONG_FORM) {
      int numberOfSubsequentOctets = firstLength & 0x7F;
      contentPos = index + numberOfSubsequentOctets;
      for (int i = 0; i < numberOfSubsequentOctets; i++) {
        src.checkIndex(index);
        contentLength = (contentLength << 8) + (src.getByte(index++) & 0xFF);
      }
    } else {
      contentPos = index;
      contentLength = firstLength;
    }
    if (contentPos + contentLength > limit) {
        throw new IndexOutOfBoundsException(String
            .format("content bound is beyond content limit (b=%d; l=%d)"
                , contentPos + contentLength, limit));
    }
    BerTlv tlv = new BerTlv(identPosition, identLength, constructed, contentPos, contentLength);
    level.add(tlv);
    if (constructed) {
      getLevel(src, tlv.children(), contentPos, contentPos + contentLength);
    }
    return index + contentLength;
  }
}
