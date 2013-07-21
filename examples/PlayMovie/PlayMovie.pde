/**
 * PlayMovie 
 * 
 * Example for the pogg library by Octavi Estape.
 * It plays a Theora movie.
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
  image(myMovie, 0, 0);
}

