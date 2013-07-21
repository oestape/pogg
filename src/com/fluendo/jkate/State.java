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
import com.fluendo.utils.*;

public class State 
{
  long granulepos;

  private Decode dec;
  private Event ev = null;

  public void clear()
  {
  }

  /**
   * Initialize for decoding.
   */
  public int decodeInit(Info ki)
  {
    dec = new Decode(ki);
    granulepos=-1;

    return(0);
  }

  /**
   * Decode a Kate data packet.
   * Headers are supposed to have been parsed already.
   * An event may be generated, and will then be available from decodeEventOut.
   */
  public int decodePacketin (Packet op)
  {
    long ret;
    byte type;

    ev = null;

    dec.opb.readinit(op.packet_base, op.packet, op.bytes);

    /* get packet type */
    type = (byte)dec.opb.read(8);

    /* ignore headers */
    if ((type & 0x80) != 0)
      return 0;

    switch (type) {
      case 0x00:
        ev = new Event(dec.info);
        ret = dec.decodeTextPacket(ev);

        if(ret < 0) {
          ev = null;
          return (int) ret;
        }
 
        if(op.granulepos>-1)
          granulepos=op.granulepos;
        else{
          if(granulepos==-1){
            granulepos=0;
          } 
        }
  
        return 0;

      case 0x01:
        /* keepalive */
        return 0;

      case 0x7f:
        /* eos */
        return 1;

      default:
        Debug.debug("Kate packet type "+type+" ignored");
        return 0;
    }
  }

  /**
   * Returns the event (if any) generated from the last decoded packet.
   */
  public Event decodeEventOut ()
  {
    return ev;
  }

  /**
   * Returns, in seconds, absolute time of current packet in given
   * logical stream
   */
  public double granuleTime(long granulepos)
  {
    return dec.granuleTime(granulepos);
  }

  /**
   * Returns, in seconds, duration in granule units
   */
  public double granuleDuration(long granulepos)
  {
    return dec.granuleDuration(granulepos);
  }
}
