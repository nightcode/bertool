/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nightcode.tools.ber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class LevelStrategy implements SearchStrategy {

  private static final LevelStrategy INSTANCE = new LevelStrategy();

  public static SearchStrategy instance() {
    return INSTANCE;
  }

  private LevelStrategy() {
    // do nothing
  }

  @Override public List<byte[]> getAllContents(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs) {
    List<byte[]> result = new ArrayList<>();
    for (BerTlv tlv : tlvs) {
      if (contains(buffer, identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        byte[] content = new byte[tlv.contentLength()];
        buffer.getBytes(tlv.contentPosition(), content);
        result.add(content);
      }
    }
    return result;
  }

  @Override public byte[] getContent(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs) {
    for (BerTlv tlv : tlvs) {
      if (contains(buffer, identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        byte[] content = new byte[tlv.contentLength()];
        buffer.getBytes(tlv.contentPosition(), content);
        return content;
      }
    }
    return null;
  }

  @Override public BerFrame getTag(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs) {
    for (BerTlv tlv : tlvs) {
      if (contains(buffer, identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        return new BerFrame(buffer, tlv.identifierPosition(), tlv.contentPosition() + tlv.contentLength(), Collections.singletonList(tlv)
            , Collections.emptyList(), this);
      }
    }
    return null;
  }
}
