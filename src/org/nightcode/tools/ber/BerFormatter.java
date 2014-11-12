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

/**
 * Formats TLV.
 */
public interface BerFormatter {

  byte CARRIAGE_RETURN = 0x0A;
  byte LEFT_BRACKET = 0x5B;
  byte RIGHT_BRACKET = 0x5D;
  byte SPACE = 0x20;

  byte[] LIGHT_VERTICAL = new byte[] {(byte) 0xE2, (byte) 0x94, (byte) 0x82};

  byte[] LINE_FEED = new byte[] {CARRIAGE_RETURN, SPACE};

  // "   "
  byte[] LEAF_NEXT_PREFIX = new byte[] {SPACE, SPACE, SPACE};

  // "│  "
  byte[] NODE_NEXT_PREFIX = new byte[] {(byte) 0xE2, (byte) 0x94, (byte) 0x82, SPACE, SPACE};

  // "└─"
  byte[] LEAF_PREFIX
      = new byte[] {(byte) 0xE2, (byte) 0x94, (byte) 0x94, (byte) 0xE2, (byte) 0x94, (byte) 0x80};

  // "├─"
  byte[] NODE_PREFIX
      = new byte[] {(byte) 0xE2, (byte) 0x94, (byte) 0x9C, (byte) 0xE2, (byte) 0x94, (byte) 0x80};

  /**
   * Formats the {@code tlv} and writes result to supplied {@code OutputStream}.
   *
   * @param stream the supplied {@code OutputStream}
   * @param buffer the supplied {@code BerBuffer} with the TLV content
   * @param tlv the TLV to be formatted
   * @param prefix the prefix content
   * @param prefixLength the prefix length
   * @param node is TLV the last one on level
   * @throws IOException if an I/O error occurs
   */
  void format(OutputStream stream, BerBuffer buffer, BerTlv tlv, byte[] prefix, int prefixLength,
      boolean node) throws IOException;

  /**
   * Returns line feed.
   *
   * @return line feed
   */
  byte[] lineFeed();

  /**
   * Returns prefix for next TLV.
   *
   * @param node is TLV the last one on level
   * @return prefix for next TLV
   */
  byte[] nextPrefix(boolean node);
}
