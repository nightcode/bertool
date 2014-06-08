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
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;

public class BerDecoderTest {

  @Test
  public void testDecodePrimitive() {
    BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(DatatypeConverter
        .parseHexBinary("9F2608C2C12B098F3DA6E3"));

    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("C2C12B098F3DA6E3")
        , berFrame.getContent((byte) 0x9F, (byte) 0x26));
  }

  @Test
  public void testDecodeConstructed() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(DatatypeConverter
        .parseHexBinary("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060"));

    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("315041592E5359532E4444463031")
        , berFrame.getContent((byte) 0x84));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("8801025F2D02656E")
        , berFrame.getContent((byte) 0xA5));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("02")
        , berFrame.getContent((byte) 0x88));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("656E")
        , berFrame.getContent((byte) 0x5F, (byte) 0x2D));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("0060")
        , berFrame.getContent((byte) 0x9F, (byte) 0x36));
  }

  @Test
  public void testDecodeConstructedWithOffset() {
    final byte[] ber = DatatypeConverter
        .parseHexBinary("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(ber);
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(buffer, offset, ber.length);

    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("315041592E5359532E4444463031")
        , berFrame.getContent((byte) 0x84));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("8801025F2D02656E")
        , berFrame.getContent((byte) 0xA5));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("02")
        , berFrame.getContent((byte) 0x88));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("656E")
        , berFrame.getContent((byte) 0x5F, (byte) 0x2D));
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("0060")
        , berFrame.getContent((byte) 0x9F, (byte) 0x36));
  }

  @Test
  public void testDecodeDefiniteLongForm() {
    Random random = new Random();
    final byte[] content = new byte[435];
    random.nextBytes(content);
    final byte[] frame = new byte[content.length + 4];
    frame[0] = (byte) 0x84;
    frame[1] = (byte) 0x82;
    frame[2] = (byte) 0x01;
    frame[3] = (byte) 0xB3;
    System.arraycopy(content, 0, frame, 4, content.length);

    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(frame);

    Assert.assertArrayEquals(content, berFrame.getContent((byte) 0x84));
  }

  @Test
  public void testDecodeIndefiniteForm() {
    final byte[] frame = new byte[5];
    frame[0] = (byte) 0x84;
    frame[1] = (byte) 0x80;
    frame[2] = (byte) 0x01;
    frame[3] = (byte) 0x00;
    frame[3] = (byte) 0x00;

    final BerDecoder berDecoder = new BerDecoder();
    try {
      berDecoder.decode(frame);
      Assert.fail("Indefinite form is not supported yet.");
    } catch (IllegalStateException ex) {
      Assert.assertEquals("Indefinite form is not supported yet.", ex.getMessage());
    }
  }

  @Test
  public void testWrongBerLength() {
    final BerDecoder berDecoder = new BerDecoder();
    final byte[] ber = DatatypeConverter
        .parseHexBinary("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + offset * 2);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(ber);
    try {
      berDecoder.decode(buffer, offset, ber.length);
      Assert.fail("Incorrect message length check.");
    } catch (IndexOutOfBoundsException ex) {
      Assert.assertEquals("content bound is beyond content limit (b=43; l=42)", ex.getMessage());
    }
    
    try {
      berDecoder.decode(DatatypeConverter
        .parseHexBinary("6F1A840E315041592E5359532E4444463031A5088801022D02656E9f36020060"));
      Assert.fail("Incorrect message length check.");
    } catch (IndexOutOfBoundsException ex) {
      Assert.assertEquals("content bound is beyond content limit (b=137; l=27)", ex.getMessage());
    }
    
    try {
      berDecoder.decode(DatatypeConverter.parseHexBinary("9f"));
      Assert.fail("Incorrect message length check.");
    } catch (IndexOutOfBoundsException ex) {
      Assert.assertEquals("index is beyond bound (i=1; b=0)", ex.getMessage());
    }
  }
}
