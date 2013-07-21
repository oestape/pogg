/**
 * Invert 
 * 
 * Example for the pogg library by Octavi Estape.
 * It plays a Theora movie inverting the colors of the pixels (negative image).
 */
 
import pogg.*;

TheoraMovie myMovie;

void setup() {
  frameRate(10);

  myMovie = new TheoraMovie(this, "BugsShort.ogg");
  myMovie.loop();

  size(myMovie.width, myMovie.height);

}

void draw() {

  myMovie.read();

  loadPixels();
  for(int i=0; i<myMovie.pixels.length; i++) {
    int c = myMovie.pixels[i];
    int r = c >> 16 & 0xFF; // = red(c);
    int g = c >> 8 & 0xFF;  // = green(c);
    int b = c & 0xFF;       // = blue(c);

    //color inverse
    r = 255-r;
    g = 255-g;
    b = 255-b;

    pixels[i] = color(r, g, b);
  }
  updatePixels();

}






