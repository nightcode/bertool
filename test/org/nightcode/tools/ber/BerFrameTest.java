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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

@RunWith(Theories.class)
public class BerFrameTest {

  @DataPoint
  public static final byte[] BER = hexToByteArray((
        "5A01305E 01315F2D 01325FDF 030133DF DFDF0401 345F2D01 35DFDFDF DF060136"
      + "DFDFDFDF DF070137 DFDFDFDF DFDF0801 38DFDFDF DFDFDFDF 090139"
  ).replaceAll(" ", ""));

  @DataPoint
  public static final byte[] BER_WITH_DUP = hexToByteArray((
        "5A01305E 01315F2D 01325FDF 030133DF DFDF0401 345F2D01 35DFDFDF DF060136"
      + "DFDFDFDF DF070137 DFDFDFDF DFDF0801 38DFDFDF DFDFDFDF 090139DF DFDFDFDF"
      + "DFDF0901 41DFDFDF 0401366F 1A840E31 5041592E 5359532E 44444630 31A50888"
      + "01025F2D 02656E6F 1A840E31 5041592E 5359532E 44444630 31A50888 01025F2D"
      + "02656F5E 0137"
  ).replaceAll(" ", ""));

  private final BerDecoder berDecoder = new BerDecoder();

  @Test
  public void testLimit() {
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(BER.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(BER);
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(buffer, offset, BER.length);

    assertEquals(buffer.capacity(), berFrame.limit());
  }

  @Test
  public void testOffset() {
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(BER.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(BER);
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(buffer, offset, BER.length);

    assertEquals(offset, berFrame.offset());
  }

  @Test
  public void testGetContentEmpty() {
    BerFrame berFrame = berDecoder.decode(BER);
    byte[] result = berFrame.getContent();

    assertNull(result);
  }

  @Theory
  public void shouldGetContentByByte(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContent((byte) 0x5A), is(hexToByteArray("30")));
    assertThat(berFrame.getContent((byte) 0x5E), is(hexToByteArray("31")));
  }

  @Theory
  public void shouldGetContentByInt(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContent(0x5A),       is(hexToByteArray("30")));
    assertThat(berFrame.getContent(0x5E),       is(hexToByteArray("31")));
    assertThat(berFrame.getContent(0x5F2D),     is(hexToByteArray("32")));
    assertThat(berFrame.getContent(0x5FDF03),   is(hexToByteArray("33")));
    assertThat(berFrame.getContent(0xDFDFDF04), is(hexToByteArray("34")));
  }

  @Theory
  public void shouldGetContentByLong(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContent(0x5AL),               is(hexToByteArray("30")));
    assertThat(berFrame.getContent(0x5EL),               is(hexToByteArray("31")));
    assertThat(berFrame.getContent(0x5F2DL),             is(hexToByteArray("32")));
    assertThat(berFrame.getContent(0x5FDF03L),           is(hexToByteArray("33")));
    assertThat(berFrame.getContent(0xDFDFDF04L),         is(hexToByteArray("34")));
    assertThat(berFrame.getContent(0xDFDFDFDF06L),       is(hexToByteArray("36")));
    assertThat(berFrame.getContent(0xDFDFDFDFDF07L),     is(hexToByteArray("37")));
    assertThat(berFrame.getContent(0xDFDFDFDFDFDF08L),   is(hexToByteArray("38")));
    assertThat(berFrame.getContent(0xDFDFDFDFDFDFDF09L), is(hexToByteArray("39")));
  }

  @Theory
  public void shouldGetContentByByteArray(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContent(hexToByteArray("5A")),               is(hexToByteArray("30")));
    assertThat(berFrame.getContent(hexToByteArray("5E")),               is(hexToByteArray("31")));
    assertThat(berFrame.getContent(hexToByteArray("5F2D")),             is(hexToByteArray("32")));
    assertThat(berFrame.getContent(hexToByteArray("5FDF03")),           is(hexToByteArray("33")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDF04")),         is(hexToByteArray("34")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDF06")),       is(hexToByteArray("36")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDFDF07")),     is(hexToByteArray("37")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDFDFDF08")),   is(hexToByteArray("38")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDFDFDFDF09")), is(hexToByteArray("39")));
  }

  @Test
  public void testGetAllContentsByte() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final byte tag = 0x5E;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("31"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("37"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsInt() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final int tag = 0xDFDFDF04;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("34"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("36"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsLong() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final long tag = 0xDFDFDFDFDFDFDF09L;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("39"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("41"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsConstructed() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final int tag = 0xA5;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("8801025F2D02656F"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsByteArray() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final byte[] tag = hexToByteArray("DFDFDFDFDFDFDF09");

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("39"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("41"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsEmpty() {
    BerFrame berFrame = berDecoder.decode(BER);
    List<byte[]> result = berFrame.getAllContents();

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetIdentifiers() {
    BerFrame berFrame
        = berDecoder.decode(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"));

    Iterator<byte[]> i = berFrame.getIdentifiers();
    assertTrue(i.hasNext());
    assertArrayEquals(new byte[] {(byte) 0x84}, i.next());
    assertTrue(i.hasNext());
    assertArrayEquals(new byte[] {(byte) 0xA5}, i.next());
    assertFalse(i.hasNext());
    try {
      i.next();
      fail("should throw NoSuchElementException");
    } catch (NoSuchElementException ex) {
      // do nothing
    }
  }

  @Test
  public void testGetContentAsAsciiStringEmpty() {
    BerFrame berFrame = berDecoder.decode(BER);
    String result = berFrame.getContentAsAsciiString();

    assertNull(result);
  }

  @Theory
  public void shouldGetContentAsAsciiStringByByte(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsAsciiString((byte) 0x5A), is("0"));
    assertThat(berFrame.getContentAsAsciiString((byte) 0x5E), is("1"));
  }

  @Theory
  public void shouldGetContentAsAsciiStringByInt(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsAsciiString(0x5A),       is("0"));
    assertThat(berFrame.getContentAsAsciiString(0x5E),       is("1"));
    assertThat(berFrame.getContentAsAsciiString(0x5F2D),     is("2"));
    assertThat(berFrame.getContentAsAsciiString(0x5FDF03),   is("3"));
    assertThat(berFrame.getContentAsAsciiString(0xDFDFDF04), is("4"));
  }

  @Theory
  public void shouldGetContentAsAsciiStringByLong(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsAsciiString(0x5AL),               is("0"));
    assertThat(berFrame.getContentAsAsciiString(0x5EL),               is("1"));
    assertThat(berFrame.getContentAsAsciiString(0x5F2DL),             is("2"));
    assertThat(berFrame.getContentAsAsciiString(0x5FDF03L),           is("3"));
    assertThat(berFrame.getContentAsAsciiString(0xDFDFDF04L),         is("4"));
    assertThat(berFrame.getContentAsAsciiString(0xDFDFDFDF06L),       is("6"));
    assertThat(berFrame.getContentAsAsciiString(0xDFDFDFDFDF07L),     is("7"));
    assertThat(berFrame.getContentAsAsciiString(0xDFDFDFDFDFDF08L),   is("8"));
    assertThat(berFrame.getContentAsAsciiString(0xDFDFDFDFDFDFDF09L), is("9"));
  }

  @Theory
  public void shouldGetContentAsAsciiStringByByteArray(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("5A")),               is("0"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("5E")),               is("1"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("5F2D")),             is("2"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("5FDF03")),           is("3"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("DFDFDF04")),         is("4"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDF06")),       is("6"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDFDF07")),     is("7"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDFDFDF08")),   is("8"));
    assertThat(berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDFDFDFDF09")), is("9"));
  }

  @Test
  public void testGetContentAsHexStringEmpty() {
    BerFrame berFrame = berDecoder.decode(BER);
    String result = berFrame.getContentAsHexString();

    assertNull(result);
  }

  @Theory
  public void shouldGetContentAsHexStringByByte(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsHexString((byte) 0x5A), is("30"));
    assertThat(berFrame.getContentAsHexString((byte) 0x5E), is("31"));
  }

  @Theory
  public void shouldGetContentAsHexStringByInt(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsHexString(0x5A),       is("30"));
    assertThat(berFrame.getContentAsHexString(0x5E),       is("31"));
    assertThat(berFrame.getContentAsHexString(0x5F2D),     is("32"));
    assertThat(berFrame.getContentAsHexString(0x5FDF03),   is("33"));
    assertThat(berFrame.getContentAsHexString(0xDFDFDF04), is("34"));
  }

  @Theory
  public void shouldGetContentAsHexStringByLong(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsHexString(0x5AL),               is("30"));
    assertThat(berFrame.getContentAsHexString(0x5EL),               is("31"));
    assertThat(berFrame.getContentAsHexString(0x5F2DL),             is("32"));
    assertThat(berFrame.getContentAsHexString(0x5FDF03L),           is("33"));
    assertThat(berFrame.getContentAsHexString(0xDFDFDF04L),         is("34"));
    assertThat(berFrame.getContentAsHexString(0xDFDFDFDF06L),       is("36"));
    assertThat(berFrame.getContentAsHexString(0xDFDFDFDFDF07L),     is("37"));
    assertThat(berFrame.getContentAsHexString(0xDFDFDFDFDFDF08L),   is("38"));
    assertThat(berFrame.getContentAsHexString(0xDFDFDFDFDFDFDF09L), is("39"));
  }

  @Theory
  public void shouldGetContentAsHexStringByByteArray(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContentAsHexString(hexToByteArray("5A")),               is("30"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("5E")),               is("31"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("5F2D")),             is("32"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("5FDF03")),           is("33"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("DFDFDF04")),         is("34"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("DFDFDFDF06")),       is("36"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("DFDFDFDFDF07")),     is("37"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("DFDFDFDFDFDF08")),   is("38"));
    assertThat(berFrame.getContentAsHexString(hexToByteArray("DFDFDFDFDFDFDF09")), is("39"));
  }

  @Test
  public void testGetTag() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);

    BerFrame tag7F = berFrame.getTag(0x7F);
    assertNull(tag7F);

    BerFrame tag6F = berFrame.getTag(0x6F);
    assertNotNull(tag6F);

    BerFrame tagA5 = berFrame.getTag(0xA5);
    assertNotNull(tagA5);
    
    BerFrame tag5A = berFrame.getTag(0x5A);
    assertNotNull(tag5A);
  }

  @Test
  public void testGetTagByByte() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);

    BerFrame tag6F = berFrame.getTag((byte) 0x6F);
    assertTag6F(tag6F);
  }

  @Test
  public void testGetTagByInt() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);

    BerFrame tag6F = berFrame.getTag(0x6F);
    assertTag6F(tag6F);
  }

  @Test
  public void testGetTagByLong() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);

    BerFrame tag6F = berFrame.getTag(0x6FL);
    assertTag6F(tag6F);
  }

  @Test
  public void testGetTagByByteArray() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);

    BerFrame tag6F = berFrame.getTag(new byte[] {0x6F});
    assertTag6F(tag6F);
  }

  @Theory
  public void testToByteArray(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.toByteArray(), is(buffer));
    assertThat(berFrame.getTag(0x5A).toByteArray(),     is(hexToByteArray("5A0130")));
    assertThat(berFrame.getTag(0x5E).toByteArray(),     is(hexToByteArray("5E0131")));
    assertThat(berFrame.getTag(0x5FDF03).toByteArray(), is(hexToByteArray("5FDF030133")));
  }

  @Test
  public void testGetTagAsByteArray() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);

    byte[] tag6F;
    
    tag6F = berFrame.getTagAsByteArray((byte) 0x6F);
    assertTag6F(berDecoder.decode(tag6F));
    
    tag6F = berFrame.getTagAsByteArray(0x6F);
    assertTag6F(berDecoder.decode(tag6F));

    tag6F = berFrame.getTagAsByteArray(0x6FL);
    assertTag6F(berDecoder.decode(tag6F));

    tag6F = berFrame.getTagAsByteArray(new byte[] {0x6F});
    assertTag6F(berDecoder.decode(tag6F));
  } 

  private void assertTag6F(BerFrame tag6F) {
    assertNotNull(tag6F);
    assertNotNull(tag6F.getContentAsHexString(hexToByteArray("6F")));
    assertNull(tag6F.getContentAsHexString(hexToByteArray("5E")));
    assertThat(tag6F.getContentAsHexString(hexToByteArray("A5")), is("8801025F2D02656E"));
    assertThat(tag6F.getContentAsHexString(hexToByteArray("5F2D")), is("656E"));
  }
}
