package org.nightcode.tools.ber;

import java.io.IOException;

/**
 *
 */
public interface BerPrinter {

  void print(BerFrame berFrame) throws IOException;
}
