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

public final class Decode {

  Buffer   opb = new Buffer();
  Info     info;

  public Decode (Info i) {
    info = i;
  }

  /**
   * Decode a text data packet (packet type 0x00), and fills the passed event
   * from the decoded data.
   */
  int decodeTextPacket(Event ev)
  {
    ev.ki = info;

    ev.start = Bitwise.read64(opb);
    ev.duration = Bitwise.read64(opb);
    ev.backlink = Bitwise.read64(opb);

    ev.start_time = granuleDuration(ev.start);
    ev.end_time = ev.start_time+granuleDuration(ev.duration);

    /* text */
    int len = Bitwise.read32(opb);
    ev.text = new byte[len];
    Bitwise.readbuf(opb, ev.text, len);

    /* event ID */
    ev.id = -1;
    if (opb.read1() != 0) {
      ev.id = Bitwise.read32v(opb);
    }

    /* motions */
    ev.motions = null;
    if (opb.read1() != 0) {
      int nmotions = Bitwise.read32v(opb);
      if (nmotions < 0)
        return -1;
      ev.motions = new Motion[nmotions];
      for (int n=0; n<nmotions; ++n) {
        if (opb.read1() != 0) {
          int idx = Bitwise.read32v(opb);
          if (idx < 0 || idx >= info.motions.length)
            return -1;
          ev.motions[n] = info.motions[idx];
        }
        else {
          try {
            ev.motions[n] = info.unpackMotion(opb);
          }
          catch (KateException ke) {
            return Result.KATE_E_BAD_PACKET;
          }
        }
      }
    }

    /* overrides */
    if (opb.read1() != 0) {
      try {
        if (opb.read1() != 0)
          ev.text_encoding = KateTextEncoding.CreateTextEncoding(opb.read(8));
        if (opb.read1() != 0)
          ev.text_directionality = KateTextDirectionality.CreateTextDirectionality(opb.read(8));
      }
      catch (KateException ke) {
        return Result.KATE_E_BAD_PACKET;
      }

      /* language override */
      if (opb.read1() != 0) {
        int nbytes = Bitwise.read32v(opb);
        if (nbytes < 0)
          return Result.KATE_E_BAD_PACKET;
        if (nbytes > 0) {
          ev.language=new byte[nbytes];
          Bitwise.readbuf(opb, ev.language, nbytes);
        }
      }

      /* region override */
      if (opb.read1() != 0) {
        int idx = Bitwise.read32v(opb);
        if (idx < 0 || idx >= info.regions.length)
          return Result.KATE_E_BAD_PACKET;
        ev.kr = info.regions[idx];
      }
      if (opb.read1() != 0) {
        try {
          ev.kr = info.unpackRegion(opb);
        }
        catch (KateException ke) {
          return Result.KATE_E_BAD_PACKET;
        }
      }

      /* style override */
      if (opb.read1() != 0) {
        int idx = Bitwise.read32v(opb);
        if (idx < 0 || idx >= info.styles.length)
          return Result.KATE_E_BAD_PACKET;
        ev.ks = info.styles[idx];
      }
      if (opb.read1() != 0) {
        try {
          ev.ks = info.unpackStyle(opb);
        }
        catch (KateException ke) {
          return Result.KATE_E_BAD_PACKET;
        }
      }
      if (opb.read1() != 0) {
        int idx = Bitwise.read32v(opb);
        if (idx < 0 || idx >= info.styles.length)
          return Result.KATE_E_BAD_PACKET;
        ev.ks2 = info.styles[idx];
      }
      if (opb.read1() != 0) {
        try {
          ev.ks2 = info.unpackStyle(opb);
        }
        catch (KateException ke) {
          return Result.KATE_E_BAD_PACKET;
        }
      }

      /* font mapping */
      if (opb.read1() != 0) {
        int idx = Bitwise.read32v(opb);
        if (idx < 0 || idx >= info.font_mappings.length)
          return Result.KATE_E_BAD_PACKET;
        ev.font_mapping = info.font_mappings[idx];
      }
    }

    /* new in 0.2: palettes/bitmaps/markup type */
    if (((info.bitstream_version_major<<8) | info.bitstream_version_minor) >= 0x0002) {
      Bitwise.read32v(opb);
      if (opb.read1() != 0) {
        if (opb.read1() != 0) {
          int idx = Bitwise.read32v(opb);
          if (idx < 0 || idx >= info.palettes.length)
            return Result.KATE_E_BAD_PACKET;
          ev.palette = info.palettes[idx];
        }
        if (opb.read1() != 0) {
          try {
            ev.palette = info.unpackPalette(opb);
          }
          catch (KateException e) {
            return Result.KATE_E_BAD_PACKET;
          }
        }
        if (opb.read1() != 0) {
          int idx = Bitwise.read32v(opb);
          if (idx < 0 || idx >= info.bitmaps.length)
            return Result.KATE_E_BAD_PACKET;
          ev.bitmap = info.bitmaps[idx];
        }
        if (opb.read1() != 0) {
          try {
            ev.bitmap = info.unpackBitmap(opb);
          }
          catch (KateException e) {
            return Result.KATE_E_BAD_PACKET;
          }
        }
        if (opb.read1() != 0) {
          try {
            ev.markup_type = KateMarkupType.CreateMarkupType(opb.read(8));
          }
          catch (KateException e) {
            return Result.KATE_E_BAD_PACKET;
          }
        }
      }
    }

// TODO: remainder
    return 0;
  }

  /**
   * Convert a granule to a time.
   */
  public double granuleTime(long granulepos)
  {
    if(granulepos>=0){
      long base=granulepos>>info.granule_shift;
      long offset=granulepos-(base<<info.granule_shift);

      return (base+offset)*
        ((double)info.gps_denominator/info.gps_numerator);
    }
    return(-1);
  }

  /**
   * Convert a time in granule units to a duration.
   */
  public double granuleDuration(long granule)
  {
    if(granule>=0){
      return (granule)*
        ((double)info.gps_denominator/info.gps_numerator);
    }
    return(-1);
  }

}
