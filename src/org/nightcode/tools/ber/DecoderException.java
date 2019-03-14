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

/**
 * A general exception that occurs when trying to decode BER packet.
 */
public class DecoderException extends RuntimeException {

  private final BerFrame partialFrame;
  private final byte[] undecoded;

  /**
   * Constructs a DecoderException.
   *
   * @param cause the cause
   * @param partialFrame the decoded part of the frame
   * @param undecoded the undecoded part of the frame
   */
  public DecoderException(Throwable cause, BerFrame partialFrame, byte[] undecoded) {
    super(cause);
    this.partialFrame = partialFrame;
    this.undecoded = new byte[undecoded.length];
    System.arraycopy(undecoded, 0, this.undecoded, 0, undecoded.length);
  }

  /**
   * @return the decoded part of the frame
   */
  public BerFrame getPartialBerFrame() {
    return partialFrame;
  }

  /**
   * @return the undecoded part of the frame
   */
  public byte[] getUndecoded() {
    byte[] copy = new byte[undecoded.length];
    System.arraycopy(undecoded, 0, copy, 0, undecoded.length);
    return copy;
  }
}
