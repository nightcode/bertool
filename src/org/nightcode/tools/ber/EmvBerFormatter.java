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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * BerFormatter which try to represents identifiers as EMV tags.
 */
public final class EmvBerFormatter extends AbstractBerFormatter {

  /**
   * @throws IOException if an I/O error occurs
   */
   public static EmvBerFormatter newInstance() throws IOException {
    return new EmvBerFormatter(false);
  }

  /**
   * @throws IOException if an I/O error occurs
   */
  public static EmvBerFormatter newInstanceWithSpaces() throws IOException {
    return new EmvBerFormatter(true);
  }

  private final Map<String, String> tags = new HashMap<>();

  private EmvBerFormatter(boolean printWithSpaces) throws IOException {
    super(printWithSpaces);
    String  resourceName = System.getProperty("emv.tags");
    try (InputStream in = (resourceName != null)
        ? new FileInputStream(resourceName)
        : EmvBerFormatter.class.getResourceAsStream("/emv.tags")) {
      LineNumberReader lnr = new LineNumberReader(new BufferedReader(new InputStreamReader(in)));
      while (lnr.readLine() != null) {
        String tag = normalize(lnr.readLine());
        if (tag != null) {
          String name = normalize(lnr.readLine());
          tags.put(tag, name);
        }
      }
    }
  }

  @Override public void format(OutputStream stream, BerBuffer buffer, BerTlv tlv, byte[] prefix,
      int prefixLength, boolean node) throws IOException {
    stream.write(prefix, 0, prefixLength);
    stream.write(node ? NODE_PREFIX : LEAF_PREFIX);

    byte[] identifier = new byte[tlv.identifierLength()];
    buffer.getBytes(tlv.identifierPosition(), identifier);
    stream.write(LEFT_BRACKET);
    writeToStream(stream, identifier, 0, identifier.length);
    stream.write(RIGHT_BRACKET);

    final String tag = BerUtil.byteArrayToHex(identifier);
    if (tags.containsKey(tag)) {
      stream.write(SPACE);
      stream.write(tags.get(tag).getBytes("UTF-8"));
    }

    final int contentLength = tlv.contentLength();
    if (contentLength > 0) {
      if (printWithSpaces) {
        final int contentPosition = tlv.contentPosition();
        final int limit = contentPosition + contentLength;
        for (int i = contentPosition; i < limit; i += 16) {
          printContent(stream, buffer, tlv, prefix, prefixLength, node, i, Math.min(16, limit - i));
        }
      } else {
        printContent(stream, buffer, tlv, prefix, prefixLength, node, tlv.contentPosition()
            , contentLength);
      }
    }
  }

  private String normalize(String str) {
    return str != null ? str.trim() : null;
  }

  private void printContent(OutputStream stream, BerBuffer buffer, BerTlv tlv, byte[] prefix,
      int prefixLength, boolean node, int contentPosition, int contentLength) throws IOException {
    stream.write(prefix, 0, prefixLength);
    stream.write(node ? NODE_NEXT_PREFIX : LEAF_NEXT_PREFIX);
    if (tlv.isConstructed()) {
      stream.write(LIGHT_VERTICAL);
    }
    stream.write(SPACE);
    writeToStream(stream, buffer, contentPosition, contentLength);
  }
}
