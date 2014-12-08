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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class EmvBerFormatterTest {

  private final byte[] BER = BerUtil.hexToByteArray(
      ("6F1A840E 31504159 2E535953 2E444446 3031A508 8801025F 2D02656E 77299f27"
     + "01009f36 0200609F 2608c2c1 2b098f3d a6e39f10 12011125 8013423a 02cfec00"
     + "00000201 1400ff20 00").replaceAll(" ", ""));

  private final String expected =
      "\n ├─[6F] File Control Information (FCI) Template\n"
      + " │  │ 840E315041592E5359532E4444463031A5088801025F2D02656E\n"
      + " │  ├─[84] Dedicated File (DF) Name\n"
      + " │  │   315041592E5359532E4444463031\n"
      + " │  └─[A5] File Control Information (FCI) Proprietary Template\n"
      + " │     │ 8801025F2D02656E\n"
      + " │     ├─[88] Short File Identifier (SFI)\n"
      + " │     │   02\n"
      + " │     └─[5F2D] Language Preference\n"
      + " │         656E\n"
      + " ├─[77] Response Message Template Format 2\n"
      + " │  │ 9F2701009F360200609F2608C2C12B098F3DA6E39F10120111258013423A02CFEC00000002011400FF\n"
      + " │  ├─[9F27] Cryptogram Information Data\n"
      + " │  │   00\n"
      + " │  ├─[9F36] Application Transaction Counter (ATC)\n"
      + " │  │   0060\n"
      + " │  ├─[9F26] Application Cryptogram\n"
      + " │  │   C2C12B098F3DA6E3\n"
      + " │  └─[9F10] Issuer Application Data\n"
      + " │      0111258013423A02CFEC00000002011400FF\n"
      + " └─[20]";

  private final String expectedWithSpaces =
      "\n ├─[6F] File Control Information (FCI) Template\n"
      + " │  │ 84 0E 31 50 41 59 2E 53  59 53 2E 44 44 46 30 31\n"
      + " │  │ A5 08 88 01 02 5F 2D 02  65 6E\n"
      + " │  ├─[84] Dedicated File (DF) Name\n"
      + " │  │   31 50 41 59 2E 53 59 53  2E 44 44 46 30 31\n"
      + " │  └─[A5] File Control Information (FCI) Proprietary Template\n"
      + " │     │ 88 01 02 5F 2D 02 65 6E\n"
      + " │     ├─[88] Short File Identifier (SFI)\n"
      + " │     │   02\n"
      + " │     └─[5F2D] Language Preference\n"
      + " │         65 6E\n"
      + " ├─[77] Response Message Template Format 2\n"
      + " │  │ 9F 27 01 00 9F 36 02 00  60 9F 26 08 C2 C1 2B 09\n"
      + " │  │ 8F 3D A6 E3 9F 10 12 01  11 25 80 13 42 3A 02 CF\n"
      + " │  │ EC 00 00 00 02 01 14 00  FF\n"
      + " │  ├─[9F27] Cryptogram Information Data\n"
      + " │  │   00\n"
      + " │  ├─[9F36] Application Transaction Counter (ATC)\n"
      + " │  │   00 60\n"
      + " │  ├─[9F26] Application Cryptogram\n"
      + " │  │   C2 C1 2B 09 8F 3D A6 E3\n"
      + " │  └─[9F10] Issuer Application Data\n"
      + " │      01 11 25 80 13 42 3A 02  CF EC 00 00 00 02 01 14\n"
      + " │      00 FF\n"
      + " └─[20]";

  private final BerDecoder berDecoder = new BerDecoder();

  @Rule
  public final ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void testPrint() throws Exception {
    BerFrame berFrame = berDecoder.decode(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, EmvBerFormatter.newInstance());
    printer.print(berFrame);
    assertEquals(expected, baos.toString());
  }

  @Test
  public void testPrintWithSpaces() throws Exception {
    BerFrame berFrame = berDecoder.decode(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, EmvBerFormatter.newInstanceWithSpaces());
    printer.print(berFrame);
    assertEquals(expectedWithSpaces, baos.toString());
  }

  @Test
  public void testPrintTagsFromFile() throws Exception {
    BerFrame berFrame = berDecoder.decode(BER);
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

  @Theory
  public void shouldThrowExceptionForIoException() throws IOException {
    exceptionRule.expect(IOException.class);
    try {
      System.setProperty("emv.tags", "unset");
      EmvBerFormatter.newInstanceWithSpaces();
    } finally {
      System.clearProperty("emv.tags");
    }
  }
}
