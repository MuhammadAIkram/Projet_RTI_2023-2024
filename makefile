.SILENT:

SA = ServeurAchat
LB = LibSockets

PROGRAMS = Serveur

all:	$(PROGRAMS)

Serveur:	$(SA)/Serveur.cpp $(LB)/TCP.o OVESP/OVESP.o
	echo "Creation du Serveur"
	g++ $(SA)/Serveur.cpp $(LB)/TCP.o OVESP/OVESP.o -o Serveur -lpthread

$(LB)/TCP.o:	$(LB)/TCP.h $(LB)/TCP.cpp
	echo "Creation du TCP.o"
	g++ $(LB)/TCP.cpp -c -o $(LB)/TCP.o -Wall #-D DEBUG

OVESP/OVESP.o:	OVESP/OVESP.h OVESP/OVESP.cpp
	echo "Creation du OVESP.o"
	g++ OVESP/OVESP.cpp -c -o OVESP/OVESP.o -Wall #-D DEBUG

clean:
	rm $(LB)/*.o
	rm OVESP/*.o