# BerTool 

[![Build Status](https://travis-ci.org/nightcode/bertool.svg?branch=master)](https://travis-ci.org/nightcode/bertool)
[![Coverage Status](https://coveralls.io/repos/nightcode/bertool/badge.svg?branch=master&service=github)](https://coveralls.io/github/nightcode/bertool?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/org.nightcode/bertool.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.nightcode%20bertool)

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
  BerBuilder builder = BerBuilder.newInstance()
        .add(0x6F, BerBuilder.newInstance()
            .addHexString(0x84, "315041592E5359532E4444463031")
            .add(0xA5, BerBuilder.newInstance()
                .add(0x88, new byte[] {0x02})
                .addAsciiString(0x5F2D, "en")))
        .add(0x9F36, new byte[] {0x00, 0x60});

  // write to OutputStream
  OutputStream out = new ByteArrayOutputStream();
  builder.writeTo(out);

  // or to ByteBuffer
  ByteBuffer buffer = ByteBuffer.allocate(builder.length());
  builder.writeTo(buffer);
```

How to decode 
-------------

```java
  byte[] byteArray = BerUtil.hexToByteArray("6F1A840E315041592E5359532E444446"
    + "3031A5088801025F2D02656E9f36020060");

  BerFrame berFrame = BerFrame.parseFrom(byteArray);

  byte[] tag84 = berFrame.getContent(0x84)
  byte[] tag5F2D = berFrame.getContent(0x5F2D);
```

StreamBerPrinter example
------------------------

with DefaultBerFormatter

```java
  byte[] byteArray = BerUtil.hexToByteArray("6F1A840E315041592E5359532E444446"
    + "3031A5088801025F2D02656E77299f2701009f360200609f2608c2c12b098f3d"
    + "a6e39f10120111258013423a02cfec00000002011400ff9000"); 

  BerFrame berFrame = BerFrame.parseFrom(byteArray);

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

with EmvBerFormatter

```java
  byte[] byteArray = BerUtil.hexToByteArray("6F1A840E315041592E5359532E444446"
    + "3031A5088801025F2D02656E77299f2701009f360200609f2608c2c12b098f3d"
    + "a6e39f10120111258013423a02cfec00000002011400ff9000"); 

  BerFrame berFrame = BerFrame.parseFrom(byteArray);

  BerPrinter printer = new StreamBerPrinter(System.out
      , EmvBerFormatter.newInstanceWithSpaces());
  printer.print(berFrame);
```

output

```
 ├─[6F] File Control Information (FCI) Template
 │  │ 84 0E 31 50 41 59 2E 53  59 53 2E 44 44 46 30 31
 │  │ A5 08 88 01 02 5F 2D 02  65 6E
 │  ├─[84] Dedicated File (DF) Name
 │  │   31 50 41 59 2E 53 59 53  2E 44 44 46 30 31
 │  └─[A5] File Control Information (FCI) Proprietary Template
 │     │ 88 01 02 5F 2D 02 65 6E
 │     ├─[88] Short File Identifier (SFI)
 │     │   02
 │     └─[5F2D] Language Preference
 │         65 6E
 ├─[77] Response Message Template Format 2
 │  │ 9F 27 01 00 9F 36 02 00  60 9F 26 08 C2 C1 2B 09
 │  │ 8F 3D A6 E3 9F 10 12 01  11 25 80 13 42 3A 02 CF
 │  │ EC 00 00 00 02 01 14 00  FF
 │  ├─[9F27] Cryptogram Information Data
 │  │   00
 │  ├─[9F36] Application Transaction Counter (ATC)
 │  │   00 60
 │  ├─[9F26] Application Cryptogram
 │  │   C2 C1 2B 09 8F 3D A6 E3
 │  └─[9F10] Issuer Application Data
 │      01 11 25 80 13 42 3A 02  CF EC 00 00 00 02 01 14
 │      00 FF
 └─[90] Issuer Public Key Certificate
```

Download
--------

Download [the latest jar][1] via Maven:
```xml
<dependency>
  <groupId>org.nightcode</groupId>
  <artifactId>bertool</artifactId>
  <version>0.5</version>
</dependency>
```

Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/bertool/issues) or simply drop me a line at <dmitry@nightcode.org>.


 [1]: http://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=org.nightcode&a=bertool&v=LATEST
