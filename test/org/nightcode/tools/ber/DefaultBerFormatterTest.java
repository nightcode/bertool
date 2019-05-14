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

import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class DefaultBerFormatterTest {

  private final byte[] BER = BerUtil.hexToByteArray(
      ("6F1A840E 31504159 2E535953 2E444446 3031A508 8801025F 2D02656E 77299f27"
     + "01009f36 0200609F 2608c2c1 2b098f3d a6e39f10 12011125 8013423a 02cfec00"
     + "00000201 1400ff20 00").replaceAll(" ", ""));

  private final String expected =
        " ├─[6F]\n"
      + " │  ├─[84] 315041592E5359532E4444463031\n"
      + " │  └─[A5]\n"
      + " │     ├─[88] 02\n"
      + " │     └─[5F2D] 656E\n"
      + " ├─[77]\n"
      + " │  ├─[9F27] 00\n"
      + " │  ├─[9F36] 0060\n"
      + " │  ├─[9F26] C2C12B098F3DA6E3\n"
      + " │  └─[9F10] 0111258013423A02CFEC00000002011400FF\n"
      + " └─[20]";

  private final BerDecoder berDecoder = new BerDecoder();

  @Test
  public void testPrint() throws Exception {
    BerFrame berFrame = berDecoder.decode(BER);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BerPrinter printer = new StreamBerPrinter(baos, new DefaultBerFormatter());
    printer.print(berFrame);
    assertEquals(expected, baos.toString());
  }
}
