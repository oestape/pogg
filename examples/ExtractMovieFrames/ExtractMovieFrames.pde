/**
 * ExtractMovieFrames 
 * 
 * Example for the pogg library by Octavi Estape.
 * It plays a Theora movie.
 */

import pogg.*;

TheoraMovie myMovie;
int frames = 0;

void setup() {
  frameRate(10);

  myMovie = new TheoraMovie(this, "BugsShort.ogg");
  myMovie.noLoop();

  size(myMovie.width, myMovie.height);
}

void draw() {
  myMovie.read();
  image(myMovie, 0, 0);

  String framesStr = "0000"+frames;
  framesStr = framesStr.substring(framesStr.length()-5,framesStr.length());

  String filename = "c:/out/frame"+framesStr;
  //savePixelsToPPM(filename, myMovie);
  //savePixelsToJPG(filename, myMovie);
  savePixelsToPNG(filename, myMovie);



  frames++;
}

/**
 * Portable PixMap (PPM) is a simple color image file format.
 * See http://en.wikipedia.org/wiki/Portable_Gray_Map
 */
void savePixelsToPPM(String filename, TheoraMovie myMovie) {
  try {
    OutputStream os = new FileOutputStream(new File(filename+".ppm"));
    String headerstring = "P6\n# Converted from Theora\n" + myMovie.width + " " + myMovie.height + "\n255\n";
    os.write(headerstring.getBytes());
    for(int i=0; i<myMovie.pixels.length; i++) {
      int myColor = myMovie.pixels[i];
      byte r = (byte)(myColor >> 16 & 0xFF);
      byte g = (byte)(myColor >> 8 & 0xFF);
      byte b = (byte)(myColor & 0xFF);
      os.write(r);
      os.write(g);
      os.write(b);
    }

    os.close();
  } 
  catch (Exception ex) {
    println("Exception "+ex); 
  }
}

BufferedImage createImage(TheoraMovie myMovie) {
  BufferedImage bufferedImage = new BufferedImage(myMovie.width, myMovie.height, BufferedImage.TYPE_INT_RGB);

  Graphics2D g2d = bufferedImage.createGraphics();
  int pos =0;
  for(int j=0; j<myMovie.height; j++) {
    for(int i=0; i<myMovie.width; i++) {
      int myColor = myMovie.pixels[pos];
      int r = (myColor >> 16 & 0xFF);
      int g = (myColor >> 8 & 0xFF);
      int b = (myColor & 0xFF);

      g2d.setColor(new Color(r, g, b));
      g2d.fillRect( i, j, 1, 1 ); //draw a single pixel
      pos++;
    }
  }

  // Graphics context no longer needed so dispose it
  g2d.dispose();

  return bufferedImage;
}

void savePixelsToJPG(String filename, TheoraMovie myMovie) {
  try {
    File file = new File(filename+".jpg");
    javax.imageio.ImageIO.write(createImage(myMovie), "jpg", file);
  } 
  catch (Exception ex) {
    println("Exception "+ex); 
  }
}

void savePixelsToPNG(String filename, TheoraMovie myMovie) {
  try {
    File file = new File(filename+".png");
    javax.imageio.ImageIO.write(createImage(myMovie), "png", file);
  } 
  catch (Exception ex) {
    println("Exception "+ex); 
  }
}

