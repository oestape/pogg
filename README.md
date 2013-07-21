Pogg: Processing Theora Ogg Videos
==================================

Pogg is a pure Java library for Theora ogg movie playback. It allows you to play ogg videos in any Java application, applet or Processing.org sketch.

Pogg is licensed under the GNU LGPL (Lesser General Public License) which means that you can use it in proprietary software as long as you release the source code of the library (not the rest of the application) as well. You are encouraged to contribute to Pogg.

Being a pure Java library it works in Windows, Linux and Mac OS X, and you can use it in Java applets without special security permissions.

If you want to see Pogg in action visit http://www.activovision.com/pogg/ 

Features
--------

* Theora Ogg player: the open format for video
* Pure Java: no native libraries needed, no need to sign the applets, works on Windows, Mac OS and Linux
* LGPL license: it is open source and you can use it in your proprietary applications
* Small: the library is 156KB
* Relatively fast: plays video smoothly even in old computers (for example 400×300 at 30fps in a 7 year old P4 2GHz consumes 60% of the CPU)
* Easy: the same interface as other Processing video libraries

Using Pogg with Processing.org
------------------------------

Pogg is specially designed to be used with Processing.org. Processing allows you to easily create 3D graphics and it has many libraries available to use sound, image processing, I/O, particle dynamics and many other things. Pogg is just another Processing library.

* Download and install [Processing] (http://processing.org/)
* Download Pogg and uncompress it in the Processing/libraries folder (in your sketchbook)
* Create a new Processing sketch or copy one example
* You can place the videos in a folder called data (in the same folder of the sketch) or write the absolute file path.

Limitations
-----------

* No camera capture
* No video encoding (recording) only playing
* No sound (we hope that future versions will extract Vorbis audio)
