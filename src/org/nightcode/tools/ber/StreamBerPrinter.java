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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class StreamBerPrinter implements BerPrinter {

  private final OutputStream stream;
  private final BerFormatter formatter;

  private byte[] tmpBuffer;

  /**
   * Creates a new StreamBerPrinter.
   *
   * @param stream the supplied output stream
   */
  public StreamBerPrinter(OutputStream stream) {
    this(stream, new DefaultBerFormatter());
  }

  /**
   * Creates a new StreamBerPrinter.
   *
   * @param stream the supplied output stream
   * @param formatter node formatter
   */
  public StreamBerPrinter(OutputStream stream, BerFormatter formatter) {
    this.stream = stream;
    this.formatter = formatter;
    tmpBuffer = new byte[1024];
  }

  @Override public void print(BerFrame berFrame) throws IOException {
    BerBuffer buffer = berFrame.berBuffer();
    printImpl(buffer, berFrame.getTlvs(), formatter.linePrefix(), formatter.linePrefix().length);
    stream.flush();
  }

  private void printImpl(BerBuffer berBuffer, List<BerTlv> tlvs, byte[] prefix, int prefixLength)
      throws IOException {
    Iterator<BerTlv> i = tlvs.iterator();
    if (i.hasNext()) {
      BerTlv tlv = i.next();
      printTlv(berBuffer, tlv, prefix, prefixLength, i.hasNext());
    }
    printLevel(berBuffer, i, prefix, prefixLength);
  }

  private void printLevel(BerBuffer berBuffer, Iterator<BerTlv> i, byte[] prefix, int prefixLength)
      throws IOException {
    while (i.hasNext()) {
      stream.write(formatter.lineFeed());
      BerTlv tlv = i.next();
      printTlv(berBuffer, tlv, prefix, prefixLength, i.hasNext());
    }
  }

  private void printTlv(BerBuffer berBuffer, BerTlv tlv, byte[] prefix, int prefixLength,
      boolean node) throws IOException {
    formatter.format(stream, berBuffer, tlv, prefix, prefixLength, node);
    if (tlv.isConstructed()) {
      byte[] addPrefix = formatter.nextPrefix(node);
      if (tmpBuffer.length < prefixLength + addPrefix.length) {
        tmpBuffer = new byte[tmpBuffer.length << 1];
      }
      System.arraycopy(prefix, 0, tmpBuffer, 0, prefixLength);
      System.arraycopy(addPrefix, 0, tmpBuffer, prefixLength, addPrefix.length);
      printLevel(berBuffer, tlv.children().iterator(), tmpBuffer, prefixLength + addPrefix.length);
    }
  }
}
