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

import java.util.ArrayList;
import java.util.List;

/**
 * The BerParser performs decoding BER packet.
 */
final class BerParser {

  private static final int MASK_CONSTRUCTED = 0x20;

  private static final int MASK_INDEFINITE_FORM    = 0x80;
  private static final int MASK_DEFINITE_LONG_FORM = 0x80;

  static BerFrame parseFrom(final BerBuffer berBuffer, final int offset, final int length, final int maxDepth) {
    if (maxDepth < 1) {
      throw new IllegalArgumentException("maxDepth must be positive");
    }
    final int       limit     = berBuffer.checkLimit(offset + length);
    List<BerTlv>    root      = new ArrayList<>();
    List<Undecoded> undecoded = new ArrayList<>();
    getLevel(berBuffer, root, offset, limit, 0, maxDepth - 1, undecoded); // maxDepth - 1 because root is not counted
    return new BerFrame(berBuffer, offset, limit, root, undecoded);
  }

  private static void getLevel(final BerBuffer src, final List<BerTlv> level, final int position, final int limit, final int depth, final int maxDepth,
                               final List<Undecoded> undecoded) {
    int index = position;
    while (index < limit) {
      int next = getBerTlv(src, index, level, limit, depth, maxDepth, undecoded);
      if (next < 0) {
        return;
      }
      if (index >= next) {
        undecoded(undecoded, index, next, depth, "trailing bytes");
        return;
      }
      index = next;
    }
  }

  private static int getBerTlv(final BerBuffer src, final int identPosition, final List<BerTlv> level, final int limit, final int depth, final int maxDepth,
                               final List<Undecoded> unparsable) {
    int index = identPosition;
    if (index >= limit) {
      return undecoded(unparsable, identPosition, limit, depth, "missing tag");
    }

    byte    firstIdentifier = src.getByte(index++);
    boolean constructed     = (firstIdentifier & MASK_CONSTRUCTED) == MASK_CONSTRUCTED;
    if ((firstIdentifier & 0x1F) == 0x1F) {
      byte b;
      do {
        if (index >= limit) {
          return undecoded(unparsable, identPosition, limit, depth, "truncated tag");
        }
        b = src.getByte(index++);
      } while ((b & 0x80) == 0x80);
    }
    if (index >= limit) {
      return undecoded(unparsable, identPosition, limit, depth, "missing length");
    }
    final int identLength = index - identPosition;
    int       firstLength = src.getByte(index++) & 0xFF;
    if ((firstLength ^ MASK_INDEFINITE_FORM) == 0) {
      return undecoded(unparsable, identPosition, limit, depth, "indefinite length form");
    }
    final int contentPos;
    int       contentLength = 0;
    if ((firstLength & MASK_DEFINITE_LONG_FORM) == MASK_DEFINITE_LONG_FORM) {
      int numberOfSubsequentOctets = firstLength & 0x7F;
      // currently supported only int value
      if (numberOfSubsequentOctets < 1 || numberOfSubsequentOctets > 4) {
        return undecoded(unparsable, identPosition, limit, depth, "invalid length");
      }
      contentPos = index + numberOfSubsequentOctets;
      if (contentPos > limit) {
        return undecoded(unparsable, identPosition, limit, depth, "truncated value");
      }
      for (int i = 0; i < numberOfSubsequentOctets; i++) {
        contentLength = (contentLength << 8) + (src.getByte(index++) & 0xFF);
      }
      if (contentLength < 0) {
        return undecoded(unparsable, identPosition, limit, depth, String.format("negative length (l=%d)", contentLength));
      }
    } else {
      contentPos    = index;
      contentLength = firstLength;
    }
    if (contentLength > limit - contentPos) {
      return undecoded(unparsable, identPosition, limit, depth
          , String.format("content bound is beyond content limit (p=%d, b=%d; l=%d)", contentPos, contentPos + contentLength, limit));
    }
    BerTlv tlv = new BerTlv(identPosition, identLength, constructed, contentPos, contentLength, depth);
    level.add(tlv);
    if (constructed && depth < maxDepth) {
      getLevel(src, tlv.children(), contentPos, contentPos + contentLength, depth + 1, maxDepth, unparsable);
    }
    return contentPos + contentLength;
  }

  private static int undecoded(List<Undecoded> unparsable, int offset, int limit, int depth, String reason) {
    unparsable.add(new Undecoded(offset, limit - offset, depth, reason));
    return -1;
  }

  private BerParser() {
    // do nothing
  }
}
