/*
 * Copyright (C) 2019 The NightCode Open Source Project
 *
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * Main BER tags container.
 */
public final class BerFrame {

  private static final class BerTlvIterator implements Iterator<byte[]> {

    private final BerBuffer buffer;
    private final Iterator<BerTlv> iterator;

    private BerTlv next;
    private boolean ready = false;

    private BerTlvIterator(BerFrame source) {
      this.buffer = source.buffer;
      this.iterator = source.tlvs.iterator();
    }

    @Override public boolean hasNext() {
      return ready || tryNext();
    }

    @Override public byte[] next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      ready = false;
      byte[] identifier = new byte[next.identifierLength()];
      buffer.getBytes(next.identifierPosition(), identifier);
      return identifier;
    }

    private boolean tryNext() {
      if (iterator.hasNext()) {
        next = iterator.next();
        ready = true;
        return true;
      }
      return false;
    }
  }

  /**
   * Decode the BER data which contains in the supplied bytes array.
   *
   * @param src which contains the BER data
   * @exception DecoderException
   */
  public static BerFrame parseFrom(final byte[] src) {
    ByteBuffer buffer = ByteBuffer.wrap(src);
    return parseFrom(buffer, 0, src.length);
  }

  /**
   * Decode the BER data which contains in the supplied {@link ByteBuffer}.
   *
   * @param srcBuffer which contains the BER data
   * @exception DecoderException
   */
  public static BerFrame parseFrom(final ByteBuffer srcBuffer) {
    return parseFrom(srcBuffer, 0, srcBuffer.limit());
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
  public static BerFrame parseFrom(final ByteBuffer srcBuffer, final int offset, final int length) {
    BerBuffer berBuffer = BerBufferUtil.create(srcBuffer);
    return BerParser.parseFrom(berBuffer, offset, length);
  }

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
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets
   */
  public @Nullable String getContentAsAsciiString(final byte identifier) {
    return getContentAsAsciiString(new byte[] {identifier});
  }

  /**
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets
   */
  public @Nullable String getContentAsAsciiString(final int identifier) {
    return getContentAsAsciiString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets
   */
  public @Nullable String getContentAsAsciiString(final long identifier) {
    return getContentAsAsciiString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets
   */
  public @Nullable String getContentAsAsciiString(byte... identifier) {
    byte[] content = getContent(identifier, tlvs);
    if (content == null) {
      return null;
    }
    return new String(content, BerUtil.ASCII);
  }

  /**
   * Returns the hex coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the hex coded contents octets
   */
  public @Nullable String getContentAsHexString(final byte identifier) {
    return getContentAsHexString(new byte[] {identifier});
  }

  /**
   * Returns the hex coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the hex coded contents octets
   */
  public @Nullable String getContentAsHexString(final int identifier) {
    return getContentAsHexString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the hex coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the hex coded contents octets
   */
  public @Nullable String getContentAsHexString(final long identifier) {
    return getContentAsHexString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the hex coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the hex coded contents octets
   */
  public @Nullable String getContentAsHexString(byte... identifier) {
    byte[] content = getContent(identifier, tlvs);
    if (content == null) {
      return null;
    }
    return BerUtil.byteArrayToHex(content);
  }

  /**
   * Returns the Iterator of BER tag identifiers of first level.
   *
   * @return the Iterator of BER tag identifiers of first level
   */
  public Iterator<byte[]> getIdentifiers() {
    return new BerTlvIterator(this);
  }

  /**
   * Returns the {@code BerFrame} of a Ber tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the {@code BerFrame}
   */
  public @Nullable BerFrame getTag(final byte identifier) {
    return getTag(new byte[] {identifier}, tlvs);
  }

  /**
   * Returns the {@code BerFrame} of a Ber tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the {@code BerFrame}
   */
  public @Nullable BerFrame getTag(final int identifier) {
    return getTag(BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns the {@code BerFrame} of a Ber tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the {@code BerFrame}
   */
  public @Nullable BerFrame getTag(final long identifier) {
    return getTag(BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns the {@code BerFrame} of a Ber tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the {@code BerFrame}
   */
  public @Nullable BerFrame getTag(byte... identifier) {
    return getTag(identifier, tlvs);
  }

  /**
   * Returns the byte array representation of a Ber tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier,
   * the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the byte array
   */
  public @Nullable byte[] getTagAsByteArray(final byte identifier) {
    BerFrame tag = getTag(new byte[] {identifier}, tlvs);
    if (tag == null) {
      return null;
    }
    return tag.toByteArray();
  }

  /**
   * Returns the byte array representation of a Ber tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier,
   * the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the byte array
   */
  public @Nullable byte[] getTagAsByteArray(final int identifier) {
    BerFrame tag = getTag(BerUtil.identifierToByteArray(identifier), tlvs);
    if (tag == null) {
      return null;
    }
    return tag.toByteArray();
  }

  /**
   * Returns the byte array representation of a Ber tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier,
   * the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the byte array
   */
  public @Nullable byte[] getTagAsByteArray(final long identifier) {
    BerFrame tag = getTag(BerUtil.identifierToByteArray(identifier), tlvs);
    if (tag == null) {
      return null;
    }
    return tag.toByteArray();
  }

  /**
   * Returns the byte array representation of a Ber tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier,
   * the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the byte array
   */
  public @Nullable byte[] getTagAsByteArray(byte... identifier) {
    BerFrame tag = getTag(identifier, tlvs);
    if (tag == null) {
      return null;
    }
    return tag.toByteArray();
  }

  /**
   * Returns the content of the BerFrame as a byte buffer.
   *
   * @return the content of the BerFrame as a byte buffer
   */
  public byte[] toByteArray() {
    int length = limit - offset;
    byte[] bytes = new byte[length];
    buffer.getBytes(offset, bytes);
    return bytes;
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

  private @Nullable BerFrame getTag(byte[] identifier, List<BerTlv> tlvs) {
    BerFrame result = null;
    for (BerTlv tlv : tlvs) {
      if (contains(identifier, tlv.identifierPosition(), tlv.identifierLength())) {
        result = new BerFrame(buffer, tlv.identifierPosition()
            , tlv.contentPosition() + tlv.contentLength(), Collections.singletonList(tlv));
      } else if (tlv.isConstructed()) {
        result = getTag(identifier, tlv.children());
      }
      if (result != null) {
        return result;
      }
    }
    return null;
  }
}
