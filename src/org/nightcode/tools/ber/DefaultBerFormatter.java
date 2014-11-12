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

class DefaultBerFormatter extends AbstractBerFormatter {

  @Override public void format(OutputStream stream, BerBuffer buffer, BerTlv tlv, byte[] prefix,
      int prefixLength, boolean node) throws IOException {
    stream.write(prefix, 0, prefixLength);
    stream.write(node ? NODE_PREFIX : LEAF_PREFIX);
    stream.write(LEFT_BRACKET);
    writeToStream(stream, buffer, tlv.identifierPosition(), tlv.identifierLength());
    stream.write(RIGHT_BRACKET);
    if (tlv.contentLength() > 0) {
      stream.write(SPACE);
      writeToStream(stream, buffer, tlv.contentPosition(), tlv.contentLength());
    }
  }
}
