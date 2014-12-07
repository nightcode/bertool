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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import static org.nightcode.tools.ber.BerUtil.hexToByteArray;

@RunWith(Theories.class)
public class BerDecoderTest {

  private static final int OFFSET = 10;

  @Rule
  public final ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void testDecodePrimitive() {
    BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(hexToByteArray("9F2608C2C12B098F3DA6E3"));

    assertArrayEquals(hexToByteArray("C2C12B098F3DA6E3"), berFrame.getContent(0x9F26));
  }

  @Test
  public void testDecodeConstructed() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(hexToByteArray(
        "6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060"));

    assertArrayEquals(hexToByteArray("315041592E5359532E4444463031"), berFrame.getContent(0x84));
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getContent(0xA5));
    assertArrayEquals(hexToByteArray("02"), berFrame.getContent(0x88));
    assertArrayEquals(hexToByteArray("656E"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("0060"), berFrame.getContent(0x9F36));
  }

  @Test
  public void testDecodeConstructedByteBuffer() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(ByteBuffer.wrap(hexToByteArray(
        "6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060")));

    assertArrayEquals(hexToByteArray("315041592E5359532E4444463031"), berFrame.getContent(0x84));
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getContent(0xA5));
    assertArrayEquals(hexToByteArray("02"), berFrame.getContent(0x88));
    assertArrayEquals(hexToByteArray("656E"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("0060"), berFrame.getContent(0x9F36));
  }

  @Test
  public void testDecodeConstructedWithOffset() {
    final byte[] ber = hexToByteArray(
        "6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(buffer, OFFSET, ber.length);

    assertArrayEquals(hexToByteArray("315041592E5359532E4444463031"), berFrame.getContent(0x84));
    assertArrayEquals(hexToByteArray("8801025F2D02656E"), berFrame.getContent(0xA5));
    assertArrayEquals(hexToByteArray("02"), berFrame.getContent(0x88));
    assertArrayEquals(hexToByteArray("656E"), berFrame.getContent(0x5F2D));
    assertArrayEquals(hexToByteArray("0060"), berFrame.getContent(0x9F36));
  }

  @Test
  public void testDecodeDefiniteLongForm() {
    java.util.Random random = new java.util.Random();
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

    assertArrayEquals(content, berFrame.getContent(0x84));
  }

  @Test
  public void testDecoderExceptionCase1() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    try {
      new BerDecoder().decode(ber);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      Assert.assertArrayEquals(hexToByteArray("9f360200"), ex.getUndecoded());
      Assert.assertArrayEquals(hexToByteArray("840E315041592E5359532E4444463031" 
          + "A5088801025F2D02656E"), ex.getPartialBerFrame().getContent(0x6F));
    }
  }

  @Test
  public void testDecoderExceptionCase2() {
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);
    try {
      new BerDecoder().decode(buffer, OFFSET, ber.length);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      Assert.assertArrayEquals(hexToByteArray("9f360200"), ex.getUndecoded());
      Assert.assertArrayEquals(hexToByteArray("840E315041592E5359532E4444463031" 
          + "A5088801025F2D02656E"), ex.getPartialBerFrame().getContent(0x6F));
    }
  }

  @Test
  public void testDecoderExceptionCase3() {
    byte[] ber = hexToByteArray("9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);
    try {
      new BerDecoder().decode(buffer, OFFSET, ber.length);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      Assert.assertArrayEquals(hexToByteArray("9f360200"), ex.getUndecoded());
      Assert.assertFalse(ex.getPartialBerFrame().getIdentifiers().hasNext());
    }
  }

  @Test
  public void testDecoderExceptionCase4() {
    byte[] ber = hexToByteArray("B78F9D69485B90134E653D0C9CAA283700F29EA478D3FEECC2919997C093705B");
    try {
      new BerDecoder().decode(ber);
      fail("should throw DecoderException");
    } catch (DecoderException ex) {
      Assert.assertArrayEquals(hexToByteArray("B78F9D69485B90134E653D0C9CAA283700F29EA478D3FEEC"
          + "C2919997C093705B"), ex.getUndecoded());
    }
  }

  @Theory
  public void shouldThrowExceptionForDecodeIndefiniteForm() {
    exceptionRule.expectMessage("Indefinite form is not supported yet.");

    final BerDecoder berDecoder = new BerDecoder();
    berDecoder.decode(hexToByteArray("8480010000"));
  }

  @Theory
  public void shouldThrowExceptionForIncorrectMessageLengthCase1() {
    exceptionRule.expectMessage("content bound is beyond content limit (b=43; l=42)");

    BerDecoder berDecoder = new BerDecoder();
    byte[] ber = hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f360200");
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + OFFSET * 2);
    buffer.put((byte) 0xE1);
    buffer.position(OFFSET);
    buffer.put(ber);
    berDecoder.decode(buffer, OFFSET, ber.length);
  }

  @Theory
  public void shouldThrowExceptionForIncorrectMessageLengthCase2() {
    exceptionRule.expectMessage("content bound is beyond content limit (b=137; l=27)");

    BerDecoder berDecoder = new BerDecoder();
    berDecoder.decode(hexToByteArray(
          "6F1A840E315041592E5359532E4444463031A5088801022D02656E9f36020060"));
  }

  @Theory
  public void shouldThrowExceptionForIncorrectMessageLengthCase3() {
    exceptionRule.expectMessage("index is beyond bound (i=1; b=0)");

    BerDecoder berDecoder = new BerDecoder();
    berDecoder.decode(BerUtil.hexToByteArray("9F"));
  }
}
