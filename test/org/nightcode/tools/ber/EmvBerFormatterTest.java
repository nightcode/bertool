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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmvBerFormatterTest {

  private final byte[] BER = BerUtil.hexToByteArray(
      ("6F1A840E 31504159 2E535953 2E444446 3031A508 8801025F 2D02656E 77299f27"
     + "01009f36 0200609F 2608c2c1 2b098f3d a6e39f10 12011125 8013423a 02cfec00"
     + "00000201 1400ff20 00").replace(" ", ""));

  private final String expected =
      """
       ├─[6F] File Control Information (FCI) Template
       │  │ 840E315041592E5359532E4444463031A5088801025F2D02656E
       │  ├─[84] Dedicated File (DF) Name
       │  │   315041592E5359532E4444463031
       │  └─[A5] File Control Information (FCI) Proprietary Template
       │     │ 8801025F2D02656E
       │     ├─[88] Short File Identifier (SFI)
       │     │   02
       │     └─[5F2D] Language Preference
       │         656E
       ├─[77] Response Message Template Format 2
       │  │ 9F2701009F360200609F2608C2C12B098F3DA6E39F10120111258013423A02CFEC00000002011400FF
       │  ├─[9F27] Cryptogram Information Data
       │  │   00
       │  ├─[9F36] Application Transaction Counter (ATC)
       │  │   0060
       │  ├─[9F26] Application Cryptogram
       │  │   C2C12B098F3DA6E3
       │  └─[9F10] Issuer Application Data
       │      0111258013423A02CFEC00000002011400FF
       └─[20]\
      """;

  private final String expectedWithSpaces =
      """
       ├─[6F] File Control Information (FCI) Template
       │  │ 84 0E 31 50 41 59 2E 53  59 53 2E 44 44 46 30 31
       │  │ A5 08 88 01 02 5F 2D 02  65 6E
       │  ├─[84] Dedicated File (DF) Name
       │  │   31 50 41 59 2E 53 59 53  2E 44 44 46 30 31
       │  └─[A5] File Control Information (FCI) Proprietary Template
       │     │ 88 01 02 5F 2D 02 65 6E
       │     ├─[88] Short File Identifier (SFI)
       │     │   02
       │     └─[5F2D] Language Preference
       │         65 6E
       ├─[77] Response Message Template Format 2
       │  │ 9F 27 01 00 9F 36 02 00  60 9F 26 08 C2 C1 2B 09
       │  │ 8F 3D A6 E3 9F 10 12 01  11 25 80 13 42 3A 02 CF
       │  │ EC 00 00 00 02 01 14 00  FF
       │  ├─[9F27] Cryptogram Information Data
       │  │   00
       │  ├─[9F36] Application Transaction Counter (ATC)
       │  │   00 60
       │  ├─[9F26] Application Cryptogram
       │  │   C2 C1 2B 09 8F 3D A6 E3
       │  └─[9F10] Issuer Application Data
       │      01 11 25 80 13 42 3A 02  CF EC 00 00 00 02 01 14
       │      00 FF
       └─[20]\
      """;

  @Test void testPrint() throws Exception {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, EmvBerFormatter.newInstance());
    printer.print(berFrame);
    assertEquals(expected, baos.toString());
  }

  @Test void testPrintWithSpaces() throws Exception {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, EmvBerFormatter.newInstanceWithSpaces());
    printer.print(berFrame);
    assertEquals(expectedWithSpaces, baos.toString());
  }

  @Test void testPrintTagsFromFile() throws Exception {
    BerFrame berFrame = BerFrame.parseFrom(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerFormatter berFormatter;
    try {
      System.setProperty("emv.tags", "resources/emv.tags");
      berFormatter = EmvBerFormatter.newInstanceWithSpaces();
    } finally {
      System.clearProperty("emv.tags");
    }
    BerPrinter printer = new StreamBerPrinter(baos, berFormatter);
    printer.print(berFrame);
    assertEquals(expectedWithSpaces, baos.toString());
  }

  @Test void shouldThrowExceptionForIoException() {
    try {
      System.setProperty("emv.tags", "unset");
      assertThrows(IOException.class, EmvBerFormatter::newInstanceWithSpaces);
    } finally {
      System.clearProperty("emv.tags");
    }
  }
}
