/*
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

public class BerParserTest {

  private static final int OFFSET = 10;

  @Test void testDecodePrimitive() {
    BerFrame berFrame = BerFrame.parseFrom(hexToByteArray("9F2608C2C12B098F3DA6E3"));

    assertArrayEquals(hexToByteArray("C2C12B098F3DA6E3"), berFrame.getContent(0x9F26));
  }

  @Test void testDecodeConstructed() {
    BerFrame berFrame
        = BerFrame.parseFrom(hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060"));

    assertArrayEquals(hexToByteArray("315041592E5359532E4444463031"), berFrame.getContent(0x84));
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getContent(0xA5));
    assertArrayEquals(hexToByteArray("02"), berFrame.getContent(0x88));
    assertArrayEquals(hexToByteArray("656E"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("0060"), berFrame.getContent(0x9F36));
  }

  @Test void testDecodeConstructedByteBuffer() {
    BerFrame berFrame = BerFrame.parseFrom(ByteBuffer.wrap(hexToByteArray(
        "6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060")));

    assertArrayEquals(hexToByteArray("315041592E5359532E4444463031"), berFrame.getContent(0x84));
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getContent(0xA5));
    assertArrayEquals(hexToByteArray("02"), berFrame.getContent(0x88));
    assertArrayEquals(hexToByteArray("656E"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("0060"), berFrame.getContent(0x9F36));
  }

  @Test void testDecodeConstructedWithOffset() {
    final byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);
    BerFrame berFrame = BerFrame.parseFrom(buffer, OFFSET, ber.length);

    assertArrayEquals(hexToByteArray("315041592E5359532E4444463031"), berFrame.getContent(0x84));
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getContent(0xA5));
    assertArrayEquals(hexToByteArray("02"), berFrame.getContent(0x88));
    assertArrayEquals(hexToByteArray("656E"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("0060"), berFrame.getContent(0x9F36));
  }

  @Test void testDecodeDefiniteLongForm() {
    java.util.Random random = new java.util.Random();
    final byte[] content = new byte[435];
    random.nextBytes(content);
    final byte[] frame = new byte[content.length + 4];
    frame[0] = (byte) 0x84;
    frame[1] = (byte) 0x82;
    frame[2] = (byte) 0x01;
    frame[3] = (byte) 0xB3;
    System.arraycopy(content, 0, frame, 4, content.length);

    BerFrame berFrame = BerFrame.parseFrom(frame);

    assertArrayEquals(content, berFrame.getContent(0x84));
  }

  @Test void testDecoderExceptionCase1() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    try {
      BerFrame.parseFrom(ber);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      assertArrayEquals(hexToByteArray("9f360200"), ex.getUndecoded());
      assertArrayEquals(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"), ex.getPartialBerFrame().getContent(0x6F));
    }
  }

  @Test void testDecoderExceptionCase2() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);
    try {
      BerFrame.parseFrom(buffer, OFFSET, ber.length);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      assertArrayEquals(hexToByteArray("9f360200"), ex.getUndecoded());
      assertArrayEquals(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"), ex.getPartialBerFrame().getContent(0x6F));
    }
  }

  @Test void testDecoderExceptionCase3() {
    byte[] ber = hexToByteArray("9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);
    try {
      BerFrame.parseFrom(buffer, OFFSET, ber.length);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      assertArrayEquals(hexToByteArray("9f360200"), ex.getUndecoded());
      assertFalse(ex.getPartialBerFrame().getIdentifiers().hasNext());
    }
  }

  @Test void testDecoderExceptionCase4() {
    byte[] ber = hexToByteArray("B78F9D69485B90134E653D0C9CAA283700F29EA478D3FEECC2919997C093705B");
    try {
      BerFrame.parseFrom(ber);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      assertArrayEquals(hexToByteArray("B78F9D69485B90134E653D0C9CAA283700F29EA478D3FEECC2919997C093705B"), ex.getUndecoded());
    }
  }

  @Test void shouldThrowExceptionForDecodeIndefiniteForm() {
    Throwable th = assertThrows(DecoderException.class, () -> BerFrame.parseFrom(hexToByteArray("8480010000")));
    assertInstanceOf(IllegalStateException.class, th.getCause());
    assertEquals("Indefinite form is not supported yet.", th.getCause().getMessage());
  }

  @Test void shouldThrowExceptionForIncorrectMessageLengthCase1() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);

    Throwable th = assertThrows(DecoderException.class, () -> BerFrame.parseFrom(buffer, OFFSET, ber.length));
    assertInstanceOf(IndexOutOfBoundsException.class, th.getCause());
    assertEquals("content bound is beyond content limit (b=43; l=42)", th.getCause().getMessage());
  }

  @Test void shouldThrowExceptionForIncorrectMessageLengthCase2() {
    Throwable th = assertThrows(DecoderException.class
        , () -> BerFrame.parseFrom(hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801022D02656E9f36020060")));
    assertInstanceOf(IndexOutOfBoundsException.class, th.getCause());
    assertEquals("content bound is beyond content limit (b=137; l=27)", th.getCause().getMessage());
  }

  @Test void shouldThrowExceptionForIncorrectMessageLengthCase3() {
    Throwable th = assertThrows(DecoderException.class, () -> BerFrame.parseFrom(BerUtil.hexToByteArray("9F")));
    assertInstanceOf(IndexOutOfBoundsException.class, th.getCause());
    assertEquals("index is beyond bound (i=1; b=0)", th.getCause().getMessage());
  }
}
