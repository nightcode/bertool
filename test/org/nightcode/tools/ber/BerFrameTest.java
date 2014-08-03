/*
 * Copyright (C) The NightCode Open Source Project
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

import org.junit.Assert;
import org.junit.Test;

public class BerFrameTest {

  @Test
  public void testGetContentByte() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105"));

    Assert.assertArrayEquals(BerUtil.hexToByteArray("01"), berFrame.getContent((byte) 0x5E));
  }

  @Test
  public void testGetContentInt() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105"));

    Assert.assertArrayEquals(BerUtil.hexToByteArray("01"), berFrame.getContent(0x5E));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("02"), berFrame.getContent(0x5F2D));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("03"), berFrame.getContent(0x5FDF03));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("04"), berFrame.getContent(0xDFDFDF04));
  }
  
  @Test
  public void testGetContentLong() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105DFDFDFDF060106DFDFDFDFDF070107" 
            + "DFDFDFDFDFDF080108DFDFDFDFDFDFDF090109"));

    Assert.assertArrayEquals(BerUtil.hexToByteArray("01"), berFrame.getContent(0x5EL));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("02"), berFrame.getContent(0x5F2DL));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("03"), berFrame.getContent(0x5FDF03L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("04"), berFrame.getContent(0xDFDFDF04L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("06"), berFrame.getContent(0xDFDFDFDF06L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("07"), berFrame.getContent(0xDFDFDFDFDF07L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("08"), berFrame.getContent(0xDFDFDFDFDFDF08L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("09")
        , berFrame.getContent(0xDFDFDFDFDFDFDF09L));
  }

  @Test
  public void testGetContentByteArray() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105DFDFDFDF060106DFDFDFDFDF070107" 
            + "DFDFDFDFDFDF080108DFDFDFDFDFDFDF090109"));

    Assert.assertArrayEquals(BerUtil.hexToByteArray("01"), berFrame.getContent((byte) 0x5EL));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("02"), berFrame
        .getContent((byte) 0x5F, (byte) 0x2D));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("03"), berFrame
        .getContent((byte) 0x5F, (byte) 0xDF, (byte) 0x03));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("04"), berFrame
        .getContent((byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0x04));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("06"), berFrame
        .getContent((byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0x06));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("07"), berFrame
        .getContent((byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0x07));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("08"), berFrame.getContent((byte) 0xDF
        , (byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0x08));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("09"), berFrame
        .getContent((byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0xDF
                  , (byte) 0xDF, (byte) 0xDF, (byte) 0xDF, (byte) 0x09));
  }
}
