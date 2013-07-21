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
 * RLE decoding routines.
 */
public class RLE
{
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC = 4;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA = 3;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND = 3;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP = 6;
  private static final int KATE_RLE_RUN_LENGTH_BITS_DELTA = 6;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_START = 9;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_END = 8;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP_START = 8;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA_STOP = 3;
  private static final int KATE_RLE_RUN_LENGTH_BITS_DELTA_STOP = 5;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_ZERO = 8;
  private static final int KATE_RLE_RUN_LENGTH_BITS_BASIC_NON_ZERO = 3;

  private static final int KATE_RLE_TYPE_EMPTY = 0;
  private static final int KATE_RLE_TYPE_BASIC = 1;
  private static final int KATE_RLE_TYPE_DELTA = 2;
  private static final int KATE_RLE_TYPE_BASIC_STOP = 3;
  private static final int KATE_RLE_TYPE_BASIC_STARTEND = 4;
  private static final int KATE_RLE_TYPE_DELTA_STOP = 5;
  private static final int KATE_RLE_TYPE_BASIC_ZERO = 6;

  private static final int KATE_RLE_TYPE_BITS = 3;

  private static int decodeLineEmpty(Buffer opb, int width, byte pixels[], int offset, int bits, byte zero)
  {
    for (int n=0; n<width; ++n)
      pixels[offset+n] = zero;
    return 0;
  }

  private static int decodeLineBasic(Buffer opb, int width, byte pixels[], int offset, int bits, byte zero)
  {
    final int run_length_bits = KATE_RLE_RUN_LENGTH_BITS_BASIC;
    int p = 0;
    int count = width;
    while (count > 0) {
      int run_length = 1+opb.read(run_length_bits);
      if (run_length == 0 || run_length > count)
        return -1;
      byte value = (byte)opb.read(bits);
      for (int n=0; n<run_length; ++n)
        pixels[offset+p++] = value;
      count -= run_length;
    }
    return 0;
  }

  private static int decodeLineDelta(Buffer opb, int width, byte pixels[], int offset, int bits, byte zero)
  {
    final int run_length_delta_bits = KATE_RLE_RUN_LENGTH_BITS_DELTA;
    final int run_length_basic_bits = KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA;
    int p = 0;
    int count = width;
    while (count > 0) {
      int type = opb.read1();
      if (type != 0) {
        int run_length = 1+opb.read(run_length_delta_bits);
        if (run_length == 0 || run_length > count)
          return -1;
        if (offset > 0) {
          for (int n=0; n<run_length; ++n) {
            pixels[offset+p] = pixels[offset+p-width];
            ++p;
          }
        }
        else {
          for (int n=0; n<run_length; ++n)
            pixels[offset+p++] = zero;
        }
        count -= run_length;
      }
      else {
        int run_length = 1 + opb.read(run_length_basic_bits);
        if (run_length == 0 || run_length > count)
          return -1;
        byte value = (byte)opb.read(bits);
        for (int n=0; n<run_length; ++n)
          pixels[offset+p++] = value;
        count -= run_length;
      }
    }
    return 0;
  }

  private static int decodeLineBasicStartEnd(Buffer opb, int width, byte pixels[], int offset, int bits, byte zero)
  {
    final int run_length_bits = KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND;
    int run_length;
    int count = width;
    int p = 0;

    run_length = opb.read(KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_START);
    if (run_length > 0) {
      if (run_length > count)
        return -1;
      for (int n=0; n<run_length; ++n)
        pixels[offset+p++] = zero;
      count -= run_length;
    }

    run_length = opb.read(KATE_RLE_RUN_LENGTH_BITS_BASIC_STARTEND_END);
    if (run_length > 0) {
      if (run_length > count)
        return -1;
      for (int n=0; n<run_length; ++n)
        pixels[offset+width-1-n] = zero;
      count -= run_length;
    }

    while (count > 0) {
      run_length = 1 + opb.read(run_length_bits);
      if (run_length == 0 || run_length > count)
        return -1;
      byte value = (byte)opb.read(bits);
      for (int n=0; n<run_length; ++n)
        pixels[offset+p++] = value;
      count -= run_length;
    }

    return 0;
  }

  private static int decodeLineBasicStop(Buffer opb, int width, byte pixels[], int offset, int bits, byte zero)
  {
    final int run_length_bits = KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP;
    int run_length;
    int count = width;
    int p = 0;

    run_length = opb.read(KATE_RLE_RUN_LENGTH_BITS_BASIC_STOP_START);
    if (run_length > 0) {
      if (run_length > count)
        return -1;
      for (int n=0; n<run_length; ++n)
        pixels[offset+p++] = zero;
      count -= run_length;
    }

    while (count > 0) {
      run_length = opb.read(run_length_bits);
      if (run_length > count)
        return -1;
      if (run_length == 0) {
        for (int n=0; n<run_length; ++n)
          pixels[offset+p++] = zero;
        break;
      }
      byte value = (byte)opb.read(bits);
      for (int n=0; n<run_length; ++n)
        pixels[offset+p++] = value;
      count -= run_length;
    }

    return 0;
  }

  private static int decodeLineDeltaStop(Buffer opb, int width, byte pixels[], int offset, int bits, byte zero)
  {
    final int run_length_delta_bits = KATE_RLE_RUN_LENGTH_BITS_DELTA_STOP;
    final int run_length_basic_bits = KATE_RLE_RUN_LENGTH_BITS_BASIC_IN_DELTA_STOP;
    int run_length;
    int count = width;
    int p = 0;

    while (count > 0) {
      int type = opb.read1();
      if (type != 0) {
        run_length = 1 + opb.read(run_length_delta_bits);
        if (run_length == 0 || run_length > count)
          return -1;
        if (offset > 0) {
          for (int n=0; n<run_length; ++n) {
            pixels[offset+p] = pixels[offset+p-width];
            ++p;
          }
        }
        else {
          for (int n=0; n<run_length; ++n)
            pixels[offset+p++] = zero;
        }
      }
      else {
        run_length = opb.read(run_length_basic_bits);
        if (run_length == 0) {
          for (int n=0; n<run_length; ++n)
            pixels[offset+p++] = zero;
          break;
        }
        if (run_length > count)
          return -1;
        byte value = (byte)opb.read(bits);
        for (int n=0; n<run_length; ++n)
          pixels[offset+p++] = value;
      }
      count -= run_length;
    }

    return 0;
  }

  private static int decodeLineBasicZero(Buffer opb, int width, byte pixels[], int offset, int bits, byte zero)
  {
    final int run_length_bits_zero = KATE_RLE_RUN_LENGTH_BITS_BASIC_ZERO;
    final int run_length_bits_non_zero = KATE_RLE_RUN_LENGTH_BITS_BASIC_NON_ZERO;
    int run_length;
    int count = width;
    int p = 0;

    while (count > 0) {
      byte value = (byte)opb.read(bits);
      if (value == zero) {
        run_length = 1 + opb.read(run_length_bits_zero);
      }
      else {
        run_length = 1 + opb.read(run_length_bits_non_zero);
      }
      if (run_length == 0 || run_length > count)
        return -1;
      for (int n=0; n<run_length; ++n)
        pixels[offset+p++] = value;
      count -= run_length;
    }

    return 0;
  }

  public static byte[] decodeRLE(Buffer opb, int width, int height, int bpp)
  {
    byte[] pixels = new byte[width*height];
    int offset = 0;
    int ret;
    byte zero = (byte)opb.read(bpp);
    while (height > 0) {
      int type = opb.read(KATE_RLE_TYPE_BITS);
      switch (type) {
        case KATE_RLE_TYPE_EMPTY:
          ret = decodeLineEmpty(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_DELTA:
          ret = decodeLineDelta(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC:
          ret = decodeLineBasic(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC_STARTEND:
          ret = decodeLineBasicStartEnd(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC_STOP:
          ret = decodeLineBasicStop(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_DELTA_STOP:
          ret = decodeLineDeltaStop(opb, width, pixels, offset, bpp, zero);
          break;
        case KATE_RLE_TYPE_BASIC_ZERO:
          ret = decodeLineBasicZero(opb, width, pixels, offset, bpp, zero);
          break;
        default:
          return null;
      }
      if (ret != 0)
        return null;
      offset += width;
      --height;
    }
    return pixels;
  }
}
