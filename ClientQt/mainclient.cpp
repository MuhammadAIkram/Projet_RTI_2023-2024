#include "windowclient.h"
#include "../LibSockets/TCP.h"

#include <QApplication>

WindowClient *w;

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Erreur...\n");
        printf("USAGE : Client ipServeur portServeur\n");
        exit(1);
    }

    int sClient;
    
    if ((sClient = ClientSocket(argv[1],atoi(argv[2]))) == -1)
    {
        perror("Erreur de ClientSocket");
        exit(1);
    }

    QApplication a(argc, argv);
    w = new WindowClient();
    w->show();
    return a.exec();
}
