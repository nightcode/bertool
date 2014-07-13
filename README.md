# BerTool 

Basic Encoding Rules tool.

How to encode 
-------------

```
  BerBuilder builderA5 = new BerBuilder();
  builderA5.add((byte) 0x88, new byte[] {0x02});
  builderA5.addAsciiString((byte) 0x5F, (byte) 0x2D, "en");

  BerBuilder builder6F = new BerBuilder();
  builder6F.addHexString((byte) 0x84, "315041592E5359532E4444463031");
  builder6F.add((byte) 0xA5, builderA5);

  BerBuilder builder = new BerBuilder();
  builder.add((byte) 0x6F, builder6F);
  builder.add((byte) 0x9F, (byte) 0x36, new byte[] {0x00, 0x60});

  ByteBuffer buffer = ByteBuffer.allocate(1024);
  BerEncoder berEncoder = new BerEncoder();
  berEncoder.encode(builder, buffer);
```

How to decode 
-------------

```
  byte[] byteArray = DatatypeConverter.parseHexBinary("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E9f36020060");

  BerDecoder berDecoder = new BerDecoder();
  BerFrame berFrame = berDecoder.decode(byteArray);

  byte[] tag84 = berFrame.getContent((byte) 0x84)
  byte[] tag5F2D = berFrame.getContent((byte) 0x5F, (byte) 0x2D);
```

StreamBerPrinter example
------------------------

```
  byte[] byteArray = DatatypeConverter
    .parseHexBinary("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E77299f2701009f36"
    + "0200609f2608c2c12b098f3da6e39f10120111258013423a02cfec00000002011400ff9000"); 

  BerDecoder berDecoder = new BerDecoder();
  BerFrame berFrame = berDecoder.decode(byteArray);

  BerPrinter printer = new StreamBerPrinter(System.out);
  printer.print(berFrame);
```

output

```
 ├─[6F] 840E315041592E5359532E4444463031A5088801025F2D02656E
 │  ├─[84] 315041592E5359532E4444463031
 │  └─[A5] 8801025F2D02656E
 │     ├─[88] 02
 │     └─[5F2D] 656E
 ├─[77] 9F2701009F360200609F2608C2C12B098F3DA6E39F10120111258013423A02CFEC00000002011400FF
 │  ├─[9F27] 00
 │  ├─[9F36] 0060
 │  ├─[9F26] C2C12B098F3DA6E3
 │  └─[9F10] 0111258013423A02CFEC00000002011400FF
 └─[90]
```


Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/bertool/issues) or simply drop me a line at <dmitry@nightcode.org>.
