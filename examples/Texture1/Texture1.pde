/**
 * Texture 1. 
 * 
 * Example for the pogg library by Octavi Estape.
 * Adapted from the example in File->Examples->3D->Textures->Texture1.
 * Load an image and draw it onto a quad. The texture() function sets
 * the texture image. The vertex() function maps the image to the geometry.
 */

import pogg.*;

TheoraMovie myMovie;

void setup() {
  frameRate(10);
  size(640, 360, P3D);

  myMovie = new TheoraMovie(this, "BugsShort.ogg");
  myMovie.loop();

  noStroke();
  
  //with smooth() the perspective is better, but it is slower and there are lines in the triangle edges 
  //smooth();
}

void draw() {
  myMovie.read();
  
  background(0);
  translate(width / 2, height / 2);
  rotateY(map(mouseX, 0, width, -PI, PI));
  rotateZ(PI/6);
  
  //original Texture 1 example:
  /*
  beginShape();
  texture(myMovie);
  vertex(-100, -75, 0, 0, 0);
  vertex(100, -75, 0,  myMovie.width, 0);
  vertex(100, 75, 0,  myMovie.width, myMovie.height);
  vertex(-100, 75, 0, 0, myMovie.height);
  endShape();
  */
  
  //sligthly better perspective quality when smooth() is not called
  beginShape(TRIANGLE_FAN);
  texture(myMovie);
  vertex(0, 0, 0,  myMovie.width/2, myMovie.height/2);
  vertex(-100, -75, 0, 0, 0);
  vertex(100, -75, 0, myMovie.width, 0);
  vertex(100, 75, 0, myMovie.width, myMovie.height);
  vertex(-100, 75, 0, 0, myMovie.height);
  vertex(-100, -75, 0, 0, 0);
  endShape();
  
  
}
