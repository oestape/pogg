/* JKate
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JKate are based on code by Wim Taymans <wim@fluendo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jkate;

import com.jcraft.jogg.*;

/**
 * Various methods to read data from a bitstream.
 */
public class Bitwise {

  /**
   * Read a number of bytes into a buffer.
   */
  static void readbuf(Buffer opb, byte[] buf, int len)
  {
    for (int i=0; i<len; i++) {
      buf[i] = (byte)opb.read(8);
    }
  }

  /**
   * Read a little endian 32 bit integer.
   */
  static int read32(Buffer opb)
  {
    int value;

    value  = opb.read(8);
    value |= opb.read(8) << 8;
    value |= opb.read(8) << 16;
    value |= opb.read(8) << 24;

    return value;
  }

  /**
   * Read a variable size integer.
   * Format defined in the Kate specification.
   */
  static int read32v(Buffer opb)
  {
    int value;

    value  = opb.read(4);
    if (value == 15) {
      int sign = opb.read(1);
      int bits = opb.read(5)+1;
      value = opb.read(bits);
      if (sign != 0) value = -value;
    }

    return value;
  }

  /**
   * Read a little endian 64 bit integer.
   */
  static long read64(Buffer opb)
  {
    long vl, vh;
    vl = (long)read32(opb);
    vh = (long)read32(opb);
    return vl | (vh<<32);
  }

  /**
   * Read a (possibly multiple) warp.
   * Used to skip over unhandled data from newer (bit still compatible)
   * bitstream versions.
   */
  static int skipWarp(Buffer opb)
  {
    while (true) {
      int bits = read32v(opb);
      if (bits == 0)
        break;
      if (bits < 0)
        return Result.KATE_E_BAD_PACKET;
      opb.adv(bits);
    }

    return 0;
  }

  static private final int fp_bits = (4*8);
  static private final int fp_cuts_bits_bits = 4;
  private static int[] readFixed(Buffer opb, int count) {
    int head = opb.read(fp_cuts_bits_bits);
    int tail = opb.read(fp_cuts_bits_bits);
    int bits = fp_bits - head - tail;
    int values[] = new int[count];
    int n = 0;
    while (count-- > 0) {
      int sign = 0;
      if (head > 0)
        sign = opb.read1();
      int v = opb.read(bits);
      v <<= tail;
      if (sign != 0)
        v = -v;
      values[n++] = v;
    }
    return values;
  }
  static private double fixedToFloat(int v) {
    return ((double)v) / (1<<16);
  }

  /**
   * Read an array of float channels.
   * Float format defined in the Kate specification.
   */
  static double[][] readFloats(Buffer opb, int count, int streams) {
    if (count*streams == 0)
     return null;
    if (streams > 1) {
      if (opb.read1() != 0) {
        count *= streams;
        streams = 1;
      }
    }

    double values[][] = new double[streams][];
    for (int s=0; s<streams; ++s) {
      int ints[] = readFixed(opb, count);
      values[s] = new double[count];
      for (int c=0; c<count; ++c) {
        values[s][c] = fixedToFloat(ints[c]);
      }
    }
    return values;
  }
}
