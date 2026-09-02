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

final class DeepStrategy implements SearchStrategy {

  private static final DeepStrategy INSTANCE = new DeepStrategy();

  public static SearchStrategy instance() {
    return INSTANCE;
  }

  private DeepStrategy() {
    // do nothing
  }

  @Override public List<byte[]> getAllContents(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs) {
    List<byte[]> result = new ArrayList<>();
    for (BerTlv tlv : tlvs) {
      if (contains(buffer, identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        byte[] content = new byte[tlv.contentLength()];
        buffer.getBytes(tlv.contentPosition(), content);
        result.add(content);
      } else if (tlv.isConstructed()) {
        result.addAll(getAllContents(buffer, identifier, tlv.children()));
      }
    }
    return result;
  }

  @Override public byte[] getContent(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs) {
    byte[] result = null;
    for (BerTlv tlv : tlvs) {
      if (contains(buffer, identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        byte[] content = new byte[tlv.contentLength()];
        buffer.getBytes(tlv.contentPosition(), content);
        result = content;
      } else if (tlv.isConstructed()) {
        result = getContent(buffer, identifier, tlv.children());
      }
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override public BerFrame getTag(BerBuffer buffer, byte[] identifier, List<BerTlv> tlvs) {
    BerFrame result = null;
    for (BerTlv tlv : tlvs) {
      if (contains(buffer, identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        result = new BerFrame(buffer, tlv.identifierPosition(), tlv.contentPosition() + tlv.contentLength(), Collections.singletonList(tlv)
            , Collections.emptyList(), this);
      } else if (tlv.isConstructed()) {
        result = getTag(buffer, identifier, tlv.children());
      }
      if (result != null) {
        return result;
      }
    }
    return null;
  }
}
