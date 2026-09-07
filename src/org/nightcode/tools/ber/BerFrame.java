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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

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

  private static final int DEF_MAX_DEPTH = Integer.getInteger("org.nightcode.tools.ber.MaxDepth", 8);

  /**
   * Decode the BER data which contains in the supplied bytes array.
   *
   * @param src which contains the BER data
   */
  public static BerFrame parseFrom(final byte[] src) {
    ByteBuffer buffer = ByteBuffer.wrap(src);
    return parseFrom(buffer, 0, src.length);
  }

  public static BerFrame parseFrom(final byte[] src, int maxDepth) {
    ByteBuffer buffer = ByteBuffer.wrap(src);
    return parseFrom(buffer, 0, src.length, maxDepth);
  }

  /**
   * Decode the BER data which contains in the supplied {@link ByteBuffer}.
   *
   * @param srcBuffer which contains the BER data
   */
  public static BerFrame parseFrom(final ByteBuffer srcBuffer) {
    return parseFrom(srcBuffer, 0, srcBuffer.remaining());
  }

  public static BerFrame parseFrom(final ByteBuffer srcBuffer, int maxDepth) {
    return parseFrom(srcBuffer, 0, srcBuffer.remaining(), maxDepth);
  }

  /**
   * Decode the BER data which contains in the supplied {@link ByteBuffer}
   * with specified offset and length.
   *
   * @param srcBuffer which contains the BER data
   * @param offset in the supplied srcBuffer
   * @param length of the BER data in bytes
   */
  public static BerFrame parseFrom(final ByteBuffer srcBuffer, final int offset, final int length) {
    return parseFrom(srcBuffer, offset, length, DEF_MAX_DEPTH);
  }

  public static BerFrame parseFrom(final ByteBuffer srcBuffer, final int offset, final int length, int maxDepth) {
    if (maxDepth > DEF_MAX_DEPTH) {
      throw new IllegalArgumentException("maxDepth must be less or equal to " + DEF_MAX_DEPTH);
    }
    BerBuffer berBuffer = BerBufferUtil.create(srcBuffer);
    return BerParser.parseFrom(berBuffer, offset, length, maxDepth);
  }

  private final BerBuffer       buffer;
  private final int             offset;
  private final int             limit;
  private final List<BerTlv>    tlvs;
  private final List<Undecoded> undecoded;
  private final SearchStrategy  search;

  BerFrame(final BerBuffer buffer, final int offset, final int limit, final List<BerTlv> tlvs, final List<Undecoded> undecoded) {
    this(buffer, offset, limit, tlvs, undecoded, SearchStrategy.def());
  }

  BerFrame(final BerBuffer buffer, final int offset, final int limit, final List<BerTlv> tlvs, final List<Undecoded> undecoded, SearchStrategy search) {
    this.buffer    = buffer;
    this.offset    = offset;
    this.limit     = limit;
    this.tlvs      = tlvs;
    this.search    = search;
    this.undecoded = Collections.unmodifiableList(undecoded);
  }

  public void breadthFirstSearch(BerTlvVisitor visitor) {
    int depth = 0;
    Queue<BerTlv> queue = new LinkedList<>(tlvs);
    while (!queue.isEmpty()) {
      int levelSize = queue.size();
      for (int i = 0; i < levelSize; i++) {
        BerTlv tlv = queue.poll();
        if (tlv.isConstructed() && !tlv.children().isEmpty()) {
          visitor.onConstructed(ref(tlv, depth));
          queue.addAll(tlv.children());
        } else {
          visitor.onLeaf(ref(tlv, depth));
        }
      }
      depth++;
    }
  }

  public void depthFirstSearch(BerTlvVisitor visitor) {
    depthFirstSearch(visitor, 0, tlvs);
  }

  private void depthFirstSearch(BerTlvVisitor visitor, int depth, List<BerTlv> level) {
    for (BerTlv tlv : level) {
      BerTlvRef ref = ref(tlv, depth);
      if (tlv.isConstructed() && !tlv.children().isEmpty()) {
        visitor.onConstructed(ref);
        depthFirstSearch(visitor, depth + 1, tlv.children());
      } else {
        visitor.onLeaf(ref);
      }
    }
  }

  /**
   * Returns a list of objects containing all of the contents octets the given BER tag has,
   * or empty list if the BER tag does not exists.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public List<byte[]> getAllContents(final byte identifier) {
    return search.getAllContents(buffer, new byte[] {identifier}, tlvs);
  }

  /**
   * Returns a list of objects containing all of the contents octets the given BER tag has,
   * or empty list if the BER tag does not exists.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public List<byte[]> getAllContents(final int identifier) {
    return search.getAllContents(buffer, BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns a list of objects containing all of the contents octets the given BER tag has,
   * or empty list if the BER tag does not exists.
   *
   * @param identifier the BER tag
   * @return the contents octets
   */
  public List<byte[]> getAllContents(final long identifier) {
    return search.getAllContents(buffer, BerUtil.identifierToByteArray(identifier), tlvs);
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
    return search.getAllContents(buffer, identifier, tlvs);
  }

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets, or {@code null} if the BER tag does not exist
   */
  public byte[] getContent(final byte identifier) {
    return search.getContent(buffer, new byte[] {identifier}, tlvs);
  }

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets, or {@code null} if the BER tag does not exist
   */
  public byte[] getContent(final int identifier) {
    return search.getContent(buffer, BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets, or {@code null} if the BER tag does not exist
   */
  public byte[] getContent(final long identifier) {
    return search.getContent(buffer, BerUtil.identifierToByteArray(identifier), tlvs);
  }

  /**
   * Returns the contents octets of a BER tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the contents octets, or {@code null} if the BER tag does not exist
   */
  public byte[] getContent(byte... identifier) {
    if (identifier.length == 0) {
      return null;
    }
    return search.getContent(buffer, identifier, tlvs);
  }

  /**
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsAsciiString(final byte identifier) {
    return getContentAsAsciiString(new byte[] {identifier});
  }

  /**
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsAsciiString(final int identifier) {
    return getContentAsAsciiString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsAsciiString(final long identifier) {
    return getContentAsAsciiString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the ASCII coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the ASCII coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsAsciiString(byte... identifier) {
    byte[] content = search.getContent(buffer, identifier, tlvs);
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
   * @return the hex coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsHexString(final byte identifier) {
    return getContentAsHexString(new byte[] {identifier});
  }

  /**
   * Returns the hex coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the hex coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsHexString(final int identifier) {
    return getContentAsHexString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the hex coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the hex coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsHexString(final long identifier) {
    return getContentAsHexString(BerUtil.identifierToByteArray(identifier));
  }

  /**
   * Returns the hex coded contents octets of a BER tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the hex coded contents octets, or {@code null} if the BER tag does not exist
   */
  public String getContentAsHexString(byte... identifier) {
    byte[] content = search.getContent(buffer, identifier, tlvs);
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
   * @return the {@code BerFrame}, or {@code null} if the BER tag does not exist
   */
  public BerFrame getTag(final byte identifier) {
    return search.getTag(buffer, new byte[] {identifier}, tlvs, undecoded);
  }

  /**
   * Returns the {@code BerFrame} of a Ber tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the {@code BerFrame}, or {@code null} if the BER tag does not exist
   */
  public BerFrame getTag(final int identifier) {
    return search.getTag(buffer, BerUtil.identifierToByteArray(identifier), tlvs, undecoded);
  }

  /**
   * Returns the {@code BerFrame} of a Ber tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the {@code BerFrame}, or {@code null} if the BER tag does not exist
   */
  public BerFrame getTag(final long identifier) {
    return search.getTag(buffer, BerUtil.identifierToByteArray(identifier), tlvs, undecoded);
  }

  /**
   * Returns the {@code BerFrame} of a Ber tag, or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier, the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the {@code BerFrame}, or {@code null} if the BER tag does not exist
   */
  public BerFrame getTag(byte... identifier) {
    return search.getTag(buffer, identifier, tlvs, undecoded);
  }

  /**
   * Returns the byte array representation of a Ber tag,
   * or {@code null} if the BER tag does not exists.
   * If there are multiple BER tags with the same identifier,
   * the value returned is equal to
   * the first value in the list returned by getAllContents.
   *
   * @param identifier the BER tag
   * @return the byte array, or {@code null} if the BER tag does not exist
   */
  public byte[] getTagAsByteArray(final byte identifier) {
    BerFrame tag = search.getTag(buffer, new byte[] {identifier}, tlvs, undecoded);
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
   * @return the byte array, or {@code null} if the BER tag does not exist
   */
  public byte[] getTagAsByteArray(final int identifier) {
    BerFrame tag = search.getTag(buffer, BerUtil.identifierToByteArray(identifier), tlvs, undecoded);
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
   * @return the byte array, or {@code null} if the BER tag does not exist
   */
  public byte[] getTagAsByteArray(final long identifier) {
    BerFrame tag = search.getTag(buffer, BerUtil.identifierToByteArray(identifier), tlvs, undecoded);
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
   * @return the byte array, or {@code null} if the BER tag does not exist
   */
  public byte[] getTagAsByteArray(byte... identifier) {
    BerFrame tag = search.getTag(buffer, identifier, tlvs, undecoded);
    if (tag == null) {
      return null;
    }
    return tag.toByteArray();
  }

  public List<Undecoded> getUndecoded() {
    return undecoded;
  }

  public int tlvCount() {
    return tlvs.size();
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

  public BerFrame deepSearch() {
    return new BerFrame(buffer, offset, limit, tlvs, undecoded, DeepStrategy.instance());
  }

  public BerFrame levelSearch() {
    return new BerFrame(buffer, offset, limit, tlvs, undecoded, LevelStrategy.instance());
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

  private BerTlvRef ref(BerTlv tlv, int level) {
    byte[] identifier = new byte[tlv.identifierLength()];
    buffer.getBytes(tlv.identifierPosition(), identifier);
    int rawLength = tlv.contentPosition() - tlv.identifierPosition() + tlv.contentLength();
    return new BerTlvRef(identifier, tlv.identifierPosition(), rawLength, tlv.isConstructed(), level);
  }
}
