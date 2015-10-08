Homework 1 - Image Quantization

CS451 Multimedia Software Systems
@ Author: Elaine Kang
Computer Science Department
California State University, Los Angeles

Student: Rick Miyamoto

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