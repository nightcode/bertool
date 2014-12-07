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

/**
 * The BerDecoder performs decoding BER packet.
 */
public class BerDecoder {

  private static final int MASK_CONSTRUCTED = 0x20;

  private static final int MASK_INDEFINITE_FORM = 0x80;
  private static final int MASK_DEFINITE_LONG_FORM = 0x80;

  /**
   * Decode the BER data which contains in the supplied bytes array.
   *
   * @param src which contains the BER data
   * @exception DecoderException
   */
  public BerFrame decode(final byte[] src) {
    ByteBuffer buffer = ByteBuffer.wrap(src);
    return decode(buffer, 0, src.length);
  }

  /**
   * Decode the BER data which contains in the supplied {@link ByteBuffer}.
   *
   * @param srcBuffer which contains the BER data
   * @exception DecoderException
   */
  public BerFrame decode(final ByteBuffer srcBuffer) {
    return decode(srcBuffer, 0, srcBuffer.limit());
  }

  /**
   * Decode the BER data which contains in the supplied {@link ByteBuffer}
   * with specified offset and length.
   *
   * @param srcBuffer which contains the BER data
   * @param offset in the supplied srcBuffer
   * @param length of the BER data in bytes
   * @exception java.lang.IndexOutOfBoundsException
   * @exception DecoderException
   */
  public BerFrame decode(final ByteBuffer srcBuffer, final int offset, final int length) {
    BerBuffer berBuffer = new BerBuffer(srcBuffer);
    return decode(berBuffer, offset, length);
  }

  private BerFrame decode(final BerBuffer berBuffer, final int offset, final int length) {
    final int limit = berBuffer.checkLimit(offset + length);
    List<BerTlv> root = new ArrayList<>();
    try {
      getLevel(berBuffer, root, offset, limit);
    } catch (Exception ex) {
      int undecodedLength;
      if (root.isEmpty()) {
        undecodedLength = length;
      } else {
        BerTlv last = root.get(root.size() - 1);
        undecodedLength = limit - last.contentPosition() - last.contentLength();
      }
      byte[] undecoded = new byte[undecodedLength];
      berBuffer.getBytes(limit - undecodedLength, undecoded);
      throw new DecoderException(ex, new BerFrame(berBuffer, offset, limit, root), undecoded);
    }
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
    src.checkIndex(index);
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
      // currently supported only int value
      if (numberOfSubsequentOctets > 4) {
        throw new IllegalStateException("Illegal ber packet structure.");
      }
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
