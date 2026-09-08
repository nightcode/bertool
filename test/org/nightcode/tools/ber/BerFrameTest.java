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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

public class BerFrameTest {

  private static final byte[] BER = hexToByteArray((
        "5A01305E 01315F2D 01325FDF 030133DF DFDF0401 345F2D01 35DFDFDF DF060136"
      + "DFDFDFDF DF070137 DFDFDFDF DFDF0801 38DFDFDF DFDFDFDF 090139"
  ).replace(" ", ""));

  private static final byte[] BER_WITH_DUP = hexToByteArray((
        "5A01305E 01315F2D 01325FDF 030133DF DFDF0401 345F2D01 35DFDFDF DF060136"
      + "DFDFDFDF DF070137 DFDFDFDF DFDF0801 38DFDFDF DFDFDFDF 090139DF DFDFDFDF"
      + "DFDF0901 41DFDFDF 0401366F 1A840E31 5041592E 5359532E 44444630 31A50888"
      + "01025F2D 02656E6F 1A840E31 5041592E 5359532E 44444630 31A50888 01025F2D"
      + "02656F5E 0137"
  ).replace(" ", ""));

  static Stream<byte[]> buffers() {
    return Stream.of(BER, BER_WITH_DUP);
  }

  @Test void testLimit() {
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(BER.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(BER);
    BerFrame berFrame = BerFrame.parseFrom(buffer, offset, BER.length);

    assertEquals(buffer.capacity(), berFrame.limit() + offset);
  }

  @Test void testOffset() {
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(BER.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(BER);
    BerFrame berFrame = BerFrame.parseFrom(buffer, offset, BER.length);

    assertEquals(0, berFrame.offset());
  }

  @Test void testGetContentEmpty() {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    byte[] result = berFrame.getContent();

    assertNull(result);
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentByByte(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertArrayEquals(hexToByteArray("30"), berFrame.getContent((byte) 0x5A));
    assertArrayEquals(hexToByteArray("31"), berFrame.getContent((byte) 0x5E));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentByInt(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertArrayEquals(hexToByteArray("30"), berFrame.getContent(0x5A));
    assertArrayEquals(hexToByteArray("31"), berFrame.getContent(0x5E));
    assertArrayEquals(hexToByteArray("32"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("33"), berFrame.getContent(0x5FDF03));
    assertArrayEquals(hexToByteArray("34"), berFrame.getContent(0xDFDFDF04));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentByLong(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertArrayEquals(hexToByteArray("30"), berFrame.getContent(0x5AL));
    assertArrayEquals(hexToByteArray("31"), berFrame.getContent(0x5EL));
    assertArrayEquals(hexToByteArray("32"), berFrame.getContent(0x5F2DL));
    assertArrayEquals(hexToByteArray("33"), berFrame.getContent(0x5FDF03L));
    assertArrayEquals(hexToByteArray("34"), berFrame.getContent(0xDFDFDF04L));
    assertArrayEquals(hexToByteArray("36"), berFrame.getContent(0xDFDFDFDF06L));
    assertArrayEquals(hexToByteArray("37"), berFrame.getContent(0xDFDFDFDFDF07L));
    assertArrayEquals(hexToByteArray("38"), berFrame.getContent(0xDFDFDFDFDFDF08L));
    assertArrayEquals(hexToByteArray("39"), berFrame.getContent(0xDFDFDFDFDFDFDF09L));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentByByteArray(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertArrayEquals(hexToByteArray("30"), berFrame.getContent(hexToByteArray("5A")));
    assertArrayEquals(hexToByteArray("31"), berFrame.getContent(hexToByteArray("5E")));
    assertArrayEquals(hexToByteArray("32"), berFrame.getContent(hexToByteArray("5F2D")));
    assertArrayEquals(hexToByteArray("33"), berFrame.getContent(hexToByteArray("5FDF03")));
    assertArrayEquals(hexToByteArray("34"), berFrame.getContent(hexToByteArray("DFDFDF04")));
    assertArrayEquals(hexToByteArray("36"), berFrame.getContent(hexToByteArray("DFDFDFDF06")));
    assertArrayEquals(hexToByteArray("37"), berFrame.getContent(hexToByteArray("DFDFDFDFDF07")));
    assertArrayEquals(hexToByteArray("38"), berFrame.getContent(hexToByteArray("DFDFDFDFDFDF08")));
    assertArrayEquals(hexToByteArray("39"), berFrame.getContent(hexToByteArray("DFDFDFDFDFDFDF09")));
  }

  @Test void testGetAllContentsByte() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP);
    final byte tag = 0x5E;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("31"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("37"), berFrame.getAllContents(tag).get(1));

    berFrame = BerFrame.parseFrom(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"));

    List<byte[]> contents = berFrame.getAllContents((byte) 0xA5);
    assertFalse(contents.isEmpty());
  }

  @Test void testGetAllContentsInt() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP);
    final int tag = 0xDFDFDF04;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("34"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("36"), berFrame.getAllContents(tag).get(1));
  }

  @Test void testGetAllContentsLong() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();
    final long tag = 0xDFDFDFDFDFDFDF09L;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("39"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("41"), berFrame.getAllContents(tag).get(1));
  }

  @Test void testGetAllContentsConstructed() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();
    final int tag = 0xA5;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("8801025F2D02656F"), berFrame.getAllContents(tag).get(1));
  }

  @Test void testGetAllContentsByteArray() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();
    final byte[] tag = hexToByteArray("DFDFDFDFDFDFDF09");

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("39"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("41"), berFrame.getAllContents(tag).get(1));
  }

  @Test void testGetAllContentsEmpty() {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    List<byte[]> result = berFrame.getAllContents();

    assertTrue(result.isEmpty());
  }

  @Test void testGetIdentifiers() {
    BerFrame berFrame = BerFrame.parseFrom(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"));

    Iterator<byte[]> i = berFrame.getIdentifiers();
    assertTrue(i.hasNext());
    assertArrayEquals(new byte[] {(byte) 0x84}, i.next());
    assertTrue(i.hasNext());
    assertArrayEquals(new byte[] {(byte) 0xA5}, i.next());
    assertFalse(i.hasNext());
    assertThrows(NoSuchElementException.class, i::next);
  }

  @Test void testGetContentAsAsciiStringEmpty() {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    String result = berFrame.getContentAsAsciiString();

    assertNull(result);
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsAsciiStringByByte(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("0", berFrame.getContentAsAsciiString((byte) 0x5A));
    assertEquals("1", berFrame.getContentAsAsciiString((byte) 0x5E));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsAsciiStringByInt(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("0", berFrame.getContentAsAsciiString(0x5A));
    assertEquals("1", berFrame.getContentAsAsciiString(0x5E));
    assertEquals("2", berFrame.getContentAsAsciiString(0x5F2D));
    assertEquals("3", berFrame.getContentAsAsciiString(0x5FDF03));
    assertEquals("4", berFrame.getContentAsAsciiString(0xDFDFDF04));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsAsciiStringByLong(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("0", berFrame.getContentAsAsciiString(0x5AL));
    assertEquals("1", berFrame.getContentAsAsciiString(0x5EL));
    assertEquals("2", berFrame.getContentAsAsciiString(0x5F2DL));
    assertEquals("3", berFrame.getContentAsAsciiString(0x5FDF03L));
    assertEquals("4", berFrame.getContentAsAsciiString(0xDFDFDF04L));
    assertEquals("6", berFrame.getContentAsAsciiString(0xDFDFDFDF06L));
    assertEquals("7", berFrame.getContentAsAsciiString(0xDFDFDFDFDF07L));
    assertEquals("8", berFrame.getContentAsAsciiString(0xDFDFDFDFDFDF08L));
    assertEquals("9", berFrame.getContentAsAsciiString(0xDFDFDFDFDFDFDF09L));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsAsciiStringByByteArray(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("0", berFrame.getContentAsAsciiString(hexToByteArray("5A")));
    assertEquals("1", berFrame.getContentAsAsciiString(hexToByteArray("5E")));
    assertEquals("2", berFrame.getContentAsAsciiString(hexToByteArray("5F2D")));
    assertEquals("3", berFrame.getContentAsAsciiString(hexToByteArray("5FDF03")));
    assertEquals("4", berFrame.getContentAsAsciiString(hexToByteArray("DFDFDF04")));
    assertEquals("6", berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDF06")));
    assertEquals("7", berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDFDF07")));
    assertEquals("8", berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDFDFDF08")));
    assertEquals("9", berFrame.getContentAsAsciiString(hexToByteArray("DFDFDFDFDFDFDF09")));
  }

  @Test void testGetContentAsHexStringEmpty() {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    String result = berFrame.getContentAsHexString();

    assertNull(result);
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsHexStringByByte(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("30", berFrame.getContentAsHexString((byte) 0x5A));
    assertEquals("31", berFrame.getContentAsHexString((byte) 0x5E));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsHexStringByInt(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("30", berFrame.getContentAsHexString(0x5A));
    assertEquals("31", berFrame.getContentAsHexString(0x5E));
    assertEquals("32", berFrame.getContentAsHexString(0x5F2D));
    assertEquals("33", berFrame.getContentAsHexString(0x5FDF03));
    assertEquals("34", berFrame.getContentAsHexString(0xDFDFDF04));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsHexStringByLong(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("30", berFrame.getContentAsHexString(0x5AL));
    assertEquals("31", berFrame.getContentAsHexString(0x5EL));
    assertEquals("32", berFrame.getContentAsHexString(0x5F2DL));
    assertEquals("33", berFrame.getContentAsHexString(0x5FDF03L));
    assertEquals("34", berFrame.getContentAsHexString(0xDFDFDF04L));
    assertEquals("36", berFrame.getContentAsHexString(0xDFDFDFDF06L));
    assertEquals("37", berFrame.getContentAsHexString(0xDFDFDFDFDF07L));
    assertEquals("38", berFrame.getContentAsHexString(0xDFDFDFDFDFDF08L));
    assertEquals("39", berFrame.getContentAsHexString(0xDFDFDFDFDFDFDF09L));
  }

  @ParameterizedTest @MethodSource("buffers")
  void shouldGetContentAsHexStringByByteArray(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer);

    assertEquals("30", berFrame.getContentAsHexString(hexToByteArray("5A")));
    assertEquals("31", berFrame.getContentAsHexString(hexToByteArray("5E")));
    assertEquals("32", berFrame.getContentAsHexString(hexToByteArray("5F2D")));
    assertEquals("33", berFrame.getContentAsHexString(hexToByteArray("5FDF03")));
    assertEquals("34", berFrame.getContentAsHexString(hexToByteArray("DFDFDF04")));
    assertEquals("36", berFrame.getContentAsHexString(hexToByteArray("DFDFDFDF06")));
    assertEquals("37", berFrame.getContentAsHexString(hexToByteArray("DFDFDFDFDF07")));
    assertEquals("38", berFrame.getContentAsHexString(hexToByteArray("DFDFDFDFDFDF08")));
    assertEquals("39", berFrame.getContentAsHexString(hexToByteArray("DFDFDFDFDFDFDF09")));
  }

  @Test void testGetTag() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();

    BerFrame tag7F = berFrame.getTag(0x7F);
    assertNull(tag7F);

    BerFrame tag6F = berFrame.getTag(0x6F);
    assertNotNull(tag6F);

    BerFrame tagA5 = berFrame.getTag(0xA5);
    assertNotNull(tagA5);
    
    BerFrame tag5A = berFrame.getTag(0x5A);
    assertNotNull(tag5A);
  }

  @Test void testGetTagByByte() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();

    BerFrame tag6F = berFrame.getTag((byte) 0x6F);
    assertTag6F(tag6F);
  }

  @Test void testGetTagByInt() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();

    BerFrame tag6F = berFrame.getTag(0x6F);
    assertTag6F(tag6F);
  }

  @Test void testGetTagByLong() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();

    BerFrame tag6F = berFrame.getTag(0x6FL);
    assertTag6F(tag6F);
  }

  @Test
  public void testGetTagByByteArray() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();

    BerFrame tag6F = berFrame.getTag(new byte[] {0x6F});
    assertTag6F(tag6F);
  }

  @ParameterizedTest @MethodSource("buffers")
  void testToByteArray(final byte[] buffer) {
    BerFrame berFrame = BerFrame.parseFrom(buffer).deepSearch();

    assertArrayEquals(buffer, berFrame.toByteArray());
    assertArrayEquals(hexToByteArray("5A0130"),     berFrame.getTag(0x5A).toByteArray());
    assertArrayEquals(hexToByteArray("5E0131"),     berFrame.getTag(0x5E).toByteArray());
    assertArrayEquals(hexToByteArray("5FDF030133"), berFrame.getTag(0x5FDF03).toByteArray());
  }

  @Test void testGetTagAsByteArray() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();

    byte[] tag6F;
    
    tag6F = berFrame.getTagAsByteArray((byte) 0x6F);
    assertTag6F(BerFrame.parseFrom(tag6F).deepSearch());
    
    tag6F = berFrame.getTagAsByteArray(0x6F);
    assertTag6F(BerFrame.parseFrom(tag6F).deepSearch());

    tag6F = berFrame.getTagAsByteArray(0x6FL);
    assertTag6F(BerFrame.parseFrom(tag6F).deepSearch());

    tag6F = berFrame.getTagAsByteArray(new byte[] {0x6F});
    assertTag6F(BerFrame.parseFrom(tag6F).deepSearch());
  }

  @Test void testGetTagAsByteArrayNullCheck() {
    BerFrame berFrame = BerFrame.parseFrom(BER_WITH_DUP).deepSearch();

    byte[] tag6F;

    tag6F = berFrame.getTagAsByteArray((byte) 0x7F);
    assertNull(tag6F);

    tag6F = berFrame.getTagAsByteArray(0x7F);
    assertNull(tag6F);

    tag6F = berFrame.getTagAsByteArray(0x7FL);
    assertNull(tag6F);

    tag6F = berFrame.getTagAsByteArray(new byte[] {0x7F});
    assertNull(tag6F);
  }

  private void assertTag6F(BerFrame tag6F) {
    assertNotNull(tag6F);
    assertNotNull(tag6F.getContentAsHexString(hexToByteArray("6F")));
    assertNull(tag6F.getContentAsHexString(hexToByteArray("5E")));
    assertEquals("8801025F2D02656E", tag6F.getContentAsHexString(hexToByteArray("A5")));
    assertEquals("656E", tag6F.getContentAsHexString(hexToByteArray("5F2D")));
  }
}
