/*
 * Copyright (C) 2019 The NightCode Open Source Project
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects BER tags which will be used to create ber packet.
 */
public final class BerBuilder {

  /**
   * Creates new BerBuilder instance.
   *
   * @return new BerBuilder instance.
   */
  public static BerBuilder newInstance() {
    return new BerBuilder();
  }

  private static abstract class BerTlvContainer {

    void putLengthOctets(final OutputStream out, final int contentLength) throws IOException {
      if (contentLength < 0x80) {
        out.write((byte) (contentLength & 0x7F));
      } else if (contentLength < 0x100) {
        out.write((byte) 0x81);
        out.write((byte) contentLength);
      } else if (contentLength < 0x10000) {
        out.write((byte) 0x82);
        out.write((byte) (contentLength >>> 8));
        out.write((byte) (contentLength >>> 0));
      } else if (contentLength < 0x1000000) {
        out.write((byte) 0x83);
        out.write((byte) (contentLength >>> 16));
        out.write((byte) (contentLength >>>  8));
        out.write((byte) (contentLength >>>  0));
      } else {
        out.write((byte) 0x84);
        out.write((byte) (contentLength >>> 24));
        out.write((byte) (contentLength >>> 16));
        out.write((byte) (contentLength >>>  8));
        out.write((byte) (contentLength >>>  0));
      }
    }

    void putLengthOctets(final BerBuffer buffer, final int offset, final int contentLength) {
      if (contentLength < 0x80) {
        buffer.putByte(offset, (byte) (contentLength & 0x7F));
      } else if (contentLength < 0x100) {
        buffer.putByte(offset, (byte) 0x81);
        buffer.putByte(offset + 1, (byte) contentLength);
      } else if (contentLength < 0x10000) {
        buffer.putByte(offset, (byte) 0x82);
        buffer.putByte(offset + 1, (byte) (contentLength >>> 8));
        buffer.putByte(offset + 2, (byte) (contentLength >>> 0));
      } else if (contentLength < 0x1000000) {
        buffer.putByte(offset, (byte) 0x83);
        buffer.putByte(offset + 1, (byte) (contentLength >>> 16));
        buffer.putByte(offset + 2, (byte) (contentLength >>>  8));
        buffer.putByte(offset + 3, (byte) (contentLength >>>  0));
      } else {
        buffer.putByte(offset, (byte) 0x84);
        buffer.putInt(offset + 1, contentLength);
      }
    }

    abstract int writeTo(BerBuffer buffer, int offset);

    abstract void writeTo(OutputStream out) throws IOException;
  }

  private static final class BerTlvContainerByteArray extends BerTlvContainer {
    private final byte[] identifier;
    private final int numberOfLengthOctets;
    private final byte[] content;

    private BerTlvContainerByteArray(final byte[] identifier, final int numberOfLengthOctets, final byte[] content) {
      BerUtil.checkIdentifier(identifier);
      this.identifier = identifier;
      this.numberOfLengthOctets = numberOfLengthOctets;
      this.content = content;
    }

    @Override public int writeTo(final BerBuffer buffer, final int offset) {
      buffer.putBytes(offset, identifier);
      int index = offset + identifier.length;
      putLengthOctets(buffer, index, content.length);
      index += numberOfLengthOctets;
      buffer.putBytes(index, content);
      return index + content.length;
    }

    @Override void writeTo(OutputStream out) throws IOException {
      out.write(identifier);
      putLengthOctets(out, content.length);
      out.write(content);
    }
  }

  private static final class BerTlvContainerBuilder extends BerTlvContainer {
    private final byte[] identifier;
    private final int numberOfLengthOctets;
    private final BerBuilder builder;

    private BerTlvContainerBuilder(final byte[] identifier, final int numberOfLengthOctets, final BerBuilder builder) {
      BerUtil.checkIdentifier(identifier);
      this.identifier = identifier;
      this.numberOfLengthOctets = numberOfLengthOctets;
      this.builder = builder;
    }

    @Override public int writeTo(final BerBuffer buffer, final int offset) {
      buffer.putBytes(offset, identifier);
      int index = offset + identifier.length;
      putLengthOctets(buffer, index, builder.length);
      index += numberOfLengthOctets;
      builder.writeTo(buffer, index);
      return index + builder.length;
    }

    @Override void writeTo(OutputStream out) throws IOException {
      out.write(identifier);
      putLengthOctets(out, builder.length);
      builder.writeTo(out);
    }
  }

  private int length;
  private List<BerTlvContainer> containers;

  private BerBuilder() {
    containers = new ArrayList<>();
    length = 0;
  }

  /**
   * Adds all BER tags from supplied BerFrame.
   *
   * @param berFrame the supplied BerFrame
   */
  public BerBuilder add(BerFrame berFrame) {
    for (BerTlv berTlv : berFrame.getTlvs()) {
      byte[] identifier = new byte[berTlv.identifierLength()];
      berFrame.berBuffer().getBytes(berTlv.identifierPosition(), identifier);
      byte[] content = new byte[berTlv.contentLength()];
      berFrame.berBuffer().getBytes(berTlv.contentPosition(), content);
      add(identifier, content);
    }
    return this;
  }

  /**
   * Adds a bytes array for encoding.
   *
   * @param identifier the BER tag value
   * @param content the contents octets
   */
  public BerBuilder add(final byte identifier, final byte[] content) {
    return add(new byte[] {identifier}, content);
  }

  /**
   * Adds a bytes array for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte (right-most)
   * @param content the contents octets
   */
  public BerBuilder add(final byte b1, final byte b2, final byte[] content) {
    return add(new byte[] {b1, b2}, content);
  }

  /**
   * Adds a bytes array for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte (right-most)
   * @param content the contents octets
   */
  public BerBuilder add(final byte b1, final byte b2, final byte b3, final byte[] content) {
    return add(new byte[] {b1, b2, b3}, content);
  }

  /**
   * Adds a bytes array for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte
   * @param b4 the BER tag value fourth byte (right-most)
   * @param content the contents octets
   */
  public BerBuilder add(final byte b1, final byte b2, final byte b3, final byte b4, final byte[] content) {
    return add(new byte[] {b1, b2, b3, b4}, content);
  }

  /**
   * Adds a bytes array for encoding.
   *
   * @param identifier the BER tag value
   * @param content the contents octets
   */
  public BerBuilder add(final int identifier, final byte[] content) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return add(buffer, content);
  }

  /**
   * Adds a bytes array for encoding.
   *
   * @param identifier the BER tag value
   * @param content the contents octets
   */
  public BerBuilder add(final long identifier, final byte[] content) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return add(buffer, content);
  }

  /**
   * Adds a bytes array for encoding.
   *
   * @param identifier the BER tag value
   * @param content the contents octets
   */
  public BerBuilder add(final byte[] identifier, final byte[] content) {
    final int numberOfLengthOctets = calculateNumberOfLengthOctets(content.length);
    BerTlvContainer container = new BerTlvContainerByteArray(identifier, numberOfLengthOctets, content);
    containers.add(container);
    length += (identifier.length + numberOfLengthOctets + content.length);
    return this;
  }

  /**
   * Adds a BerBuilder for encoding.
   *
   * @param identifier the BER tag value
   * @param builder the contents octets
   */
  public BerBuilder add(final byte identifier, final BerBuilder builder) {
    return add(new byte[] {identifier}, builder);
  }

  /**
   * Adds a BerBuilder for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte (right-most)
   * @param builder the contents octets
   */
  public BerBuilder add(final byte b1, final byte b2, final BerBuilder builder) {
    return add(new byte[] {b1, b2}, builder);
  }

  /**
   * Adds a BerBuilder for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte (right-most)
   * @param builder the contents octets
   */
  public BerBuilder add(final byte b1, final byte b2, final byte b3, final BerBuilder builder) {
    return add(new byte[] {b1, b2, b3}, builder);
  }

  /**
   * Adds a BerBuilder for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte
   * @param b4 the BER tag value fourth byte (right-most)
   * @param builder the contents octets
   */
  public BerBuilder add(final byte b1, final byte b2, final byte b3, final byte b4, final BerBuilder builder) {
    return add(new byte[] {b1, b2, b3, b4}, builder);
  }

  /**
   * Adds a BerBuilder for encoding.
   *
   * @param identifier the BER tag value
   * @param builder the contents octets
   */
  public BerBuilder add(final int identifier, final BerBuilder builder) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return add(buffer, builder);
  }

  /**
   * Adds a BerBuilder for encoding.
   *
   * @param identifier the BER tag value
   * @param builder the contents octets
   */
  public BerBuilder add(final long identifier, final BerBuilder builder) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return add(buffer, builder);
  }

  /**
   * Adds a BerBuilder for encoding.
   *
   * @param identifier the BER tag value
   * @param builder the contents octets
   */
  public BerBuilder add(final byte[] identifier, final BerBuilder builder) {
    final int numberOfLengthOctets = calculateNumberOfLengthOctets(builder.length);
    BerTlvContainer container = new BerTlvContainerBuilder(identifier, numberOfLengthOctets, builder);
    containers.add(container);
    length += (identifier.length + numberOfLengthOctets + builder.length);
    return this;
  }

  /**
   * Adds an ASCII string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
  public BerBuilder addAsciiString(final byte identifier, final String src) {
    return addAsciiString(new byte[] {identifier}, src);
  }

  /**
   * Adds an ASCII string for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte (right-most)
   * @param src the contents octets
   */
   public BerBuilder addAsciiString(final byte b1, final byte b2, final String src) {
    return addAsciiString(new byte[] {b1, b2}, src);
  }

  /**
   * Adds an ASCII string for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte (right-most)
   * @param src the contents octets
   */
   public BerBuilder addAsciiString(final byte b1, final byte b2, final byte b3, final String src) {
    return addAsciiString(new byte[] {b1, b2, b3}, src);
  }

  /**
   * Adds an ASCII string for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte
   * @param b4 the BER tag value fourth byte (right-most)
   * @param src the contents octets
   */
   public BerBuilder addAsciiString(final byte b1, final byte b2, final byte b3, final byte b4, final String src) {
    return addAsciiString(new byte[] {b1, b2, b3, b4}, src);
  }

  /**
   * Adds an ASCII string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
  public BerBuilder addAsciiString(final int identifier, final String src) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return addAsciiString(buffer, src);
  }

  /**
   * Adds an ASCII string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
   public BerBuilder addAsciiString(final long identifier, final String src) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return addAsciiString(buffer, src);
  }

  /**
   * Adds an ASCII string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
   public BerBuilder addAsciiString(final byte[] identifier, final String src) {
    return add(identifier, src.getBytes(BerUtil.ASCII));
  }

  /**
   * Adds a hex string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
  public BerBuilder addHexString(final byte identifier, final String src) {
    return addHexString(new byte[] {identifier}, src);
  }

  /**
   * Adds a hex string for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte (right-most)
   * @param src the contents octets
   */
  public BerBuilder addHexString(final byte b1, final byte b2, final String src) {
    return addHexString(new byte[] {b1, b2}, src);
  }

  /**
   * Adds a hex string for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte (right-most)
   * @param src the contents octets
   */
  public BerBuilder addHexString(final byte b1, final byte b2, final byte b3, final String src) {
    return addHexString(new byte[] {b1, b2, b3}, src);
  }

  /**
   * Adds a hex string for encoding.
   *
   * @param b1 the BER tag value first byte (left-most)
   * @param b2 the BER tag value second byte
   * @param b3 the BER tag value third byte
   * @param b4 the BER tag value fourth byte (right-most)
   * @param src the contents octets
   */
  public BerBuilder addHexString(final byte b1, final byte b2, final byte b3, final byte b4, final String src) {
    return addHexString(new byte[] {b1, b2, b3, b4}, src);
  }

  /**
   * Adds a hex string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
  public BerBuilder addHexString(final int identifier, final String src) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return addHexString(buffer, src);
  }

  /**
   * Adds a hex string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
  public BerBuilder addHexString(final long identifier, final String src) {
    final byte[] buffer = BerUtil.identifierToByteArray(identifier);
    return addHexString(buffer, src);
  }

  /**
   * Adds a hex string for encoding.
   *
   * @param identifier the BER tag value
   * @param src the contents octets
   */
  public BerBuilder addHexString(final byte[] identifier, final String src) {
    return add(identifier, BerUtil.hexToByteArray(src));
  }

  /**
   * Returns the length of encoding.
   *
   * @return the length of encoding
   */
  public int length() {
    return length;
  }

  /**
   * Encode the BER data which contains in the builder.
   *
   * @param dst the destination of encoded content
   */
  public void writeTo(byte[] dst) {
    final ByteBuffer byteBuffer = ByteBuffer.wrap(dst);
    writeTo(byteBuffer);
  }

  /**
   * Encode the BER data which contains in the builder.
   *
   * @param dstBuffer the destination of encoded content
   */
  public void writeTo(ByteBuffer dstBuffer) {
    writeTo(dstBuffer, 0);
  }

  /**
   * Encode the BER data which contains in the builder.
   *
   * @param dstBuffer the destination of encoded content
   * @param offset in the supplied dstBuffer
   */
  public void writeTo(ByteBuffer dstBuffer, int offset) {
    final BerBuffer berBuffer = BerBufferUtil.create(dstBuffer);
    berBuffer.checkLimit(offset + length);
    writeTo(berBuffer, offset);
  }

  /**
   * Encode the BER data which contains in the builder.
   *
   * @param out the destination of encoded content
   * @throws IOException if an I/O error occurs
   */
  public void writeTo(OutputStream out) throws IOException {
    for (BerTlvContainer container : containers) {
      container.writeTo(out);
    }
  }

  private int calculateNumberOfLengthOctets(final int contentLength) {
    final int numberOfLengthOctets;
    if (contentLength < 0x80) {
      numberOfLengthOctets = 1;
    } else if (contentLength < 0x100) {
      numberOfLengthOctets = 2;
    } else if (contentLength < 0x10000) {
      numberOfLengthOctets = 3;
    } else if (contentLength < 0x1000000) {
      numberOfLengthOctets = 4;
    } else {
      numberOfLengthOctets = 5;
    }
    return numberOfLengthOctets;
  }

  private void writeTo(final BerBuffer buffer, final int offset) {
    buffer.checkLimit(offset + length);
    int index = offset;
    for (BerTlvContainer container : containers) {
      index = container.writeTo(buffer, index);
    }
  }
}
