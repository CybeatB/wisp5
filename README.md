
CRFID Reader Authentication Protocol Demo
====

This is the code repository for the CRFID Reader Authentication Protocol Demo
The name 'CRFIDVirus' is an artefact of this project's past, and should be changed at some point.
Much of the source code for this project was borrowed from the WISP5 project, the BLOC project, and the Impinj Octane SDK.
 * WISP5: http://github.com/wisp/wisp5
 * BLOC: https://github.com/kmarquet/bloc
 * Impinj Octane SDK: https://support.impinj.com/hc/en-us/articles/202755268-Octane-SDK

Layout
----

This project consists of two major components, the tag firmware (based on the WISP5) and the reader software (based on Octane SDK).
The AES Cipher as implemented by the BLOC project is used in both the tag and client applications.
Some modifications were made to the BLOC source files, which allowed them to be compiled and linked into the reader software.
The tag firmware is located in the CRIFDVirusDemo directory.
The BLOC source code is located in CRIFDVirusDemo/bloc.
The reader software is located in client/CRFIDVirus.
The Impinj Octane SDK is included in the client directory.
The report which accompanies this project is in the report directory.


Known Bugs
----

 * The makefile provided for the reader software is non-portable, and unlikely to work on a system which is configured differently to mine.
 * Running the client software for extended periods of time will cause it to crash.
 * The tag firmware currently stores important variables in volatile memory, and so does not work unless the tag is connected to a debugger.
 * The legality of including the Octane SDK in this repository is unknown.



Everything below this line is the README.md for the WISP5.

- - - - -

WISP 5
====

Welcome to the WISP5 firmware repository!

Got questions? Check out the tutorials and discussion board at: http://wisp5.wikispaces.com

Schematics for the WISP5 prototype are temporarily available here: 
http://sensor.cs.washington.edu/wisp5/wisp5-schem.pdf

Interested in building a host-side application to talk with WISPs? Look no further than the SLLURP library for configuring LLRP-based RFID readers:
https://github.com/ransford/sllurp

Important Notices
----
Please note that the MSP430FR5969 included on the WISP 5 is not compatible with TI Code Composer Studio versions prior to version 6. Please use CCS v6 or above.

The WISP 5 is intended to be compatible with Impinj Speedway and Impinj Speedway Revolution series FCC-compliant readers. For updates about compatibility with other readers, please contact the developers.

Configuration
----
1. Set your Code Composer Studio v6x workspace to wisp5/ccs and import the following projects:

 * **wisp-base** The standard library for the WISP5. Compiled as a static library.
 * **run-once** An application which generates a table of random numbers and stores them to non-volatile memory on the WISP, for use in slotting protocol and unique identification.

 * **simpleAckDemo** An application which references wisp-base and demonstrates basic communication with an RFID reader.

 * **accelDemo** An application which references wisp-base and demonstrates sampling of the accelerometer and returning acceleration data to the reader through fields of the EPC.

2. Build wisp-base and then the two applications.

3. Program and run your WISP5 with run-once, and wait for LED to pulse to indicate completion.

4. Program and run your WISP5 with simpleAckDemo and ensure that it can communicate with the reader. Use an Impinj Speedway series reader with Tari = 6.25us or 7.14us, link frequency = 640kHz, and reverse modulation type = FM0.

A summary of protocol details is given below.

Protocol summary
----

Delimiter = 12.5us

Tari = 6.25us

Link Frequency (T=>R) = 640kHz

Divide Ratio (DR) = 64/3

Reverse modulation type = FM0

RTCal (R=>T) = Nominally 15.625us (2.5*Data-0), Appears to accept 12.5us to 18.75us

TRCal (R=>T) = Appears to accept 13.75us to 56.25us, reader usage of this field may vary.

Data-0 (R=>T) = 6.25us

PW (R=>T) = 3.125us (0.5*(Data-0))

Enjoy the WISP5, and please contribute your comments and bugfixes here!


