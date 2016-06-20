#!/usr/bin/zsh
JAVA_HOME='/cygdrive/c/Program Files/Java/jdk1.8.0_51'
BLOC_DIR='../../../../Uni/MWN-WISP5/wisp5/CRIFDVirusDemo/bloc'
GCC='/cygdrive/c/msys64/mingw64/bin/gcc'

# Compile CRFIDVirus
$GCC --std=c99 -c CRFIDVirus.c -I $JAVA_HOME/include -I $JAVA_HOME/include/win32 -I $BLOC_DIR -I $BLOC_DIR/utils -I /cygdrive/c/System/Library/Frameworks/JavaVM.framework/Headers

# Compile AES
$GCC -c -D_JNI_TARGET $BLOC_DIR/AES/aes-naive.c -I $BLOC_DIR -I $BLOC_DIR/utils -I $BLOC_DIR/AES
$GCC -c -D_JNI_TARGET $BLOC_DIR/utils/tools.c -I $BLOC_DIR/utils

# Link
$GCC CRFIDVirus.o aes-naive.o tools.o -shared -o CRFIDVirus.dll

