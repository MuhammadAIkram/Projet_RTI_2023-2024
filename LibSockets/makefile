.SILENT:

all:	ServeurTest ClientTest

ServeurTest:	ServeurTest.cpp TCP.o
	echo "creation du ServeurTest"
	g++ ServeurTest.cpp TCP.o -o ServeurTest -Wall

ClientTest:	ClientTest.cpp TCP.o
	echo "creation du ClientTest"
	g++ ClientTest.cpp TCP.o -o ClientTest -Wall

TCP.o:	TCP.h TCP.cpp
	echo "Creation du TCP.o"
	g++ TCP.cpp -c -o TCP.o -Wall #-D DEBUG

clean:
	rm *.o