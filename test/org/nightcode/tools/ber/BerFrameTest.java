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
import java.util.List;

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

@RunWith(Theories.class)
public class BerFrameTest {

  @DataPoint
  public static final byte[] BER = hexToByteArray((
        "5A01005E 01015F2D 01025FDF 030103DF DFDF0401 045F2D01 05DFDFDF DF060106"
      + "DFDFDFDF DF070107 DFDFDFDF DFDF0801 08DFDFDF DFDFDFDF 090109"
  ).replaceAll(" ", ""));

  @DataPoint
  public static final byte[] BER_WITH_DUP = hexToByteArray((
        "5A01005E 01015F2D 01025FDF 030103DF DFDF0401 045F2D01 05DFDFDF DF060106"
      + "DFDFDFDF DF070107 DFDFDFDF DFDF0801 08DFDFDF DFDFDFDF 090109DF DFDFDFDF"
      + "DFDF0901 11DFDFDF 0401066F 1A840E31 5041592E 5359532E 44444630 31A50888"
      + "01025F2D 02656E6F 1A840E31 5041592E 5359532E 44444630 31A50888 01025F2D"
      + "02656F5E 0107"
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

    assertThat(berFrame.getContent((byte) 0x5A), is(hexToByteArray("00")));
    assertThat(berFrame.getContent((byte) 0x5E), is(hexToByteArray("01")));
  }

  @Theory
  public void shouldGetContentByInt(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContent(0x5A),       is(hexToByteArray("00")));
    assertThat(berFrame.getContent(0x5E),       is(hexToByteArray("01")));
    assertThat(berFrame.getContent(0x5F2D),     is(hexToByteArray("02")));
    assertThat(berFrame.getContent(0x5FDF03),   is(hexToByteArray("03")));
    assertThat(berFrame.getContent(0xDFDFDF04), is(hexToByteArray("04")));
  }

  @Theory
  public void shouldGetContentByLong(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContent(0x5AL),               is(hexToByteArray("00")));
    assertThat(berFrame.getContent(0x5EL),               is(hexToByteArray("01")));
    assertThat(berFrame.getContent(0x5F2DL),             is(hexToByteArray("02")));
    assertThat(berFrame.getContent(0x5FDF03L),           is(hexToByteArray("03")));
    assertThat(berFrame.getContent(0xDFDFDF04L),         is(hexToByteArray("04")));
    assertThat(berFrame.getContent(0xDFDFDFDF06L),       is(hexToByteArray("06")));
    assertThat(berFrame.getContent(0xDFDFDFDFDF07L),     is(hexToByteArray("07")));
    assertThat(berFrame.getContent(0xDFDFDFDFDFDF08L),   is(hexToByteArray("08")));
    assertThat(berFrame.getContent(0xDFDFDFDFDFDFDF09L), is(hexToByteArray("09")));
  }

  @Theory
  public void shouldGetContentByByteArray(final byte[] buffer) {
    BerFrame berFrame = berDecoder.decode(buffer);

    assertThat(berFrame.getContent(hexToByteArray("5A")),               is(hexToByteArray("00")));
    assertThat(berFrame.getContent(hexToByteArray("5E")),               is(hexToByteArray("01")));
    assertThat(berFrame.getContent(hexToByteArray("5F2D")),             is(hexToByteArray("02")));
    assertThat(berFrame.getContent(hexToByteArray("5FDF03")),           is(hexToByteArray("03")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDF04")),         is(hexToByteArray("04")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDF06")),       is(hexToByteArray("06")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDFDF07")),     is(hexToByteArray("07")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDFDFDF08")),   is(hexToByteArray("08")));
    assertThat(berFrame.getContent(hexToByteArray("DFDFDFDFDFDFDF09")), is(hexToByteArray("09")));
  }

  @Test
  public void testGetAllContentsByte() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final byte tag = 0x5E;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("01"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("07"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsInt() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final int tag = 0xDFDFDF04;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("04"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("06"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsLong() {
    BerFrame berFrame = berDecoder.decode(BER_WITH_DUP);
    final long tag = 0xDFDFDFDFDFDFDF09L;

    assertEquals(2, berFrame.getAllContents(tag).size());
    assertArrayEquals(hexToByteArray("09"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("11"), berFrame.getAllContents(tag).get(1));
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
    assertArrayEquals(hexToByteArray("09"), berFrame.getAllContents(tag).get(0));
    assertArrayEquals(hexToByteArray("11"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsEmpty() {
    BerFrame berFrame = berDecoder.decode(BER);
    List<byte[]> result = berFrame.getAllContents();

    assertTrue(result.isEmpty());
  }
}
