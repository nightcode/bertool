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

import java.util.List;

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

  @Test
  public void testGetContentEmpty() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105DFDFDFDF060106DFDFDFDFDF070107" 
            + "DFDFDFDFDFDF080108DFDFDFDFDFDFDF090109"));

    byte[] result = berFrame.getContent();
    Assert.assertNull(result);
  }

  @Test
  public void testGetAllContentsByte() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D01055E0107"));
    final byte tag = 0x5E;

    Assert.assertEquals(2, berFrame.getAllContents(tag).size());
    Assert.assertArrayEquals(BerUtil.hexToByteArray("01"), berFrame.getAllContents(tag).get(0));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("07"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsInt() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105DFDFDF040106"));
    final int tag = 0xDFDFDF04;

    Assert.assertArrayEquals(BerUtil.hexToByteArray("01"), berFrame.getContent(0x5E));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("02"), berFrame.getContent(0x5F2D));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("03"), berFrame.getContent(0x5FDF03));
    Assert.assertEquals(2, berFrame.getAllContents(tag).size());
    Assert.assertArrayEquals(BerUtil.hexToByteArray("04"), berFrame.getAllContents(tag).get(0));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("06"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsLong() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105DFDFDFDF060106DFDFDFDFDF070107" 
            + "DFDFDFDFDFDF080108DFDFDFDFDFDFDF090109DFDFDFDFDFDFDF090111"));
    final long tag = 0xDFDFDFDFDFDFDF09L;

    Assert.assertArrayEquals(BerUtil.hexToByteArray("01"), berFrame.getContent(0x5EL));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("02"), berFrame.getContent(0x5F2DL));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("03"), berFrame.getContent(0x5FDF03L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("04"), berFrame.getContent(0xDFDFDF04L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("06"), berFrame.getContent(0xDFDFDFDF06L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("07"), berFrame.getContent(0xDFDFDFDFDF07L));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("08"), berFrame.getContent(0xDFDFDFDFDFDF08L));
    Assert.assertEquals(2, berFrame.getAllContents(tag).size());
    Assert.assertArrayEquals(BerUtil.hexToByteArray("09"), berFrame.getAllContents(tag).get(0));
    Assert.assertArrayEquals(BerUtil.hexToByteArray("11"), berFrame.getAllContents(tag).get(1));
  }

  @Test
  public void testGetAllContentsEmpty() {
    final BerDecoder berDecoder = new BerDecoder();
    BerFrame berFrame = berDecoder.decode(BerUtil
        .hexToByteArray("5E01015F2D01025FDF030103DFDFDF0401045F2D0105DFDFDFDF060106DFDFDFDFDF070107" 
            + "DFDFDFDFDFDF080108DFDFDFDFDFDFDF090109"));

    List<byte[]> result = berFrame.getAllContents();
    Assert.assertTrue(result.isEmpty());
  }
}
