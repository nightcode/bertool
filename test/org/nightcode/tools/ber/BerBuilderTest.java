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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

public class BerBuilderTest {

  private static byte[] get(ByteBuffer buffer, int offset, int length) {    
    buffer.limit(offset + length);
    buffer.position(offset);
    byte[] frame = new byte[length];
    buffer.get(frame);
    return frame;
  }

  @Test
  public void testAdd() throws IOException {
    final byte[] expected = hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add((byte) 0x5E,                                        hexToByteArray("01"));
    builder.add((byte) 0x5F, (byte) 0x2D,                           hexToByteArray("02"));
    builder.add((byte) 0x5F, (byte) 0xDF, (byte) 0x03,              hexToByteArray("03"));
    builder.add((byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, hexToByteArray("04"));
    builder.add(hexToByteArray("5F2D"),                             hexToByteArray("05"));

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder.writeTo(out);

    assertArrayEquals(expected, out.toByteArray());
  }

  @Test
  public void testAddInt() {
    final byte[] expected = hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x5E,       hexToByteArray("01"));
    builder.add(0x5F2D,     hexToByteArray("02"));
    builder.add(0x5FDF03,   hexToByteArray("03"));
    builder.add(0xDFDFDF04, hexToByteArray("04"));
    builder.add(0x5F2D,     hexToByteArray("05"));

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }
  
  @Test
  public void testAddLong() {
    final byte[] expected = hexToByteArray("5E01015F2D01025FDF030103DFDFDF04"
        + "01045F2D0105DFDFDFDF060106DFDFDFDFDF070107DFDFDFDFDFDF080108DFDFDFDFDFDFDF090109");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x5EL,               hexToByteArray("01"));
    builder.add(0x5F2DL,             hexToByteArray("02"));
    builder.add(0x5FDF03L,           hexToByteArray("03"));
    builder.add(0xDFDFDF04L,         hexToByteArray("04"));
    builder.add(0x5F2DL,             hexToByteArray("05"));
    builder.add(0xDFDFDFDF06L,       hexToByteArray("06"));
    builder.add(0xDFDFDFDFDF07L,     hexToByteArray("07"));
    builder.add(0xDFDFDFDFDFDF08L,   hexToByteArray("08"));
    builder.add(0xDFDFDFDFDFDFDF09L, hexToByteArray("09"));

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testAddBuilder() {
    final byte[] expected = hexToByteArray("7E030101FF7F2D030101FF7FDF030301"
        + "01FF7FDFDF04030101FF7F2D030101FF5FDFDFDF2D030101FF");

    BerBuilder inner = BerBuilder.newInstance();
    inner.addHexString((byte) 0x01, "FF");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add((byte) 0x7E,                                        inner);
    builder.add((byte) 0x7F, (byte) 0x2D,                           inner);
    builder.add((byte) 0x7F, (byte) 0xDF, (byte) 0x03,              inner);
    builder.add((byte) 0x7F, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, inner);
    builder.add(hexToByteArray("7F2D"),                             inner);
    builder.add(0x5FDFDFDF2DL,                                      inner);

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testAddAsciiString() {
    final byte[] expected = hexToByteArray("5E02656E5F2D02656E5FDF0302656E5FDFDF0402656E5F2D02656E5FDFDFDF2D02656E");

    BerBuilder builder = BerBuilder.newInstance();
    builder.addAsciiString((byte) 0x5E,                                        "en");
    builder.addAsciiString((byte) 0x5F, (byte) 0x2D,                           "en");
    builder.addAsciiString((byte) 0x5F, (byte) 0xDF, (byte) 0x03,              "en");
    builder.addAsciiString((byte) 0x5F, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, "en");
    builder.addAsciiString(hexToByteArray("5F2D"),                             "en");
    builder.addAsciiString(0x5FDFDFDF2DL,                                      "en");

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void testAddHexString() {
    final byte[] expected = hexToByteArray("5E01015F2D01025FDF0301035FDFDF0401045F2D01055FDFDFDF2D0106");

    BerBuilder builder = BerBuilder.newInstance();
    builder.addHexString((byte) 0x5E,                                        "01");
    builder.addHexString((byte) 0x5F, (byte) 0x2D,                           "02");
    builder.addHexString((byte) 0x5F, (byte) 0xDF, (byte) 0x03,              "03");
    builder.addHexString((byte) 0x5F, (byte) 0xDF, (byte) 0xDF, (byte) 0x04, "04");
    builder.addHexString(hexToByteArray("5F2D"),                             "05");
    builder.addHexString(0x5FDFDFDF2DL,                                      "06");

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));
  }

  @Test
  public void calculateNumberOfLengthOctets() throws IOException {
    java.util.Random random = new java.util.Random();
    
    final byte[] content1 = new byte[0x7F];
    final byte[] content2 = new byte[0xFF];
    final byte[] content3 = new byte[0xFFFF];
    final byte[] content4 = new byte[0xFFFFFF];
    final byte[] content5 = new byte[0x1000000];

    random.nextBytes(content1);
    random.nextBytes(content2);
    random.nextBytes(content3);
    random.nextBytes(content4);
    random.nextBytes(content5);

    ByteBuffer expected = ByteBuffer.allocate(1 + 1 + 0x7F + 1 + 2 + 0xFF + 1 + 3 + 0xFFFF
        + 1 + 4 + 0xFFFFFF + 1 + 5 + 0x1000000);

    expected.put(hexToByteArray("517F"));
    expected.put(content1);
    expected.put(hexToByteArray("5281FF"));
    expected.put(content2);
    expected.put(hexToByteArray("5382FFFF"));
    expected.put(content3);
    expected.put(hexToByteArray("5483FFFFFF"));
    expected.put(content4);
    expected.put(hexToByteArray("558401000000"));
    expected.put(content5);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x51, content1);
    builder.add(0x52, content2);
    builder.add(0x53, content3);
    builder.add(0x54, content4);
    builder.add(0x55, content5);
    
    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(get(expected, 0, expected.capacity()), get(buffer, 0, builder.length()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder.writeTo(out);

    assertArrayEquals(get(expected, 0, expected.capacity()), out.toByteArray());
  }

  @Test
  public void testAddBerFrame() {
    final byte[] expected = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");
    
    BerFrame berFrame = BerFrame.parseFrom(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"));

    BerBuilder builder6F = BerBuilder.newInstance();
    builder6F.add(berFrame);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x6F, builder6F);
    builder.add(0x9F36, new byte[] {0x00, 0x60});

    byte[] buffer = new byte[builder.length()];
    builder.writeTo(buffer);

    assertArrayEquals(expected, buffer);
  }

  @Test
  public void testAdd128Bytes() {
    java.util.Random random = new java.util.Random();

    final byte[] content128 = new byte[0x80];
    random.nextBytes(content128);

    ByteBuffer expected = ByteBuffer.allocate(3 + 2 + 0x80);
    expected.put(hexToByteArray("DFAE028180"));
    expected.put(content128);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0xDFAE02, content128);

    ByteBuffer buffer = ByteBuffer.allocate(builder.length());
    builder.writeTo(buffer);

    assertArrayEquals(get(expected, 0, expected.capacity()), get(buffer, 0, builder.length()));
  }

  @Test
  public void testEncodePrimitive() throws IOException {
    final byte[] expected = hexToByteArray("9F2608C2C12B098F3DA6E3");
    final byte[] content = hexToByteArray("C2C12B098F3DA6E3");

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x9F26, content);

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder.writeTo(out);

    assertArrayEquals(expected, out.toByteArray());
  }

  @Test
  public void testEncodeConstructed() throws IOException {
    final byte[] expected = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");

    BerBuilder builderA5 = BerBuilder.newInstance();
    builderA5.add(0x88, new byte[] {0x02});
    builderA5.addAsciiString(0x5F2D, "en");

    BerBuilder builder6F = BerBuilder.newInstance();
    builder6F.addHexString(0x84, "315041592E5359532E4444463031");
    builder6F.add(0xA5, builderA5);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x6F, builder6F);
    builder.add(0x9F36, new byte[] {0x00, 0x60});

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));


    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder.writeTo(out);

    assertArrayEquals(expected, out.toByteArray());
  }

  @Test
  public void testEncodeConstructedByteArray() throws IOException {
    final byte[] expected = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");

    BerBuilder builderA5 = BerBuilder.newInstance();
    builderA5.add(0x88, new byte[] {0x02});
    builderA5.addAsciiString(0x5F2D, "en");

    BerBuilder builder6F = BerBuilder.newInstance();
    builder6F.addHexString(0x84, "315041592E5359532E4444463031");
    builder6F.add(0xA5, builderA5);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x6F, builder6F);
    builder.add(0x9F36, new byte[] {0x00, 0x60});

    byte[] buffer = new byte[builder.length()];
    builder.writeTo(buffer);

    assertArrayEquals(expected, buffer);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder.writeTo(out);

    assertArrayEquals(expected, out.toByteArray());
  }

  @Test
  public void testEncodeConstructedWithOffset() throws IOException {
    final byte[] expected = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");

    BerBuilder builderA5 = BerBuilder.newInstance();
    builderA5.add(0x88, hexToByteArray("02"));
    builderA5.addAsciiString(0x5F2D, "en");

    BerBuilder builder6F = BerBuilder.newInstance();
    builder6F.addHexString(0x84, "315041592E5359532E4444463031");
    builder6F.add(0xA5, builderA5);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(0x6F, builder6F);
    builder.add(0x9F36, hexToByteArray("0060"));

    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(expected.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    builder.writeTo(buffer, offset);

    assertArrayEquals(expected, get(buffer, offset, builder.length()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder.writeTo(out);

    assertArrayEquals(expected, out.toByteArray());
  }

  @Test
  public void testEncodeDefiniteLongForm() throws IOException {
    final byte[] identifier = hexToByteArray("84");
    Random random = new Random();
    final byte[] content = new byte[435];
    random.nextBytes(content);

    final byte[] expected = new byte[content.length + 4];
    expected[0] = (byte) 0x84;
    expected[1] = (byte) 0x82;
    expected[2] = (byte) 0x01;
    expected[3] = (byte) 0xB3;
    System.arraycopy(content, 0, expected, 4, content.length);

    BerBuilder builder = BerBuilder.newInstance();
    builder.add(identifier, content);

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    builder.writeTo(buffer);

    assertArrayEquals(expected, get(buffer, 0, builder.length()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder.writeTo(out);

    assertArrayEquals(expected, out.toByteArray());
  }
}
