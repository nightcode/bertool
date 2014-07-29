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

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;

public class BerBuilderTest {

  private static byte[] get(ByteBuffer buffer, int offset, int length) {    
    buffer.limit(offset + length);
    buffer.position(offset);
    byte[] frame = new byte[length];
    buffer.get(frame);
    return frame;
  }

  @Test
  public void testAdd() {
    final byte[] expected = DatatypeConverter
        .parseHexBinary("5E01015F2D01025FDF030103EFDFDF0401045F2D0105");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add((byte) 0x5E,                                        new byte[] {(byte) 0x01});
    builder.add((byte) 0x5F, (byte) 0x2D,                           new byte[] {(byte) 0x02});
    builder.add((byte) 0x5F, (byte) 0xDF, (byte) 0x03,              new byte[] {(byte) 0x03});
    builder.add((byte) 0xEF, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, new byte[] {(byte) 0x04});
    builder.add(new byte[] {(byte) 0x5F, (byte) 0x2D},              new byte[] {(byte) 0x05});

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    Assert.assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testAddInt() {
    final byte[] expected = DatatypeConverter
        .parseHexBinary("5E01015F2D01025FDF030103EFDFDF0401045F2D0105");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x5E,       new byte[] {(byte) 0x01});
    builder.add(0x5F2D,     new byte[] {(byte) 0x02});
    builder.add(0x5FDF03,   new byte[] {(byte) 0x03});
    builder.add(0xEFDFDF04, new byte[] {(byte) 0x04});
    builder.add(0x5F2D,     new byte[] {(byte) 0x05});

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    Assert.assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }
  
  @Test
  public void testAddLong() {
    final byte[] expected = DatatypeConverter
        .parseHexBinary("5E01015F2D01025FDF030103EFDFDF0401045F2D0105EFDFDFDF060106EFDFDFDFDF070107" 
            + "EFDFDFDFDFDF080108EFDFDFDFDFDFDF090109");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x5EL,               new byte[] {(byte) 0x01});
    builder.add(0x5F2DL,             new byte[] {(byte) 0x02});
    builder.add(0x5FDF03L,           new byte[] {(byte) 0x03});
    builder.add(0xEFDFDF04L,         new byte[] {(byte) 0x04});
    builder.add(0x5F2DL,             new byte[] {(byte) 0x05});
    builder.add(0xEFDFDFDF06L,       new byte[] {(byte) 0x06});
    builder.add(0xEFDFDFDFDF07L,     new byte[] {(byte) 0x07});
    builder.add(0xEFDFDFDFDFDF08L,   new byte[] {(byte) 0x08});
    builder.add(0xEFDFDFDFDFDFDF09L, new byte[] {(byte) 0x09});

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    Assert.assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testAddBuilder() {
    final byte[] expected = DatatypeConverter
        .parseHexBinary("7E030101FF7F2D030101FF7FDF03030101FF7FDFDF04030101FF7F2D030101FF");

    BerBuilder inner = BerBuilder.newInstance();
    inner.addHexString((byte) 0x01, "FF");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add((byte) 0x7E,                                        inner);
    builder.add((byte) 0x7F, (byte) 0x2D,                           inner);
    builder.add((byte) 0x7F, (byte) 0xDF, (byte) 0x03,              inner);
    builder.add((byte) 0x7F, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, inner);
    builder.add(new byte[] {(byte) 0x7F, (byte) 0x2D},              inner);

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    Assert.assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testAddAsciiString() {
    final byte[] expected = DatatypeConverter
        .parseHexBinary("5E02656E5F2D02656E5FDF0302656E5FDFDF0402656E5F2D02656E");

    BerBuilder builder = BerBuilder.newInstance();
    builder.addAsciiString((byte) 0x5E,                                        "en");
    builder.addAsciiString((byte) 0x5F, (byte) 0x2D,                           "en");
    builder.addAsciiString((byte) 0x5F, (byte) 0xDF, (byte) 0x03,              "en");
    builder.addAsciiString((byte) 0x5F, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, "en");
    builder.addAsciiString(new byte[] {(byte) 0x5F, (byte) 0x2D},              "en");

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    Assert.assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testAddHexString() {
    final byte[] expected = DatatypeConverter
        .parseHexBinary("5E01015F2D01025FDF0301035FDFDF0401045F2D0105");

    BerBuilder builder = BerBuilder.newInstance();
    builder.addHexString((byte) 0x5E,                                        "01");
    builder.addHexString((byte) 0x5F, (byte) 0x2D,                           "02");
    builder.addHexString((byte) 0x5F, (byte) 0xDF, (byte) 0x03,              "03");
    builder.addHexString((byte) 0x5F, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, "04");
    builder.addHexString(new byte[] {(byte) 0x5F, (byte) 0x2D},              "05");

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    BerEncoder berEncoder = new BerEncoder();
    berEncoder.encode(builder, buffer);

    Assert.assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }
}
