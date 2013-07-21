/**
 * Rotate 
 * 
 * Example for the pogg library by Octavi Estape.
 * It plays a Theora movie that you can rotate and scale with the mouse.
 */
 
import pogg.*;

TheoraMovie myMovie;


void setup() {

  size(320, 240);
  
  myMovie = new TheoraMovie(this, "BugsShort.ogg");
  frameRate(myMovie.fps);
  myMovie.loop();

  smooth();

}

void draw() {
  background(0);
  
  myMovie.read();

  pushMatrix();

  
  float x = mouseX-(float)width/2;
  float y = mouseY-(float)height/2;

  translate(width/2, height/2);
  float angle = atan2(y,x);
  rotate(angle);
  float scal = sqrt(pow(x,2)+pow(y,2))/(myMovie.width/2);
  scale(scal);
  translate(-myMovie.width/2, -myMovie.height/2);

  image(myMovie, 0, 0);

  popMatrix();

}

