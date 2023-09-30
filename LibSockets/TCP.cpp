#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

#include "TCP.h"

int ServerSocket(int port)
{
    int sEcoute;

    printf("pid = %d\n",getpid());

    if ((sEcoute = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
	 	perror("Erreur de socket()");
	 	exit(1);
	}

    printf("socket creee = %d\n",sEcoute);

    // Construction de l'adresse
	struct addrinfo hints;
	struct addrinfo *results;

	memset(&hints,0,sizeof(struct addrinfo));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE | AI_NUMERICSERV; // pour une connexion passive

    char str[10];

    sprintf(str, "%d", port);

	if (getaddrinfo(NULL,str,&hints,&results) != 0)
	    exit(1);

    // Affichage du contenu de l'adresse obtenue
	char host[NI_MAXHOST];
	char ports[NI_MAXSERV];
	getnameinfo(results->ai_addr,results->ai_addrlen, host,NI_MAXHOST,ports,NI_MAXSERV, NI_NUMERICSERV | NI_NUMERICHOST);
	printf("Mon Adresse IP: %s -- Mon Port: %s\n",host,ports);


    // Liaison de la socket à l'adresse
	if (bind(sEcoute,results->ai_addr,results->ai_addrlen) < 0)
	{
		perror("Erreur de bind()");
	 	exit(1);
	}
	 
	freeaddrinfo(results);
	printf("bind() reussi !\n");
    

    return sEcoute;
}

int Accept(int sEcoute,char *ipClient)
{
    // Mise à l'écoute de la socket
	if (listen(sEcoute,SOMAXCONN) == -1)
	{
		perror("Erreur de listen()");
		exit(1);
	}
	
	printf("listen() reussi !\n");
	
	// Attente d'une connexion
	int sService;
	if ((sService = accept(sEcoute,NULL,NULL)) == -1)
	{
		perror("Erreur de accept()");
		exit(1);
	}
	
	printf("accept() reussi !\n");
	printf("socket de service = %d\n",sService);

	// Recuperation d'information sur le client connecte
    char host[NI_MAXHOST];
	char port[NI_MAXSERV];
	struct sockaddr_in adrClient;
	socklen_t adrClientLen = sizeof(struct sockaddr_in); // nécessaire
	getpeername(sService,(struct sockaddr*)&adrClient,&adrClientLen);
	getnameinfo((struct sockaddr*)&adrClient,adrClientLen,host,NI_MAXHOST,port,NI_MAXSERV,NI_NUMERICSERV | NI_NUMERICHOST);
	printf("Client connecte --> Adresse IP: %s -- Port: %s\n",host,port);

    //strcpy(*ipClient, host);

    return sService;
}

int ClientSocket(char* ipServeur,int portServeur)
{
    int sClient;
    printf("pid = %d\n",getpid());
    
    // Creation de la socket
    if ((sClient = socket(AF_INET, SOCK_STREAM, 0)) == -1)
    {
        perror("Erreur de socket()");
        exit(1);
    }
    
    // Construction de l'adresse du serveur
    struct addrinfo hints;
    struct addrinfo *results;
    memset(&hints,0,sizeof(struct addrinfo));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_NUMERICSERV;

    char str[10];

    sprintf(str, "%d", portServeur);

    if (getaddrinfo(ipServeur,str,&hints,&results) != 0)
        exit(1);

    // Demande de connexion
    if (connect(sClient,results->ai_addr,results->ai_addrlen) == -1)
    {
        perror("Erreur de connect()");
        exit(1);
    }

    printf("connect() reussi !\n");

    return sClient;
}

int Send(int sSocket,char* data,int taille)
{
    if (taille > TAILLE_MAX_DATA)
        return -1;
    
    // Preparation de la charge utile
    char trame[TAILLE_MAX_DATA+2];
    memcpy(trame,data,taille);
    trame[taille] = '#';
    trame[taille+1] = ')';
    
    // Ecriture sur la socket
    return write(sSocket,trame,taille+2)-2;

}

int Receive(int sSocket,char* data)
{
    bool fini = false;
    int nbLus, i = 0;
    char lu1,lu2;
    
    while(!fini)
    {
        if ((nbLus = read(sSocket,&lu1,1)) == -1)
            return -1;
        if (nbLus == 0) return i; // connexion fermee par client
        
        if (lu1 == '#')
        {
            if ((nbLus = read(sSocket,&lu2,1)) == -1)
                return -1;
            
            if (nbLus == 0) return i; // connexion fermee par client
            
            if (lu2 == ')') fini = true;
            else
            {
                data[i] = lu1;
                data[i+1] = lu2;
                i += 2;
            }
        }
        else
        {
            data[i] = lu1;
            i++;
        }
    }
    
    return i;
}