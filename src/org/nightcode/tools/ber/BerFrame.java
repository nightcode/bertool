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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Main BER tags container.
 */
public final class BerFrame {

  private final BerBuffer buffer;
  private final int offset;
  private final int limit;
  private final List<BerTlv> tlvs;

  BerFrame(final BerBuffer buffer, final int offset, final int limit, final List<BerTlv> tlvs) {
    this.buffer = buffer;
    this.offset = offset;
    this.limit = limit;
    this.tlvs = tlvs;
  }

  public @Nullable byte[] getContent(byte... identifier) {
    return getContent(identifier, tlvs);
  }

  public List<byte[]> getAllContents(byte... identifier) {
    return getAllContents(identifier, tlvs);
  }

  public List<BerTlv> getTlvs() {
    return tlvs;
  }

  public int limit() {
    return limit;
  }

  public int offset() {
    return offset;
  }

  BerBuffer berBuffer() {
    return buffer;
  }

  private @Nullable byte[] getContent(byte[] identifier, List<BerTlv> tlvs) {
    byte[] result = null;
    for (BerTlv tlv : tlvs) {
      if (contains(identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        byte[] content = new byte[tlv.contentLength()];
        buffer.getBytes(tlv.contentPosition(), content);
        result = content;
      } else if (tlv.isConstructed()) {
        result = getContent(identifier, tlv.children());
      }
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private List<byte[]> getAllContents(byte[] identifier, List<BerTlv> tlvs) {
    List<byte[]> result = new ArrayList<>();
    for (BerTlv tlv : tlvs) {
      if (contains(identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        byte[] content = new byte[tlv.contentLength()];
        buffer.getBytes(tlv.contentPosition(), content);
        result.add(content);
      } else if (tlv.isConstructed()) {
        result.addAll(getAllContents(identifier, tlv.children()));
      }
    }
    return result;
  }

  private boolean contains(byte[] target, final int position, final int length) {
    if (target.length != length || buffer.capacity() < position + target.length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (target[i] != buffer.getByte(position + i)) {
        return false;
      }
    }
    return true;
  }
}
