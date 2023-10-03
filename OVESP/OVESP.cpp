#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <signal.h>
#include <mysql.h>


#include "OVESP.h"

//***** Etat du protocole : liste des clients loggés ****************
int clients[NB_MAX_CLIENTS];
int nbClients = 0;

int estPresent(int socket);
void ajoute(int socket);
void retire(int socket);

pthread_mutex_t mutexClients = PTHREAD_MUTEX_INITIALIZER;

//***** Parsing de la requete et creation de la reponse *************
bool OVESP(char* requete, char* reponse,int socket)
{
    // ***** Récupération nom de la requete *****************
    char *ptr = strtok(requete,"#");
    
    // ***** LOGIN ******************************************
    if (strcmp(ptr,"LOGIN") == 0) 
    {
        char user[50], password[50];
        strcpy(user,strtok(NULL,"#"));
        strcpy(password,strtok(NULL,"#"));
        printf("\t[THREAD %p] LOGIN de %s\n",pthread_self(),user);
        if (estPresent(socket) >= 0) // client déjà loggé
        {
            sprintf(reponse,"LOGIN#ko#Client déjà loggé !");
        }
        else
        {
            switch(OVESP_Login(user,password))
            {
                case 0:
                    sprintf(reponse,"LOGIN#ok");
                    ajoute(socket);
                    break;
                case 1:
                    sprintf(reponse,"LOGIN#ko#Mauvais identifiants!");
                    break;
                case 2:
                    sprintf(reponse,"LOGIN#ko#Mauvais mot de passe!");
                    break;
            }
        }
    }

    return true;
}

//***** Traitement des requetes *************************************
int OVESP_Login(const char* user,const char* password)
{
    // Connexion à la base de donnée
    MYSQL* connexion;
    
    connexion = mysql_init(NULL);
    
    if (mysql_real_connect(connexion,"localhost","Student","PassStudent1_","PourStudent",0,0,0) == NULL)
    {
        fprintf(stderr,"(SERVEUR) Erreur de connexion à la base de données...\n");
        exit(1);  
    }

    char requete[200];
    MYSQL_RES  *resultat;
    MYSQL_ROW  Tuple;

    int val;
    char table[20];

    strcpy(table, "utilisateurs");

    sprintf(requete,"select * from %s where login = %s;", table, user);


    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete SELECT réussie sur login.\n");

    // Affichage du Result Set

    if ((resultat = mysql_store_result(connexion)) == NULL)
    {
        fprintf(stderr, "Erreur de mysql_store_result: %s\n",mysql_error(connexion));
        exit(1);
    }

    if((Tuple = mysql_fetch_row(resultat)) != NULL)
    {
        if(strcmp(password, Tuple[1]) == 0) val = 0;
        else val = 2;
    }
    else
    {
        val = 1;
    }

    //kill(getpid(), SIGINT);

    mysql_close(connexion);

    //0: pas de probleme, login est bon
    //1: identifiant n'existe pas
    //2: mot de passe n'est pas bon

    return val;
}

//***** Gestion de l'état du protocole ******************************
int estPresent(int socket)
{
    int indice = -1;

    pthread_mutex_lock(&mutexClients);

    for(int i=0 ; i<nbClients ; i++)
        if (clients[i] == socket) { indice = i; break; }

    pthread_mutex_unlock(&mutexClients);
    
    return indice;
}

void ajoute(int socket)
{
    pthread_mutex_lock(&mutexClients);
    clients[nbClients] = socket;
    nbClients++;
    pthread_mutex_unlock(&mutexClients);
}

void retire(int socket)
{
    int pos = estPresent(socket);
    if (pos == -1) return;
    pthread_mutex_lock(&mutexClients);
    for (int i=pos ; i<=nbClients-2 ; i++)
        clients[i] = clients[i+1];
    nbClients--;
    pthread_mutex_unlock(&mutexClients);
}

//***** Fin prématurée **********************************************
void OVESP_Close()
{
    pthread_mutex_lock(&mutexClients);

    for (int i=0 ; i<nbClients ; i++)
        close(clients[i]);

    pthread_mutex_unlock(&mutexClients);
}