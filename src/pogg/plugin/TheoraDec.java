/* Copyright (C) <2010> Octavi Estape <octavi.pogg@gmail.com>
 * Based on com.fluendo.examples.DumpVideo:
 * Copyright (C) <2009> Maik Merten <maikmerten@googlemail.com>
 * and com.fluendo.plugin.TheoraDec:
 * Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

//octavi package com.fluendo.plugin;
package pogg.plugin; //octavi

import java.util.*;
import com.jcraft.jogg.*;
import com.fluendo.jheora.*;
//octavi import com.fluendo.jst.*;
import com.fluendo.utils.*;

public class TheoraDec // extends Element implements OggPayload  //octavi
{
  public static final Integer OK = new Integer(0); //octavi
  public static final Integer ERROR = new Integer(-5); //octavi

  private static final byte[] signature = { -128, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61 };

  private Info ti;
  private Comment tc;
  private State ts;
//octavi  private Packet op;
  private int packet;
  private YUVBuffer yuv;

  private long lastTs; //octavi no
  private boolean needKeyframe;
  private boolean haveDecoder = false;
  public float fps = -1; //octavi

  /* 
   * OggPayload interface
   */
  static public boolean isType (Packet op) //octavi si equal to isTheora
  {
    return typeFind (op.packet_base, op.packet, op.bytes) > 0;
  }
  public int takeHeader (Packet op) //octavi si
  {
    int ret;
    byte header;
    ret = ti.decodeHeader(tc, op);
    header = op.packet_base[op.packet];
    if (header == -126) {
      ts.decodeInit(ti);
      haveDecoder = true;
    }
    return ret;
  }
  public boolean isHeader (Packet op) //octavi si
  {
    return (op.packet_base[op.packet] & 0x80) == 0x80;
  }
  public boolean isKeyFrame (Packet op) //octavi si
  {
    return ts.isKeyframe(op);
  }
  public boolean isDiscontinuous ()
  {
    return false;
  }
  
  /*octavi
  public long getFirstTs (Vector packets)
  {
    int len = packets.size();
    int i;
    long time;
    com.fluendo.jst.Buffer data = null;

    /* first find buffer with valid offset */
  /*octavi
    for (i=0; i<len; i++) {
      data = (com.fluendo.jst.Buffer) packets.elementAt(i);

      if (data.time_offset != -1)
        break;
    }
    if (i == packets.size())
      return -1;

    time = granuleToTime (data.time_offset);

    data = (com.fluendo.jst.Buffer) packets.elementAt(0);
    data.timestamp = time - (long) ((i+1) * (Clock.SECOND * ti.fps_denominator / ti.fps_numerator));

    return time;
  }
  */
  public long granuleToTime (long gp)
  {
    long res;

    if (gp < 0 || !haveDecoder)
      return -1;

//octavi    res = (long) (ts.granuleTime(gp) * Clock.SECOND);
    res = (long) (ts.granuleTime(gp) * 1000000); //octavi

    return res;
  }
/* octavi
  private Pad srcPad = new Pad(Pad.SRC, "src") {
    protected boolean eventFunc (com.fluendo.jst.Event event) {
      return sinkPad.pushEvent(event);
    }
  };

  private Pad sinkPad = new Pad(Pad.SINK, "sink") {
    protected boolean eventFunc (com.fluendo.jst.Event event) {
      boolean result;

      switch (event.getType()) {
        case com.fluendo.jst.Event.FLUSH_START:
	  result = srcPad.pushEvent (event);
	  synchronized (streamLock) {
            Debug.log(Debug.DEBUG, "synced "+this);
	  }
          break;
        case com.fluendo.jst.Event.FLUSH_STOP:
          result = srcPad.pushEvent(event);
          break;
        case com.fluendo.jst.Event.EOS:
          Debug.log(Debug.INFO, "got EOS "+this);
          result = srcPad.pushEvent(event);
          break;
        case com.fluendo.jst.Event.NEWSEGMENT:
	default:
          result = srcPad.pushEvent(event);
          break;
      }
      return result;
    }
    */

    public Object decode(Packet op) { //octavi
//octavi    protected int chainFunc (com.fluendo.jst.Buffer buf) { //octavi similar to decode

      Object result = OK; //octavi
      
// octavi        int result;
// octavi      long timestamp;
      long timestamp = -1; //octavi
/*octavi
      Debug.log( Debug.DEBUG, parent.getName() + " <<< " + buf );
//octavi this is done by stream.packetout() 
      op.packet_base = buf.data;
      op.packet = buf.offset;
      op.bytes = buf.length;
      op.b_o_s = (packet == 0 ? 1 : 0);
      op.e_o_s = 0;
      op.packetno = packet;
      timestamp = buf.timestamp;

      if (buf.isFlagSet (com.fluendo.jst.Buffer.FLAG_DISCONT)) {
        Debug.log(Debug.INFO, "theora: got discont");
        needKeyframe = true;
	lastTs = -1;
      }
*/
      if (packet < 3) {
        //System.out.println ("decoding header");
        if (takeHeader(op) < 0){
//octavi          buf.free();
          // error case; not a theora header
          Debug.log(Debug.ERROR, "does not contain Theora video data.");
          return ERROR;
        }
        if (packet == 2) {
          ts.decodeInit(ti);
    
          Debug.log(Debug.INFO, "theora dimension: "+ti.width+"x"+ti.height);
          if (ti.aspect_denominator == 0) {
            ti.aspect_numerator = 1;
            ti.aspect_denominator = 1;
          }
          Debug.log(Debug.INFO, "theora offset: "+ti.offset_x+","+ti.offset_y);
          Debug.log(Debug.INFO, "theora frame: "+ti.frame_width+","+ti.frame_height);
          Debug.log(Debug.INFO, "theora aspect: "+ti.aspect_numerator+"/"+ti.aspect_denominator);
          this.fps = ti.fps_numerator/ti.fps_denominator; //octavi
          Debug.log(Debug.INFO, "theora framerate: "+ti.fps_numerator+"/"+ti.fps_denominator);
/* octavi
	  caps = new Caps ("video/raw");
	  caps.setFieldInt ("width", ti.frame_width);
	  caps.setFieldInt ("height", ti.frame_height);
	  caps.setFieldInt ("aspect_x", ti.aspect_numerator);
	  caps.setFieldInt ("aspect_y", ti.aspect_denominator);
*/
        }
//octavi        buf.free();
        packet++;

	return OK;
      }
      else {
        if ((op.packet_base[op.packet] & 0x80) == 0x80) {
          Debug.log(Debug.INFO, "ignoring header");
          return OK;
        }
        if (needKeyframe && ts.isKeyframe(op)) {
	  needKeyframe = false;
        }

/*octavi no*/
	if (timestamp != -1) {
	  lastTs = timestamp;
	}
	else if (lastTs != -1) {
	  long add;

//octavi add = (Clock.SECOND * ti.fps_denominator) / ti.fps_numerator;
          add = (1000000 * ti.fps_denominator) / ti.fps_numerator; //octavi
	  lastTs += add;
	  timestamp = lastTs;
	}
/*octavi end*/
	
	if (!needKeyframe) {
	  try{
            if (ts.decodePacketin(op) != 0) {
              Debug.log(Debug.ERROR, "Bad Theora packet. Most likely not fatal, hoping for better luck next packet.");
            }
            if (ts.decodeYUVout(yuv) != 0) {
//octavi              buf.free();
//octavi	      postMessage (Message.newError (this, "Error getting the Theora picture"));
              Debug.log(Debug.ERROR, "Error getting the picture.");
              return ERROR;
	    }
            return yuv.getObject(ti.offset_x, ti.offset_y, ti.frame_width, ti.frame_height); //octavi
/*octavi
            buf.object = yuv.getObject(ti.offset_x, ti.offset_y, ti.frame_width, ti.frame_height);
	    buf.caps = caps;
	    buf.timestamp = timestamp;
            Debug.log( Debug.DEBUG, parent.getName() + " >>> " + buf );
            result = srcPad.push(buf);
*/
	  }
	  catch (Exception e) {
	    e.printStackTrace();
//octavi	    postMessage (Message.newError (this, e.getMessage()));
            result = ERROR;
	  }
	}
        else {
          result = OK;
//octavi	  buf.free();
	}
      }
      packet++;

      return result;
    }

    /*octavi
    protected boolean activateFunc (int mode)
    {
      return true;
    }
  };
  */

  public TheoraDec() { //octavi si
    super();

    ti = new Info();
    tc = new Comment();
    ts = new State();
    yuv = new YUVBuffer();
//octavi    op = new Packet();

//octavi    addPad (srcPad);
//octavi    addPad (sinkPad);
  }

  /*octavi
  protected int changeState (int transition) {
    int res;

    switch (transition) {
      case STOP_PAUSE:
        lastTs = -1;
        packet = 0;
        needKeyframe = true;
	break;
      default:
        break;
    }

    res = super.changeState (transition);

    switch (transition) {
      case PAUSE_STOP:
	ti.clear();
	tc.clear();
	ts.clear();
	break;
      default:
        break;
    }

    return res;
  }
*/
  public String getFactoryName ()
  {
    return "theoradec";
  }
  public String getMime ()
  {
    return "video/x-theora";
  }

  static public int typeFind (byte[] data, int offset, int length) //octavi si
  {
    if (MemUtils.startsWith (data, offset, length, signature))
      return 10;
    return -1;
  }
}
