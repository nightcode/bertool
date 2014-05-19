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
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class StreamBerPrinter implements BerPrinter {

  private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  private static final byte CARRIAGE_RETURN = 0x0A;
  private static final byte SPACE = 0x20;
  private static final byte LEFT_BRACKET = 0x5B;
  private static final byte RIGHT_BRACKET = 0x5D;

  private static final byte[] LINE_FEED = new byte[] {CARRIAGE_RETURN, SPACE};

  private static final byte[] LEAF_NEXT_PREFIX = new byte[] {SPACE, SPACE, SPACE};
  private static final byte[] NODE_NEXT_PREFIX
      = new byte[] {(byte) 0xE2, (byte) 0x94, (byte) 0x82, SPACE, SPACE};
  private static final byte[] LEAF_PREFIX
      = new byte[] {(byte) 0xE2, (byte) 0x94, (byte) 0x94, (byte) 0xE2, (byte) 0x94, (byte) 0x80};
  private static final byte[] NODE_PREFIX
      = new byte[] {(byte) 0xE2, (byte) 0x94, (byte) 0x9C, (byte) 0xE2, (byte) 0x94, (byte) 0x80};

  private final OutputStream stream;

  private byte[] tmpBuffer;

  public StreamBerPrinter(OutputStream stream) {
    this.stream = stream;
    tmpBuffer = new byte[1024];
  }

  @Override public void print(BerFrame berFrame) throws IOException {
    BerBuffer buffer = berFrame.berBuffer();
    printLevel(buffer, berFrame.getTlvs(), LINE_FEED, LINE_FEED.length);
    stream.write(CARRIAGE_RETURN);
  }

  private void printLevel(BerBuffer berBuffer, List<BerTlv> tlvs, byte[] prefix, int prefixLength)
      throws IOException {
    Iterator<BerTlv> i = tlvs.iterator();
    while (i.hasNext()) {
      BerTlv tlv = i.next();
      printTlv(berBuffer, tlv, prefix, prefixLength, i.hasNext());
    }
  }

  private void printTlv(BerBuffer berBuffer, BerTlv tlv, byte[] prefix, int prefixLength,
      boolean node) throws IOException {
    stream.write(prefix, 0, prefixLength);
    stream.write(node ? NODE_PREFIX : LEAF_PREFIX);
    stream.write(LEFT_BRACKET);
    writeToStream(stream, berBuffer, tlv.identifierPosition(), tlv.identifierLength());
    stream.write(RIGHT_BRACKET);
    if (tlv.contentLength() > 0) {
      stream.write(SPACE);
      writeToStream(stream, berBuffer, tlv.contentPosition(), tlv.contentLength());
    }
    if (tlv.isConstructed()) {
      byte[] addPrefix = node ? NODE_NEXT_PREFIX : LEAF_NEXT_PREFIX;
      if (tmpBuffer.length < prefixLength + addPrefix.length) {
        tmpBuffer = new byte[tmpBuffer.length << 1];
      }
      System.arraycopy(prefix, 0, tmpBuffer, 0, prefixLength);
      System.arraycopy(addPrefix, 0, tmpBuffer, prefixLength, addPrefix.length);
      printLevel(berBuffer, tlv.children(), tmpBuffer, prefixLength + addPrefix.length);
    }
  }

  private void writeToStream(OutputStream stream, BerBuffer buffer, int offset, int length)
      throws IOException {
    int size = offset + length;
    for (int i = offset; i < size; i++) {
      stream.write((byte) UPPER_HEX_DIGITS[(buffer.getByte(i) & 0xF0) >> 4]);
      stream.write((byte) UPPER_HEX_DIGITS[buffer.getByte(i) & 0x0F]);
    }
  }
}
