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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class StreamBerPrinterTest {

  @Test
  public void testPrint() throws IOException {
    BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E77299f2701009f36"
            + "0200609f2608c2c12b098f3da6e39f10120111258013423a02cfec00000002011400ff9000"));
    BerPrinter printer = new StreamBerPrinter(System.out);
    printer.print(berFrame);
  }

  @Test
  public void testPrintWithOffset() throws IOException {
    final byte[] ber = BerUtil
        .hexToByteArray("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E77299f2701009f36"
            + "0200609f2608c2c12b098f3da6e39f10120111258013423a02cfec00000002011400ff9000");
    final int offset = 10;
    final ByteBuffer buffer = ByteBuffer.allocate(ber.length + offset);
    buffer.put((byte) 0xE1);
    buffer.position(offset);
    buffer.put(ber);
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(buffer, offset, ber.length);
    BerPrinter printer = new StreamBerPrinter(System.out);
    printer.print(berFrame);
  }
}
