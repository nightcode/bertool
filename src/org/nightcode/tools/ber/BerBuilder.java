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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Collects BER tags which will be used to create ber packet.
 */
public class BerBuilder {

  private static final Charset ASCII = Charset.forName("US-ASCII");

  private static abstract class BerTlvContainer {

    void putLengthOctets(final BerBuffer buffer, final int offset, final int contentLength) {
      if (contentLength < 0x80) {
        buffer.putByte(offset, (byte) (contentLength & 0x7F));
      } else if (contentLength < 0x100) {
        buffer.putByte(offset, (byte) 0x81);
        buffer.putByte(offset + 1, (byte) (contentLength & 0xFF));
      } else if (contentLength < 0x10000) {
        buffer.putByte(offset, (byte) 0x82);
        buffer.putByte(offset + 1, (byte) ((contentLength >>> 8) & 0xFF));
        buffer.putByte(offset + 2, (byte) (contentLength & 0xFF));
      } else if (contentLength < 0x1000000) {
        buffer.putByte(offset, (byte) 0x83);
        buffer.putByte(offset + 1, (byte) ((contentLength >>> 16) & 0xFF));
        buffer.putByte(offset + 2, (byte) ((contentLength >>> 8) & 0xFF));
        buffer.putByte(offset + 3, (byte) (contentLength & 0xFF));
      } else if (contentLength <= 0xFFFFFFFF) {
        buffer.putByte(offset, (byte) 0x84);
        buffer.putByte(offset + 1, (byte) ((contentLength >>> 24) & 0xFF));
        buffer.putByte(offset + 2, (byte) ((contentLength >>> 16) & 0xFF));
        buffer.putByte(offset + 3, (byte) ((contentLength >>> 8) & 0xFF));
        buffer.putByte(offset + 4, (byte) (contentLength & 0xFF));
      } else {
        throw new IllegalStateException("should never been thrown");
      }
    }

    abstract int writeTo(BerBuffer buffer, int offset);
  }

  private static final class BerTlvContainerByteArray extends BerTlvContainer {
    private final byte[] identifier;
    private final int numberOfLengthOctets;
    private final byte[] content;

    private BerTlvContainerByteArray(final byte[] identifier, final int numberOfLengthOctets,
        final byte[] content) {
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
  }

  private static final  class BerTlvContainerBuilder extends BerTlvContainer {
    private final byte[] identifier;
    private final int numberOfLengthOctets;
    private final BerBuilder builder;

    private BerTlvContainerBuilder(final byte[] identifier, final int numberOfLengthOctets,
        final BerBuilder builder) {
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
  }

  private int length;
  private List<BerTlvContainer> containers;

  public BerBuilder() {
    containers = new ArrayList<>();
    length = 0;
  }

  public BerBuilder add(final byte identifier, final byte[] content) {
    return add(new byte[] {identifier}, content);
  }

  public BerBuilder add(final byte b1, final byte b2, final byte[] content) {
    return add(new byte[] {b1, b2}, content);
  }

  public BerBuilder add(final byte b1, final byte b2, final byte b3, final byte[] content) {
    return add(new byte[] {b1, b2, b3}, content);
  }

  public BerBuilder add(final byte b1, final byte b2, final byte b3, final byte b4,
      final byte[] content) {
    return add(new byte[] {b1, b2, b3, b4}, content);
  }

  public BerBuilder add(final byte[] identifier, final byte[] content) {
    final int numberOfLengthOctets = calculateNumberOfLengthOctets(content.length);
    BerTlvContainer container
        = new BerTlvContainerByteArray(identifier, numberOfLengthOctets, content);
    containers.add(container);
    length += (identifier.length + numberOfLengthOctets + content.length);
    return this;
  }

  public BerBuilder add(final byte identifier, final BerBuilder builder) {
    return add(new byte[] {identifier}, builder);
  }

  public BerBuilder add(final byte b1, final byte b2, final BerBuilder builder) {
    return add(new byte[] {b1, b2}, builder);
  }

  public BerBuilder add(final byte b1, final byte b2, final byte b3, final BerBuilder builder) {
    return add(new byte[] {b1, b2, b3}, builder);
  }

  public BerBuilder add(final byte b1, final byte b2, final byte b3, final byte b4,
      final BerBuilder builder) {
    return add(new byte[] {b1, b2, b3, b4}, builder);
  }

  public BerBuilder add(final byte[] identifier, final BerBuilder builder) {
    final int numberOfLengthOctets = calculateNumberOfLengthOctets(builder.length);
    BerTlvContainer container
        = new BerTlvContainerBuilder(identifier, numberOfLengthOctets, builder);
    containers.add(container);
    length += (identifier.length + numberOfLengthOctets + builder.length);
    return this;
  }

  public BerBuilder addAsciiString(final byte identifier, final String src) {
    return addAsciiString(new byte[] {identifier}, src);
  }

  public BerBuilder addAsciiString(final byte b1, final byte b2, final String src) {
    return addAsciiString(new byte[] {b1, b2}, src);
  }

  public BerBuilder addAsciiString(final byte b1, final byte b2, final byte b3, final String src) {
    return addAsciiString(new byte[] {b1, b2, b3}, src);
  }

  public BerBuilder addAsciiString(final byte b1, final byte b2, final byte b3, final byte b4,
      final String src) {
    return addAsciiString(new byte[] {b1, b2, b3, b4}, src);
  }

  public BerBuilder addAsciiString(final byte[] identifier, final String src) {
    return add(identifier, src.getBytes(ASCII));
  }

  public BerBuilder addHexString(final byte identifier, final String src) {
    return addHexString(new byte[] {identifier}, src);
  }

  public BerBuilder addHexString(final byte b1, final byte b2, final String src) {
    return addHexString(new byte[] {b1, b2}, src);
  }

  public BerBuilder addHexString(final byte b1, final byte b2, final byte b3, final String src) {
    return addHexString(new byte[] {b1, b2, b3}, src);
  }

  public BerBuilder addHexString(final byte b1, final byte b2, final byte b3, final byte b4,
      final String src) {
    return addHexString(new byte[] {b1, b2, b3, b4}, src);
  }

  public BerBuilder addHexString(final byte[] identifier, final String src) {
    return add(identifier, hexToByteArray(src));
  }

  public int length() {
    return length;
  }

  public void writeTo(final BerBuffer buffer, final int offset) {
    buffer.checkLimit(offset + length);
    int index = offset;
    for (BerTlvContainer container : containers) {
      index = container.writeTo(buffer, index);
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
    } else if (contentLength <= 0xFFFFFFFF) {
      numberOfLengthOctets = 5;
    } else {
      throw new IllegalStateException("should never been thrown");
    }
    return numberOfLengthOctets;
  }

  private byte[] hexToByteArray(String hex) {
    Objects.requireNonNull(hex, "hexadecimal string");
    final int length = hex.length();
    if ((length & 0x1) != 0) {
      throw new IllegalStateException(String
          .format("hexadecimal string <%s> must have an even number of characters.", hex));
    }
    byte[] result = new byte[length >> 1];
    for (int i = 0; i < length; i += 2) {
      int hn = Character.digit(hex.charAt(i), 16);
      int ln = Character.digit(hex.charAt(i + 1), 16);
      result[i >> 1] = (byte) ((hn << 4) | ln);
    }
    return result;
  }
}
