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
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class StreamBerPrinterTest {

  private final byte[] BER = BerUtil.hexToByteArray(
      ("6F1A840E 31504159 2E535953 2E444446 3031A508 8801025F 2D02656E 77299f27"
     + "01009f36 0200609F 2608c2c1 2b098f3d a6e39f10 12011125 8013423a 02cfec00"
     + "00000201 1400ff20 00").replaceAll(" ", ""));

  private final String expected =
      " ├─[6F] 840E315041592E5359532E4444463031A5088801025F2D02656E\n"
    + " │  ├─[84] 315041592E5359532E4444463031\n"
    + " │  └─[A5] 8801025F2D02656E\n"
    + " │     ├─[88] 02\n"
    + " │     └─[5F2D] 656E\n"
    + " ├─[77] 9F2701009F360200609F2608C2C12B098F3DA6E39F10120111258013423A02CFEC00000002011400FF\n"
    + " │  ├─[9F27] 00\n"
    + " │  ├─[9F36] 0060\n"
    + " │  ├─[9F26] C2C12B098F3DA6E3\n"
    + " │  └─[9F10] 0111258013423A02CFEC00000002011400FF\n"
    + " └─[20]";

  
  @DataPoint
  public static BerFormatter DEFAULT_FORMATTER;

  @DataPoint
  public static BerFormatter EMV_FORMATTER;

  @BeforeClass
  public static void setUpOnce() throws IOException {
    DEFAULT_FORMATTER = new DefaultBerFormatter();
    EMV_FORMATTER = EmvBerFormatter.newInstance();
  }

  @Test
  public void testPrint() throws IOException {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new SimpleBerFormatter());
    printer.print(berFrame);

    assertEquals(expected, baos.toString());
  }

  @Test
  public void testPrintEmpty() throws IOException {
    BerFrame berFrame = BerFrame.parseFrom(BerUtil.hexToByteArray(""));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new SimpleBerFormatter());
    printer.print(berFrame);

    assertEquals("", baos.toString());
  }

  @Test
  public void testPrintWithOffset() throws IOException {
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(BER.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(BER);
    BerFrame berFrame = BerFrame.parseFrom(buffer, offset, BER.length);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new SimpleBerFormatter());
    printer.print(berFrame);

    assertEquals(expected, baos.toString());
  }

  @Test
  public void testPrintExtendInternalBuffer() throws IOException, NoSuchFieldException, IllegalAccessException {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new SimpleBerFormatter());

    Field internalBuffer = printer.getClass().getDeclaredField("tmpBuffer");
    boolean accessible = internalBuffer.isAccessible();
    internalBuffer.setAccessible(true);
    internalBuffer.set(printer, new byte[8]);
    internalBuffer.setAccessible(accessible);

    printer.print(berFrame);
    assertEquals(expected, baos.toString());
  }

  @Theory
  public void shouldPrint(BerFormatter formatter) throws IOException {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, formatter);
    printer.print(berFrame);
  }
}
