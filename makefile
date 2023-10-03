.SILENT:

SA = ServeurAchat
LB = LibSockets
CL = ClientQt

PROGRAMS = Serveur Client

OBS = g++ -Wno-unused-parameter -c -pipe -g -std=gnu++11 -Wall -W -D_REENTRANT -fPIC -DQT_DEPRECATED_WARNINGS -DQT_QML_DEBUG -DQT_WIDGETS_LIB -DQT_GUI_LIB -DQT_CORE_LIB -I../UNIX_DOSSIER_FINAL -I. -isystem /usr/include/qt5 -isystem /usr/include/qt5/QtWidgets -isystem /usr/include/qt5/QtGui -isystem /usr/include/qt5/QtCore -I. -I. -I/usr/lib64/qt5/mkspecs/linux-g++ -o

all:	$(PROGRAMS)

Serveur:	$(SA)/Serveur.cpp $(LB)/TCP.o OVESP/OVESP.o
	echo "Creation du Serveur"
	g++ $(SA)/Serveur.cpp $(LB)/TCP.o OVESP/OVESP.o -o Serveur -lpthread

$(LB)/TCP.o:	$(LB)/TCP.h $(LB)/TCP.cpp
	echo "Creation du TCP.o"
	g++ $(LB)/TCP.cpp -c -o $(LB)/TCP.o -Wall #-D DEBUG

OVESP/OVESP.o:	OVESP/OVESP.h OVESP/OVESP.cpp
	echo "Creation du OVESP.o"
	g++ OVESP/OVESP.cpp -c -o OVESP/OVESP.o -Wall -I/usr/include/mysql -m64 -L/usr/lib64/mysql -lmysqlclient -lpthread -lz -lm -lrt -lssl -lcrypto -ldl #-D DEBUG

Client:	$(CL)/mainclient.o $(CL)/windowclient.o $(CL)/moc_windowclient.o
	echo "Creation du Client"
	g++ -Wno-unused-parameter -o Client $(CL)/mainclient.o $(CL)/windowclient.o $(CL)/moc_windowclient.o $(LB)/TCP.o  /usr/lib64/libQt5Widgets.so /usr/lib64/libQt5Gui.so /usr/lib64/libQt5Core.so /usr/lib64/libGL.so -lpthread

$(CL)/moc_windowclient.o:	$(CL)/moc_windowclient.cpp
	echo "Creation du moc_windowclient.o"
	$(OBS) $(CL)/moc_windowclient.o $(CL)/moc_windowclient.cpp

$(CL)/windowclient.o:	$(CL)/windowclient.cpp
	echo "Creation du windowclient.o"
	$(OBS) $(CL)/windowclient.o $(CL)/windowclient.cpp

$(CL)/mainclient.o:	$(CL)/mainclient.cpp
	echo "Creation du mainclient.o"
	$(OBS) $(CL)/mainclient.o $(CL)/mainclient.cpp

clean:
	rm $(LB)/*.o
	rm OVESP/*.o
	rm $(CL)/*.o