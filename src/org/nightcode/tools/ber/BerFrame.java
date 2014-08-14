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

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public @Nullable byte[] getContent(final byte identifier) {
    return getContent(new byte[] {identifier}, tlvs);
  }

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public @Nullable byte[] getContent(final int identifier) {
    return getContent(BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public @Nullable byte[] getContent(final long identifier) {
    return getContent(BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public @Nullable byte[] getContent(byte... identifier) {
    if (identifier.length == 0) {
      return null;
    }
    return getContent(identifier, tlvs);
  }

  /**
   * Returns a list of objects containing all of the contents octets the given BER tag has,
   * or empty list if the BER tag does not exists.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public List<byte[]> getAllContents(final byte identifier) {
    return getAllContents(BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns a list of objects containing all of the contents octets the given BER tag has,
   * or empty list if the BER tag does not exists.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public List<byte[]> getAllContents(final int identifier) {
    return getAllContents(BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns a list of objects containing all of the contents octets the given BER tag has,
   * or empty list if the BER tag does not exists.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public List<byte[]> getAllContents(final long identifier) {
    return getAllContents(BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns a list of objects containing all of the contents octets the given BER tag has,
   * or empty list if the BER tag does not exists.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public List<byte[]> getAllContents(byte... identifier) {
    if (identifier.length == 0) {
      return new ArrayList<>();
    }
    return getAllContents(identifier, tlvs);
  }

  BerBuffer berBuffer() {
    return buffer;
  }

  List<BerTlv> getTlvs() {
    return tlvs;
  }

  int limit() {
    return limit;
  }

  int offset() {
    return offset;
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
    if (target.length != length) {
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
