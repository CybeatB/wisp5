##################################################
# NOTE: This makefile is not currently portable. #
#       You can try to use it, but it probably   #
#       won't work.                              #
##################################################


### Variables for Dependencies ###
JAVA_HOME=/cygdrive/c/Program\ Files/Java/jdk1.8.0_51
BLOC_DIR=../../CRIFDVirusDemo/bloc
GCC=/cygdrive/c/msys64/mingw64/bin/gcc
JAVAC=javac
JAVA=java
INCLUDES=-I ${BLOC_DIR} -I ${BLOC_DIR}/utils -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/win32 -I /cygdrive/c/System/Library/Frameworks/JavaVM.framework/Headers
OCTANESDK=../lib/OctaneSDKJava-1.22.0.30-jar-with-dependencies.jar\;.

# IP Address of Reader
READERHOST='-Dhostname=10.0.0.1'


### Make Targets ###
CRFIDVirus.o: CRFIDVirus.c
	${GCC} --std=c99 -c CRFIDVirus.c ${INCLUDES} 

tools.o: ${BLOC_DIR}/utils/tools.c
	${GCC} -c -D_JNI_TARGET ${BLOC_DIR}/utils/tools.c -I ${BLOC_DIR}/utils

aes-naive.o: ${BLOC_DIR}/AES/aes-naive.c ${BLOC_DIR}/utils/tools.c
	${GCC} -c -D_JNI_TARGET ${BLOC_DIR}/utils/aes-naive.c -I ${BLOC_DIR} -I ${BLOC_DIR}/utils

CRFIDVirus.dll: tools.o aes-naive.o CRFIDVirus.o
	${GCC} CRFIDVirus.o aes-naive.o tools.o -shared -o CRFIDVirus.dll

CRFIDVirus.class: CRFIDVirus.java CRFIDVirus.dll
	${JAVAC} CRFIDVirus.java -classpath ${OCTANESDK}

build: CRFIDVirus.class

test: CRFIDVirus.class
	${JAVA} ${READERHOST} -Djava.library.path=. -classpath ${OCTANESDK} CRFIDVirus

