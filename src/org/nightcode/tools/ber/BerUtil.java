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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class BerUtil {

  static final Charset ASCII = StandardCharsets.US_ASCII;

  static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  private static final int MASK_DEFINITE_LONG_FORM = 0x80;

  static String byteArrayToHex(byte[] bytes) {
    int capacity = bytes.length << 1;
    StringBuilder builder = new StringBuilder(capacity);
    for (byte b : bytes) {
      builder.append(UPPER_HEX_DIGITS[(b & 0xF0) >> 4]);
      builder.append(UPPER_HEX_DIGITS[b & 0x0F]);
    }
    return builder.toString();
  }

  static byte[] identifierToByteArray(final int identifier) {
    final byte[] buffer;
    final int i = identifier ^ 0x80000000;
    if (i <= 0x800000FF) {
      buffer = new byte[1];
      buffer[0] = (byte) (identifier >>>  0);
    } else if (i <= 0x8000FFFF) {
      buffer = new byte[2];
      buffer[0] = (byte) (identifier >>>  8);
      buffer[1] = (byte) (identifier >>>  0);
    } else if (i <= 0x80FFFFFF) {
      buffer = new byte[3];
      buffer[0] = (byte) (identifier >>> 16);
      buffer[1] = (byte) (identifier >>>  8);
      buffer[2] = (byte) (identifier >>>  0);
    } else {
      buffer = new byte[4];
      buffer[0] = (byte) (identifier >>> 24);
      buffer[1] = (byte) (identifier >>> 16);
      buffer[2] = (byte) (identifier >>>  8);
      buffer[3] = (byte) (identifier >>>  0);
    }
    return buffer;
  }

  static byte[] identifierToByteArray(final long identifier) {
    final byte[] buffer;
    final long l = identifier ^ 0x8000000000000000L;
    if (l <= 0x80000000000000FFL) {
      buffer = new byte[1];
      buffer[0] = (byte) (identifier >>>  0);
    } else if (l <= 0x800000000000FFFFL) {
      buffer = new byte[2];
      buffer[0] = (byte) (identifier >>>  8);
      buffer[1] = (byte) (identifier >>>  0);
    } else if (l <= 0x8000000000FFFFFFL) {
      buffer = new byte[3];
      buffer[0] = (byte) (identifier >>> 16);
      buffer[1] = (byte) (identifier >>>  8);
      buffer[2] = (byte) (identifier >>>  0);
    } else if (l <= 0x80000000FFFFFFFFL) {
      buffer = new byte[4];
      buffer[0] = (byte) (identifier >>> 24);
      buffer[1] = (byte) (identifier >>> 16);
      buffer[2] = (byte) (identifier >>>  8);
      buffer[3] = (byte) (identifier >>>  0);
    } else if (l <= 0x800000FFFFFFFFFFL) {
      buffer = new byte[5];
      buffer[0] = (byte) (identifier >>> 32);
      buffer[1] = (byte) (identifier >>> 24);
      buffer[2] = (byte) (identifier >>> 16);
      buffer[3] = (byte) (identifier >>>  8);
      buffer[4] = (byte) (identifier >>>  0);
    } else if (l <= 0x8000FFFFFFFFFFFFL) {
      buffer = new byte[6];
      buffer[0] = (byte) (identifier >>> 40);
      buffer[1] = (byte) (identifier >>> 32);
      buffer[2] = (byte) (identifier >>> 24);
      buffer[3] = (byte) (identifier >>> 16);
      buffer[4] = (byte) (identifier >>>  8);
      buffer[5] = (byte) (identifier >>>  0);
    } else if (l <= 0x80FFFFFFFFFFFFFFL) {
      buffer = new byte[7];
      buffer[0] = (byte) (identifier >>> 48);
      buffer[1] = (byte) (identifier >>> 40);
      buffer[2] = (byte) (identifier >>> 32);
      buffer[3] = (byte) (identifier >>> 24);
      buffer[4] = (byte) (identifier >>> 16);
      buffer[5] = (byte) (identifier >>>  8);
      buffer[6] = (byte) (identifier >>>  0);
    } else {
      buffer = new byte[8];
      buffer[0] = (byte) (identifier >>> 56);
      buffer[1] = (byte) (identifier >>> 48);
      buffer[2] = (byte) (identifier >>> 40);
      buffer[3] = (byte) (identifier >>> 32);
      buffer[4] = (byte) (identifier >>> 24);
      buffer[5] = (byte) (identifier >>> 16);
      buffer[6] = (byte) (identifier >>>  8);
      buffer[7] = (byte) (identifier >>>  0);
    }
    return buffer;
  }

  static byte[] hexToByteArray(final String hex) {
    Objects.requireNonNull(hex, "hexadecimal string");
    final int length = hex.length();
    if ((length & 0x1) != 0) {
      throw new IllegalStateException(String
          .format("hexadecimal string <%s> must have an even number of characters.", hex));
    }
    byte[] result = new byte[length >> 1];
    for (int i = 0; i < length; i += 2) {
      int hn = Character.digit(hex.charAt(i), 16);
      int ln = Character.digit(hex.charAt(i + 1), 16);
      result[i >> 1] = (byte) ((hn << 4) | ln);
    }
    return result;
  }

  static void checkIdentifier(byte[] identifier) {
    if (identifier == null || identifier.length == 0) {
      throw new IllegalStateException("identifier must be at least 1 byte length");
    }
    if (identifier[0] == 0) {
      throw new IllegalStateException("wrong identifier leading octet value: 0x00");
    }
    if (((identifier[0] & 0x1F) != 0x1F)) {
      if (identifier.length == 1) {
        return;
      }
      throw new IllegalStateException("wrong identifier leading octet value: 0x" + Integer.toHexString(identifier[0] & 0xFF));
    }
    int index = 1;
    while (index < identifier.length && (identifier[index] & 0x80) == 0x80) {
      index++;
    }
    index++;
    if (index > identifier.length) {
      throw new IllegalArgumentException("raw TLV has wrong identifier");
    }
  }

  static void checkRawTlv(byte[] rawTlv) {
    if (rawTlv == null || rawTlv.length < 2) {
      throw new IllegalArgumentException("raw TLV must be at least 2 bytes length");
    }

    int index = 1;
    if ((rawTlv[0] & 0x1F) == 0x1F) {
      while (index < rawTlv.length && (rawTlv[index] & 0x80) == 0x80) {
        index++;
      }
      index++;
      if (index > rawTlv.length) {
        throw new IllegalArgumentException("raw TLV has wrong identifier");
      }
    }

    if (index >= rawTlv.length) {
      throw new IllegalArgumentException("raw TLV has no length octets");
    }
    int firstLength = rawTlv[index++] & 0xFF;
    int contentLength = 0;
    if ((firstLength & MASK_DEFINITE_LONG_FORM) == MASK_DEFINITE_LONG_FORM) {
      int numberOfSubsequentOctets = firstLength & 0x7F;
      if (numberOfSubsequentOctets == 0 || numberOfSubsequentOctets > 4 || index + numberOfSubsequentOctets > rawTlv.length) {
        throw new IllegalArgumentException("raw TLV has malformed long-form length octets");
      }
      for (int i = 0; i < numberOfSubsequentOctets; i++) {
        contentLength = (contentLength << 8) + (rawTlv[index++] & 0xFF);
      }
      if (contentLength < 0) {
        throw new IllegalArgumentException("raw TLV declares a negative length");
      }
    } else {
      contentLength = firstLength;
    }
    if (index + contentLength != rawTlv.length) {
      throw new IllegalArgumentException(String.format("raw TLV length mismatch (expected=%d;supplied=%d)", contentLength, rawTlv.length - index));
    }
  }

  private BerUtil() {
    throw new AssertionError("Utility class");
  }
}
