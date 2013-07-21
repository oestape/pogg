/* Copyright (C) <2010> Octavi Estape <octavi.pogg@gmail.com>
 **Based on DumpVideo:
 * Copyright (C) <2009> Maik Merten <maikmerten@googlemail.com>
 * Copyright (C) <2004> Wim Taymans <wim@fluendo.com> (TheoraDec.java parts)
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
package com.fluendo.examples;

import com.fluendo.jheora.Comment;
import com.fluendo.jheora.Info;
import com.fluendo.jheora.State;
import com.fluendo.jheora.YUVBuffer;
import com.fluendo.utils.Debug;
import com.fluendo.utils.MemUtils;
import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


/**
 * This class borrows code from TheoraDec.java
 */
public class DumpVideo2 {

    public static final Integer OK = new Integer(0);
    public static final Integer ERROR = new Integer(-5);
    private static final byte[] signature = {-128, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61};

    private class TheoraDecoder {

        private Info ti;
        private Comment tc;
        private State ts;
        private YUVBuffer yuv;
        private int packet;
        private boolean needKeyframe;

        public TheoraDecoder() {
            super();
            ti = new Info();
            tc = new Comment();
            ts = new State();
            yuv = new YUVBuffer();
        }

        public int takeHeader(Packet op) {
            int ret;
            byte header;
            ret = ti.decodeHeader(tc, op);
            header = op.packet_base[op.packet];
            if (header == -126) {
                ts.decodeInit(ti);
            }
            return ret;
        }

        public boolean isHeader(Packet op) {
            return (op.packet_base[op.packet] & 0x80) == 0x80;
        }

        public boolean isKeyFrame(Packet op) {
            return ts.isKeyframe(op);
        }

        public Object decode(Packet op) {

            Object result = OK;


            if (packet < 3) {
                //System.out.println ("decoding header");
                if (takeHeader(op) < 0) {
                    // error case; not a theora header
                    Debug.log(Debug.ERROR, "does not contain Theora video data.");
                    return ERROR;
                }
                if (packet == 2) {
                    ts.decodeInit(ti);

                    Debug.log(Debug.INFO, "theora dimension: " + ti.width + "x" + ti.height);
                    if (ti.aspect_denominator == 0) {
                        ti.aspect_numerator = 1;
                        ti.aspect_denominator = 1;
                    }
                    Debug.log(Debug.INFO, "theora offset: " + ti.offset_x + "," + ti.offset_y);
                    Debug.log(Debug.INFO, "theora frame: " + ti.frame_width + "," + ti.frame_height);
                    Debug.log(Debug.INFO, "theora aspect: " + ti.aspect_numerator + "/" + ti.aspect_denominator);
                    Debug.log(Debug.INFO, "theora framerate: " + ti.fps_numerator + "/" + ti.fps_denominator);

                }
                packet++;

                return OK;
            } else {
                if ((op.packet_base[op.packet] & 0x80) == 0x80) {
                    Debug.log(Debug.INFO, "ignoring header");
                    return OK;
                }
                if (needKeyframe && ts.isKeyframe(op)) {
                    needKeyframe = false;
                }


                if (!needKeyframe) {
                    try {
                        if (ts.decodePacketin(op) != 0) {
                            Debug.log(Debug.ERROR, "Bad Theora packet. Most likely not fatal, hoping for better luck next packet.");
                        }
                        if (ts.decodeYUVout(yuv) != 0) {
                            Debug.log(Debug.ERROR, "Error getting the picture.");
                            return ERROR;
                        }
                        return yuv.getObject(ti.offset_x, ti.offset_y, ti.frame_width, ti.frame_height);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result = ERROR;
                    }
                } else {
                    result = OK;
                }
            }
            packet++;

            return result;
        }
    }

    private class PPMWriter implements ImageConsumer {

        private OutputStream os;
        private byte[] pixels = null;

        public PPMWriter(File outfile) {
            try {
                os = new FileOutputStream(outfile);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        public void writePPMFrame(Info ti, YUVBuffer yuv) {
            try {
                String headerstring = "P6\n# Converted from Theora\n" + ti.width + " " + ti.height + "\n255\n";
                os.write(headerstring.getBytes());

                if (pixels == null || pixels.length != yuv.y_width * yuv.y_height*3) {
                    pixels = new byte[yuv.y_width * yuv.y_height * 3];
                }

                ImageConsumer ic = this;
                yuv.startProduction(ic);

                 /*
                ic.setColorModel(colorModel);
                ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
                ImageConsumer.COMPLETESCANLINES |
                ImageConsumer.SINGLEFRAME |
                ImageConsumer.SINGLEPASS);
                ic.setDimensions(y_width, y_height);
                prepareRGBData(0, 0, y_width, y_height);
                ic.setPixels(0, 0, y_width, y_height, colorModel, pixels, 0, y_width);
                ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
                  */

                os.write(pixels);



            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        public void imageComplete(int status) {

        }
        public void setColorModel(ColorModel model) {}
        public void setDimensions(int width, int height) {}
        public void setHints(int hintflags) { }
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
            //this.pixels = pixels;
        }
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pix, int off, int scansize) {
            int pos = 0;
            for(int i=0; i<pix.length; i++) {
                int myColor = pix[i];
                byte r = (byte)(myColor >> 16 & 0xFF);
                byte g = (byte)(myColor >> 8 & 0xFF);
                byte b = (byte)(myColor & 0xFF);
                this.pixels[pos] = r;
                pos++;
                this.pixels[pos] = g;
                pos++;
                this.pixels[pos] = b;
                pos++;
            }
        }
        public void setProperties(Hashtable props) {}

    }

    public boolean isTheora(Packet op) {
        return typeFind(op.packet_base, op.packet, op.bytes) > 0;
    }

    public int typeFind(byte[] data, int offset, int length) {
        if (MemUtils.startsWith(data, offset, length, signature)) {
            return 10;
        }
        return -1;
    }

    public void dumpVideo(File videofile, String outprefix) throws IOException {
        InputStream is = new FileInputStream(videofile);

        SyncState oy = new SyncState();
        Page og = new Page();
        Packet op = new Packet();
        byte[] buf = new byte[512];

        Map streamstates = new HashMap();
        Map theoradecoders = new HashMap();
        Set hasdecoder = new HashSet();

        int frames = 0;

        int read = is.read(buf);
        while (read > 0) {
            int offset = oy.buffer(read);
            java.lang.System.arraycopy(buf, 0, oy.data, offset, read);
            oy.wrote(read);

            while (oy.pageout(og) == 1) {

                Integer serialno = new Integer(og.serialno());

                StreamState state = (StreamState) streamstates.get(serialno);
                if (state == null) {
                    state = new StreamState();
                    state.init(serialno.intValue());
                    streamstates.put(serialno, state);
                    Debug.info("created StreamState for stream no. " + og.serialno());
                }

                state.pagein(og);



                while (state.packetout(op) == 1) {

                    if (!(hasdecoder.contains(serialno)) && isTheora(op)) {

                        TheoraDecoder theoradec = (TheoraDecoder) theoradecoders.get(serialno);
                        if (theoradec == null) {
                            theoradec = new TheoraDecoder();
                            theoradecoders.put(serialno, theoradec);
                            hasdecoder.add(serialno);
                        }

                        Debug.info("is Theora: " + serialno);
                    }

                    TheoraDecoder theoradec = (TheoraDecoder) theoradecoders.get(serialno);

                    if (theoradec != null) {
                        Object result = theoradec.decode(op);
                        if (result instanceof YUVBuffer) {
                            Debug.info("got frame " + ++frames);
                            String framesStr = "0000"+frames;
                            framesStr = framesStr.substring(framesStr.length()-5,framesStr.length());

                            PPMWriter ppmwriter = new PPMWriter(new File(outprefix+framesStr+".ppm"));

                            YUVBuffer yuvbuf = (YUVBuffer) result;
                            ppmwriter.writePPMFrame(theoradec.ti, yuvbuf);

                        }
                    }
                }
            }

            read = is.read(buf);
        }

    }

    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.err.println("Dumps a Theora video to Portable PixMap (PPM) files");
            System.err.println("usage: DumpVideo2 <videofile> <prefix>");
            System.exit(1);
        }

        File infile = new File(args[0]);

        String outprefix = args[1];

        DumpVideo2 dv = new DumpVideo2();
        dv.dumpVideo(infile, outprefix);

    }
}
