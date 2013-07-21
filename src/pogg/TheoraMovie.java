/*
  Theora (Cortado) wrapper for Processing
  
  Copyright (c) 2010 Octavi Estape <octavi.pogg@gmail.com>
  
  Based on processing.video.Movie by Ben Fry and Casey Reas
  and on com.fluendo.examples.DumpVideo by Maik Merten
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */


package pogg;

import pogg.plugin.HTTPSrc;
import pogg.plugin.TheoraDec;
import processing.core.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;

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
import java.awt.image.ImageProducer;
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
 * This class implements the methods in processing.video.Movie
 * but for Theora ogg files (using Cortado) instead of mov files.
 * 
 * @author Octavi Estape
 * 
 */
public class TheoraMovie extends PImage implements PConstants {
    // no longer needing a reference to the parent because PImage has one
    //PApplet parent;

    Method movieEventMethod;

    boolean play;
    boolean repeat;
    boolean available;

    boolean stopCalled = false;
    
    private String filename; 
    private boolean readCalled = false;
    
    private float frameSeconds =0;
    
    /**
     * The frames per second this movie was recorded at. The movie should be played at the same frameRate.
     */
    public float fps = 30;
    
    private TheoraMovieImageConsumer imageConsumer;

    public TheoraMovie(String filename) {
        this(null, filename);
    }

    public TheoraMovie(PApplet parent, String filename) {
        // this creates a fake image so that the first time this
        // attempts to draw, something happens that's not an exception
        super(1, 1, RGB);

        // http://dev.processing.org/bugs/show_bug.cgi?id=882
//      SwingUtilities.invokeLater(new Runnable() {
//          public void run() {
                init(parent, filename);
//          }
//      });
    }


    private void init(PApplet parent, String filename) {
        this.imageConsumer = new TheoraMovieImageConsumer(this);
        this.parent = parent;
        this.filename = filename;

        // first check to see if this can be read locally from a file.
        // otherwise, will have to load the file into memory, which is
        // gonna make people unhappy who are trying to play back 50 MB
        // movies with a locally installed piece exported
        // as an application.
        try {
        	
            // register methods
            if(parent != null) {
                parent.registerDispose(this);
                try {
                    movieEventMethod =
                      parent.getClass().getMethod("movieEvent",
                      new Class[] { TheoraMovie.class });
                } catch (Exception e) {
                    // no such method, or an error.. which is fine, just ignore
                }
            }

        } catch (Exception e) {
          e.printStackTrace();
        }
        pixels = new int[0];
        this.width = 0;
        this.height = 0;
        read();
        
        final InputStream is = createInputStream();
        if(is == null) {
            Debug.error("Could not find "+filename);
            if (parent != null) {
               parent.die("Could not find movie file " + filename, null);
            } else {
                throw new RuntimeException("Could not find movie file "+filename);
            }
            return;
        }

        Thread t = new Thread(new Runnable() { public void run() {internalRun(is);}});
        t.start();
        try {
            synchronized(this) {
                while(this.width==0 && this.height==0) {
                    wait();
                }
                Debug.debug("Width="+this.width+" Height="+this.height);
            }
        } catch (InterruptedException e) { }
   }


    private InputStream createInputStream() {
        InputStream videofile = null;
        try {
            
            if(filename.contains("http://")) {
                Debug.debug("Trying HTTPSrc");
                HTTPSrc httpsrc = new HTTPSrc();
                videofile = httpsrc.getInputStreamAtPosition(new URL(filename), 0);               
            }
            
            if(videofile==null) {
                Debug.debug("Trying inside jar");
                String filePath="/data/"+filename;
                try{
                    InputStream is = getClass().getResourceAsStream(filePath);
                    Debug.debug("InputStream "+is);
                    videofile = is;
                }catch(Exception ex){
                    Debug.error("Exception reading jar", ex);
                }
            }
            
            if(videofile == null && parent!=null) {
                Debug.debug("Trying file in dataPath");
                // first try a local file using the dataPath. usually this will
                // work ok, but sometimes the dataPath is inside a jar file,
                // which is less fun, so this will crap out.
                File file = new File(parent.dataPath(filename));
                //File file = new File("data", filename);
                //File file = new File(filename);
                if (file.exists()) {
                    Debug.debug("Found "+file);
                    videofile = new FileInputStream(file);
                }
            }
            if(videofile==null){
                Debug.debug("Trying file");
                   
                File file = new File(filename);
                if(file.exists()) {
                    Debug.debug("Found "+file);
                    videofile = new FileInputStream(file);   
                }
            }
            
            
            if(videofile==null && parent!=null){
                Debug.debug("Trying input raw");
                videofile = parent.createInputRaw(filename);
            }
        } catch (Exception ex) {
            Debug.error("Exception", ex);
        } 
        
         return videofile;
    }

    /**
     * Checks if there is an available frame to be read.
     * @return true if there is an available frame.
     */
    public boolean available() {
        return available;
    }

    /**
     * Allows a new frame to be read
     */
    public void read() {
        synchronized(this) {
            //Debug.debug("Read");
            readCalled = true;
            notifyAll();
        }
    }


    /**
     * THIS METHOD IS HERE JUST FOR COMPATIBILITY WITH processing.video.Movie
     * BUT IT DOES NOTHING
     * 
     * Begin playing the movie, with no repeat.
     */
    public void play() {
        play = true;
    }


    /**
     * Begin playing the movie, with repeat.
     */
    public void loop() {
        play();
        repeat = true;
    }


    /**
     * Shut off the repeating loop.
     */
    public void noLoop() {
        repeat = false;
    }


    /**
     * THIS METHOD IS HERE JUST FOR COMPATIBILITY WITH processing.video.Movie
     * BUT IT DOES NOTHING
     * 
     * Pause the movie at its current time.
     */
    public void pause() {
        play = false;
        //System.out.println("pause");
    }


    /**
     * Stop the movie, and rewind.
     */
     public void stop() {
        synchronized(this) {
            stopCalled = true;
            notifyAll();
        }
     }


    /**
     * THIS METHOD IS HERE JUST FOR COMPATIBILITY WITH processing.video.Movie
     * BUT IT DOES NOTHING
     * 
     * Set a multiplier for how fast/slow the movie should be run.
     * The default is 1.0.
     * <UL>
     * <LI>speed(2) will play the movie at double speed (2x).
     * <LI>speed(0.5) will play at half speed.
     * <LI>speed(-1) will play backwards at regular speed.
     * </UL>
     */
    public void speed(float rate) {
        //todo
    }


    /**
     * Return the current time in seconds.
     * The number is a float so fractions of seconds can be used.
     */
    public float time() {
        return frameSeconds;
    }


  /**
     * THIS METHOD IS HERE JUST FOR COMPATIBILITY WITH processing.video.Movie
     * BUT IT DOES NOTHING
     * 
     * Jump to a specific location (in seconds).
     * The number is a float so fractions of seconds can be used.
     */
    public void jump(float where) {
        //todo
    }


    /**
     * THIS METHOD IS HERE JUST FOR COMPATIBILITY WITH processing.video.Movie
     * BUT IT DOES NOTHING
     * 
     * Get the full length of this movie (in seconds).
     */
    public float duration() {
        //todo
        return -1;
    }

  
    private void internalRun(InputStream is) {
        synchronized(this) {
            do {
                stopCalled = false;
                Debug.debug("Starting run for "+filename);
                if(is==null) {
                    is = createInputStream();
                }
                if(is == null) {
                    Debug.error("Could not find "+filename);
                    if (parent != null) {
                       parent.die("Could not find movie file " + filename, null);
                    } else {
                        throw new RuntimeException("Could not find movie file "+filename);
                    }
                    return;
                }
                
                try {

          
                    SyncState oy = new SyncState();
                    Page og = new Page();
                    Packet op = new Packet();
                    byte[] buf = new byte[512];
              
                    Map streamstates = new HashMap();
                    Map theoradecoders = new HashMap();
                    Set hasdecoder = new HashSet();
              
                    int frames = 0;
              
                    int read = is.read(buf);
                    while (read > 0 && !stopCalled) {
                        int offset = oy.buffer(read);
                        java.lang.System.arraycopy(buf, 0, oy.data, offset, read);
                        oy.wrote(read);
                        
                        //Debug.debug("Checking pageout");
              
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
              
              
                            //Debug.debug("Checking packetout "+serialno);
                            while (state.packetout(op) == 1) {
                                //Debug.debug("OK packetout "+serialno);
              
                                if (!(hasdecoder.contains(serialno)) && TheoraDec.isType(op)) {
              
                                    TheoraDec theoradec = (TheoraDec) theoradecoders.get(serialno);
                                    if (theoradec == null) {
                                        theoradec = new TheoraDec();
                                        theoradecoders.put(serialno, theoradec);
                                        hasdecoder.add(serialno);
                                    }
              
                                    Debug.info("is Theora: " + serialno);
                                }
              
                                TheoraDec theoradec = (TheoraDec) theoradecoders.get(serialno);
              
                                if (theoradec != null) {
                                    long granule = og.granulepos();
                                    frameSeconds = theoradec.granuleToTime(granule)/1000000F;
                                    //Debug.debug("Seconds="+frameSeconds);
                                    
                                    Object result = theoradec.decode(op);
                                    this.fps = theoradec.fps;
                                    if (result instanceof ImageProducer) { //it can be a YUVBuffer or a FilteredImageSource (if it is cropped)
                                        frames++;
    
              
                                        ImageProducer yuvbuf = (ImageProducer) result;
    
                                        try {
                                            available = true;
                                            while(!readCalled && !stopCalled) {
                                                //Debug.debug("Waiting");
                                                wait();
                                                //Debug.debug("End waiting");
                                            }
                                            readCalled = false;
                                            available = false;
                                        } catch (InterruptedException e) { }
                                        
                                        //Debug.debug("Writing frame "+frames);
                                            writeFrame(yuvbuf);
     
                                            notifyAll(); //for the initialization
    
                                        //this does not work well
                                        /*if (movieEventMethod == null) {
                                            // If no special handling, then automatically read from the movie.
                                            //read();
                                            notifyAll(); //for the initialization
    
                                        } else {
                                            try {
                                                movieEventMethod.invoke(parent, new Object[] { this });
                                            } catch (Exception e) {
                                                Debug.error("error, disabling movieEvent() for " +
                                                                 filename, e);
                                                movieEventMethod = null;
                                            }
                                        }
                                        */
    
                                    } else if(result == TheoraDec.ERROR) {
                                    	Debug.error("got error");
                                    } else {
                                        Debug.debug("result type "+result.getClass());
                                        
                                    }
                                }
                            }
                            //Debug.debug("Ended packetout");
                        }
                        //Debug.debug("Ended Pageout");
                        read = is.read(buf);
                    }
                    Debug.debug("Ended read");
                    is.close();
                    is = null;
                } catch (Exception ex) {
                    Debug.error("Exception in run", ex);
                }
            } while (repeat);
            Debug.debug("Ending run");
        }

    }

    /**
     * Call this to halt the movie from running, and stop its thread.
     */
    public void dispose() {
        stop();
    }

    
    private void writeFrame(ImageProducer yuv) {
        
        loadPixels();

        /*
        if (pixels == null || pixels.length != yuv.y_width * yuv.y_height*3) {
            pixels = new byte[yuv.y_width * yuv.y_height * 3];
        }
        */

        ImageConsumer ic = imageConsumer;
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

        updatePixels();

    }

    private class TheoraMovieImageConsumer implements ImageConsumer {
        TheoraMovie movie;
        
        public TheoraMovieImageConsumer(TheoraMovie movie) {
            this.movie = movie;
        }
        
        public void imageComplete(int status) {
    
        }
        public void setColorModel(ColorModel model) {}
        public void setDimensions(int width, int height) {
            if(movie.pixels==null || movie.pixels.length != width*height) {
                Debug.debug("Setting pixels. Old="+movie.pixels+" New: width="+width+" height="+height);
                movie.pixels = new int[width*height];
                movie.width = width;
                movie.height = height;
                 Debug.debug("Pixels set");
            }
        }
        public void setHints(int hintflags) { }
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
            //this.pixels = pixels;
        }
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pix, int off, int scansize) {
            //Debug.debug("setPixels x="+x+" y="+y+" w="+w+" h="+h+" pix.length="+pix.length+" off="+off+" scansize="+scansize);
            //System.arraycopy(pix, 0, pixels, 0, pix.length); //if the image is cropped pix.lenght > w*h
            System.arraycopy(pix, 0, movie.pixels, 0, w*h);
    
        }
        public void setProperties(Hashtable props) {}
    }
    
    /**
     * Enables or disables the debugging information
     * @param printLogs if it is true, the debugging information would be printed.
     */
    static public void printLogs(boolean printLogs) {
        if(printLogs) {
            Debug.setLevel(Debug.DEBUG);
        } else {
            Debug.setLevel(Debug.NOLOG);
        }
    }
 


}


