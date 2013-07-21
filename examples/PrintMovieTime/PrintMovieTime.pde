/**
 * PrintMovieTime 
 * 
 * Example for the pogg library by Octavi Estape.
 * It plays a Theora movie and prints the current time.
 */
 
import pogg.*;

TheoraMovie myMovie;
PFont font;

void setup() {
  frameRate(10);

  myMovie = new TheoraMovie(this, "BugsShort.ogg");
  myMovie.loop();

  size(myMovie.width, myMovie.height);

  font = createFont("Monospaced",14);
  textFont(font);
}

void draw() {
  myMovie.read();
  image(myMovie, 0, 0);

  float sec = myMovie.time();
  String time = formatTime(sec);
  
  text(time,5,15);

}

String formatTime(float sec) {
  int seconds = (int)sec;
  float fraction = sec-seconds;
  int mill = (int)(fraction *1000);
  int minutes = seconds/60;
  seconds = seconds%60;
  int hours = minutes/60;
  minutes = minutes%60;
  
  String time = "";
  if(hours<10) {
    time = time+"0";
  }
  time = time+hours+":";
  if(minutes<10) {
    time = time+"0";
  }
  time = time+minutes+":";
  if(seconds<10) {
    time = time+"0";
  }
  
  time = time+seconds+".";
  if (mill < 100) {
      time = time + "0";
  }
  if (mill < 10) {
      time = time + "0";
  }
  time = time + mill;
  return time;
}

