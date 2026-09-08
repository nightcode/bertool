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
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

public class BerParserTest {

  private static final int OFFSET = 10;

  @Test void testDecodePrimitive() {
    BerFrame berFrame = BerFrame.parseFrom(hexToByteArray("9F2608C2C12B098F3DA6E3"));

    assertArrayEquals(hexToByteArray("C2C12B098F3DA6E3"), berFrame.getContent(0x9F26));
  }

  @Test void testDecodeConstructed() {
    BerFrame berFrame = BerFrame.parseFrom(hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060")).deepSearch();

    assertArrayEquals(hexToByteArray("315041592E5359532E4444463031"), berFrame.getContent(0x84));
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getContent(0xA5));
    assertArrayEquals(hexToByteArray("02"), berFrame.getContent(0x88));
    assertArrayEquals(hexToByteArray("656E"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("0060"), berFrame.getContent(0x9F36));
  }

  @Test void testDecodeConstructedByteBuffer() {
    BerFrame berFrame = BerFrame.parseFrom(ByteBuffer.wrap(hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060"))).deepSearch();

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
    BerFrame berFrame = BerFrame.parseFrom(buffer, OFFSET, ber.length).deepSearch();

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

  @Test void testUndecodedCase1() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    BerFrame frame = BerFrame.parseFrom(ber);

    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(1, frame.tlvCount());
    assertEquals(1, frame.getUndecoded().size());

    assertArrayEquals(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"), frame.getContent(0x6F));

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(28, undecoded.offset());
    assertEquals(4, undecoded.length());
    assertEquals(0, undecoded.depth());
    assertEquals("content bound is beyond content limit (p=31, b=33; l=32)", undecoded.reason());
  }

  @Test void testUndecodedCase2() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);

    BerFrame frame = BerFrame.parseFrom(buffer, OFFSET, ber.length);

    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(1, frame.tlvCount());
    assertEquals(1, frame.getUndecoded().size());

    assertArrayEquals(hexToByteArray("840E315041592E5359532E4444463031A5088801025F2D02656E"), frame.getContent(0x6F));

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(28, undecoded.offset());
    assertEquals(4, undecoded.length());
    assertEquals(0, undecoded.depth());
    assertEquals("content bound is beyond content limit (p=31, b=33; l=32)", undecoded.reason());
  }

  @Test void testUndecodedCase3() {
    byte[] ber = hexToByteArray("9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);

    BerFrame frame = BerFrame.parseFrom(buffer, OFFSET, ber.length);

    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(0, frame.tlvCount());
    assertEquals(1, frame.getUndecoded().size());

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(0, undecoded.offset());
    assertEquals(4, undecoded.length());
    assertEquals(0, undecoded.depth());
    assertEquals("content bound is beyond content limit (p=3, b=5; l=4)", undecoded.reason());
  }

  @Test void testUndecodedCase4() {
    byte[] ber = hexToByteArray("B78F9D69485B90134E653D0C9CAA283700F29EA478D3FEECC2919997C093705B");

    BerFrame frame = BerFrame.parseFrom(ber);

    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(0, frame.tlvCount());
    assertEquals(1, frame.getUndecoded().size());

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(0, undecoded.offset());
    assertEquals(32, undecoded.length());
    assertEquals(0, undecoded.depth());
    assertEquals("invalid length", undecoded.reason());
  }

  @Test void shouldThrowExceptionForDecodeIndefiniteForm() {
    BerFrame frame = BerFrame.parseFrom(hexToByteArray("8480010000"));
    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(0, frame.tlvCount());
    assertEquals(1, frame.getUndecoded().size());

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(0, undecoded.offset());
    assertEquals(5, undecoded.length());
    assertEquals(0, undecoded.depth());
    assertEquals("indefinite length form", undecoded.reason()); 
  }

  @Test void incorrectMessageLengthCase1() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);

    BerFrame frame = BerFrame.parseFrom(buffer, OFFSET, ber.length);
    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(1, frame.tlvCount());
    assertEquals(1, frame.getUndecoded().size());

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(28, undecoded.offset());
    assertEquals(4, undecoded.length());
    assertEquals(0, undecoded.depth());
    assertEquals("content bound is beyond content limit (p=31, b=33; l=32)", undecoded.reason());
  }

  @Test void incorrectMessageLengthCase2() {
    // [6F [84 ..][A5 [88 ..][2D ..][9F ..]]] [36 [00 ..]]
    BerFrame frame = BerFrame.parseFrom(hexToByteArray(
        "6F 1A 84 0E 315041592E5359532E4444463031" +
          "A5 08" +
            "88 01 02" +
            "2D 02" +
              "65 6E" +
            "9F" +
        "36 02" +
          "00 60"));

    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(2, frame.tlvCount());
    assertEquals(3, frame.getUndecoded().size());

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(25, undecoded.offset());
    assertEquals(2, undecoded.length());
    assertEquals(3, undecoded.depth());
    assertEquals("content bound is beyond content limit (p=27, b=137; l=27)", undecoded.reason());
    
    undecoded = frame.getUndecoded().get(1);
    assertEquals(27, undecoded.offset());
    assertEquals(1, undecoded.length());
    assertEquals(2, undecoded.depth());
    assertEquals("truncated tag", undecoded.reason());
  }

  @Test void incorrectMessageLengthCase3() {
    BerFrame frame = BerFrame.parseFrom(hexToByteArray("9F"));
    assertFalse(frame.getUndecoded().isEmpty());
    assertEquals(0, frame.tlvCount());
    assertEquals(1, frame.getUndecoded().size());

    Undecoded undecoded = frame.getUndecoded().getFirst();
    assertEquals(0, undecoded.offset());
    assertEquals(1, undecoded.length());
    assertEquals(0, undecoded.depth());
    assertEquals("truncated tag", undecoded.reason());
  }
}
