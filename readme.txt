Homework 3 - DCT-Based Image Compression
Homework 2 - Aliasing and Dictionary Coding
Homework 1 - Image Quantization

CS451 Multimedia Software Systems
@ Author: Elaine Kang
Computer Science Department
California State University, Los Angeles

Student: Rick Miyamoto

Homework 3 - DCT-Based Image Compression
======================================
This program is based on a new class called DCTCompress which does not extend the Image class like the other classes, but it does use the Image class. This is done for a bit more readability and simplification. When the constructor is called with the appropriate parameters (A file name string, and n, the quantization level integer), it will create two images for you. 

One will be the resized padded image, immediately after the resizing step. This step is more so for debugging purposes and probably optional for the instructor so the line

resizedImg.write2PPM("(Resized) " + fileName);

in the constructor of the DCTCompress class may be ommitted or commented out.

The second image is the final decomposed product, which is done after the color-space transform, subsampling, DCT, quantization, calculating compression ratio, and then the inverse steps (dequantization, inverse DCT, supersample, inverse color-space transform, remove padding and resize back).

Again for simplicity, this program wasn't designed to loop unlike the other programs/homeworks since the assignment instructions did not specify too.

The newly created PPM images will have "(Resized)" or "(Restored)" attached to the front of the original file name.

Homework 2 - Aliasing (Findings)
======================================
Original - By default, creating a new Aliasing object will initialize an image and draw a circle on the image, then it will display it to the user. This circle is the original image.

The following images are resized from the original image by the constant value K.

Resize (No Filter) - Based on many test cases, this resized image looks pretty bad compared to the other resized image, especially the sharper edges.

Resize (Average Filter) - The average filter works very well where the no-filter does pretty bad. Edges show up nicely. However, because this is based on an averaging algorithm, the colors will not be accurate. For example, an image with only white and black pixels may show some gray pixels after resizing with the average filter.

Resize (3x3 Filter 1) - This filter is not as smooth as the average filter (some aliasing can be seen), however the colors are more accurate. 

Resize (3x3 Filter 2) - This filter by far is the most difficult one to compare and contrast. It is very similar to Filter 1, however there is more color in places where pixels are less represented (for example, the thin vertical lines at the edges of the circle)


Compile requirement
======================================
JDK Version 5.0 or above


Compile Instruction on Command Line:
======================================
javac CS451_Miyamoto.java Image.java ImageQuantization.java
or 
javac *.java


Execution Instruction on Command Line:
======================================
java CS451_Miyamoto [homework #] [inputfile]
e.g.
java CS451_Miyamoto 1 Ducky.ppm 


Java Classes
======================================
The Image class is mostly unchanged from the template. In fact, the only thing added is a private String to store fileName, a getter method for the fileName, and a couple empty constructors.
The ImageQuantization class extends the Image class and contains methods for image quantization. This includes Error Diffusion, Bi-level Threshold, Uniform Color Quantization, etc.


Running the Program
======================================
The program displays a menu and works as per instructions. 

Options 1-3 will perform conversions, and option 4 will terminate the program.

The menu will loop endlessly until option 4 is executed.