#!/usr/bin/zsh

if [[ -n "$1" ]]
	then READERHOST="-Dhostname=${1}"
fi
OCTANESDK="../lib/$(ls ../lib | grep "with-dependencies");."

if javac CRFIDVirus.java -classpath ${OCTANESDK}
then
	java ${READERHOST} -Djava.library.path=. -classpath "${OCTANESDK}" CRFIDVirus
fi

