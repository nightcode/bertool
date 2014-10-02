# BerTool 

[![Build Status](https://travis-ci.org/nightcode/bertool.svg?branch=master)](https://travis-ci.org/nightcode/bertool) [![Coverage Status](https://coveralls.io/repos/nightcode/bertool/badge.png?branch=master)](https://coveralls.io/r/nightcode/bertool?branch=master)

Basic Encoding Rules tool.
BerTool was primarily created to decode EMV records encoded in tag-length-value or BER TLV format (TLV is also known as type-length value).
With this tool it is easy not only to decode but to encode records too.

How to encode 
-------------

```
 ├─[6F]
 │  ├─[84] 315041592E5359532E4444463031
 │  └─[A5]
 │     ├─[88] 02
 │     └─[5F2D] 656E
 └─[9F36] 0060
```

code

```java
  BerBuilder builderA5 = BerBuilder.newInstance();
  builderA5.add(0x88, new byte[] {0x02});
  builderA5.addAsciiString(0x5F2D, "en");

  BerBuilder builder6F = BerBuilder.newInstance();
  builder6F.addHexString(0x84, "315041592E5359532E4444463031");
  builder6F.add(0xA5, builderA5);

  BerBuilder builder = BerBuilder.newInstance();
  builder.add(0x6F, builder6F);
  builder.add(0x9F36, new byte[] {0x00, 0x60});

  ByteBuffer buffer = ByteBuffer.allocate(1024);
  BerEncoder berEncoder = new BerEncoder();
  berEncoder.encode(builder, buffer);
```

How to decode 
-------------

```java
  byte[] byteArray = BerUtil.hexToByteArray("6F1A840E315041592E5359532E444446"
    + "3031A5088801025F2D02656E9f36020060");

  BerDecoder berDecoder = new BerDecoder();
  BerFrame berFrame = berDecoder.decode(byteArray);

  byte[] tag84 = berFrame.getContent(0x84)
  byte[] tag5F2D = berFrame.getContent(0x5F2D);
```

StreamBerPrinter example
------------------------

```java
  byte[] byteArray = BerUtil.hexToByteArray("6F1A840E315041592E5359532E444446"
    + "3031A5088801025F2D02656E77299f2701009f360200609f2608c2c12b098f3d"
    + "a6e39f10120111258013423a02cfec00000002011400ff9000"); 

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

Download
--------

Download [the latest jar][1] via Maven:
```xml
<dependency>
  <groupId>org.nightcode</groupId>
  <artifactId>bertool</artifactId>
  <version>0.3</version>
</dependency>
```

Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/bertool/issues) or simply drop me a line at <dmitry@nightcode.org>.


 [1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=org.nightcode&a=bertool&v=LATEST
