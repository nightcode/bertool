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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamBerPrinterTest {

  private final byte[] BER = BerUtil.hexToByteArray(
      ("6F1A840E 31504159 2E535953 2E444446 3031A508 8801025F 2D02656E 77299f27"
     + "01009f36 0200609F 2608c2c1 2b098f3d a6e39f10 12011125 8013423a 02cfec00"
     + "00000201 1400ff20 00").replace(" ", ""));

  private final String expected =
      """
       ├─[6F] 840E315041592E5359532E4444463031A5088801025F2D02656E
       │  ├─[84] 315041592E5359532E4444463031
       │  └─[A5] 8801025F2D02656E
       │     ├─[88] 02
       │     └─[5F2D] 656E
       ├─[77] 9F2701009F360200609F2608C2C12B098F3DA6E39F10120111258013423A02CFEC00000002011400FF
       │  ├─[9F27] 00
       │  ├─[9F36] 0060
       │  ├─[9F26] C2C12B098F3DA6E3
       │  └─[9F10] 0111258013423A02CFEC00000002011400FF
       └─[20]\
      """;

  static Stream<BerFormatter> formatters() throws IOException {
    return Stream.of(new DefaultBerFormatter(), EmvBerFormatter.newInstance());
  }

  @Test void testPrint() throws IOException {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new SimpleBerFormatter());
    printer.print(berFrame);

    assertEquals(expected, baos.toString());
  }

  @Test void testPrintEmpty() throws IOException {
    BerFrame berFrame = BerFrame.parseFrom(BerUtil.hexToByteArray(""));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new SimpleBerFormatter());
    printer.print(berFrame);

    assertEquals("", baos.toString());
  }

  @Test void testPrintWithOffset() throws IOException {
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

  @Test void testPrintExtendInternalBuffer() throws IOException, NoSuchFieldException, IllegalAccessException {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new SimpleBerFormatter());

    Field internalBuffer = StreamBerPrinter.class.getDeclaredField("tmpBuffer");
    internalBuffer.setAccessible(true);
    internalBuffer.set(printer, new byte[8]);

    printer.print(berFrame);
    assertEquals(expected, baos.toString());
  }

  @ParameterizedTest @MethodSource("formatters")
  void shouldPrint(BerFormatter formatter) throws IOException {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, formatter);
    printer.print(berFrame);
  }
}
