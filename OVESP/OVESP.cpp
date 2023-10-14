#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <signal.h>
#include <mysql.h>
#include <locale.h>


#include "OVESP.h"

//***** Etat du protocole : liste des clients loggés ****************
int clients[NB_MAX_CLIENTS];
int nbClients = 0;

int estPresent(int socket);
void ajoute(int socket);
void retire(int socket);

pthread_mutex_t mutexClients = PTHREAD_MUTEX_INITIALIZER;

//***** Parsing de la requete et creation de la reponse *************
bool OVESP(char* requete, char* reponse,int socket, MYSQL* conn)
{
    // ***** Récupération nom de la requete *****************
    char *ptr = strtok(requete,"#");
    
    // ***** LOGIN ******************************************
    if (strcmp(ptr,"LOGIN") == 0) 
    {
        char user[50], password[50];
        int nouveauClient;
        
        strcpy(user,strtok(NULL,"#"));
        strcpy(password,strtok(NULL,"#"));
        nouveauClient = atoi(strtok(NULL,"#"));

        printf("LOGIN: %s - %s", user, password);
        
        if (estPresent(socket) >= 0) // client déjà loggé
        {
            sprintf(reponse,"LOGIN#ko#Client déjà loggé !");
        }
        else
        {
            if(nouveauClient == 0)
            {
                switch(OVESP_Login(user,password,conn))
                {
                    case 0:
                        sprintf(reponse,"LOGIN#ok#%d", Caddie_Verification(user, conn));
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
            else
            {
                if(OVESP_NouveauLogin(user,password,conn) == 0)
                {
                    sprintf(reponse,"LOGIN#ok#%d", Caddie_Verification(user, conn));
                    ajoute(socket);
                }
                else
                {
                    sprintf(reponse,"LOGIN#ko#Ce nom d'utilisateur est déjà pris!");
                }
            }
        }
    }

    // ***** LOGOUT ******************************************
    if (strcmp(ptr,"LOGOUT") == 0)
    {
        OVESP_Logout(socket);
        sprintf(reponse,"LOGOUT#ok");
    }

    // ***** CONSULT ******************************************
    if (strcmp(ptr,"CONSULT") == 0)
    {
        int idCon = atoi(strtok(NULL,"#"));

        OVESP_Consult(idCon, reponse, conn);
    }

    // ***** ACHAT ******************************************
    if (strcmp(ptr,"ACHAT") == 0)
    {
        int idCon = atoi(strtok(NULL,"#"));
        int quant = atoi(strtok(NULL,"#"));

        OVESP_Achat(idCon, reponse, quant, conn);
    }

    // ***** CANCEL ******************************************
    if (strcmp(ptr,"CANCEL") == 0)
    {
        int id = atoi(strtok(NULL,"#"));
        int stock = atoi(strtok(NULL,"#"));

        OVESP_Cancel(id, stock, reponse, conn);
    }

    // ***** CANCEL_ALL ******************************************
    if (strcmp(ptr,"CANCEL_ALL") == 0)
    {
        int nbArti = atoi(strtok(NULL,"#"));

        if(nbArti == 0) sprintf(reponse, "CANCEL_ALL#ko");
        else
        {
            char req[200] = "";
            int i = 1;

            strcat(req, strtok(NULL,"#"));

            while(i < nbArti)
            {
                strcat(req, "#");
                strcat(req, strtok(NULL,"#"));

                i++;
            }

            //printf("%s\n", req);

            OVESP_Cancel_All(req, nbArti, reponse, conn);
        }
    }

    // ***** UPDATE_CAD ******************************************
    if (strcmp(ptr,"UPDATE_CAD") == 0)
    {
        int numFact = atoi(strtok(NULL,"#"));
        int raison = atoi(strtok(NULL,"#"));

        if(raison == 0) //ajouter dans le caddie
        {
            printf("Ajout d'article \n");

            int nouveauArticle = atoi(strtok(NULL,"#"));
            setlocale(LC_NUMERIC, "C");
            float montant = atof(strtok(NULL,"#"));
            int idArti = atoi(strtok(NULL,"#"));
            int quant = atoi(strtok(NULL,"#"));

            OVESP_Ajout_Facture(numFact, nouveauArticle, montant, idArti, quant, conn);
        }
        else //supprimer du caddie
        {
            setlocale(LC_NUMERIC, "C");
            float montant = atof(strtok(NULL,"#"));
            int idArti = atoi(strtok(NULL,"#"));

            OVESP_Supprime_Facture(numFact, montant, idArti, conn);
        }
        
        sprintf(reponse, "UPDATE_CAD#ok");
    }

    // ***** DELETE_CAD ******************************************
    if (strcmp(ptr,"DELETE_CAD") == 0)
    {
        int numFacture = atoi(strtok(NULL,"#"));

        OVESP_Supprime_ALL_Facture(numFacture, conn);

        sprintf(reponse, "DELETE_CAD#ok");
    }

    // ***** CONFIRMER ******************************************
    if (strcmp(ptr,"CONFIRMER") == 0)
    {
        int numFact = atoi(strtok(NULL,"#"));
        char user[50];

        strcpy(user, strtok(NULL,"#"));

        //printf("%s\n", req);

        OVESP_Confirmer(numFact, conn); //confirme notre achat et creer facture definitive

        sprintf(reponse,"CONFIRMER#%d", Caddie_Verification(user, conn));

    }

    return true;
}

//***** Traitement des requetes *************************************
int OVESP_Login(const char* user,const char* password, MYSQL* connexion)
{
    char requete[200];
    MYSQL_RES  *resultat;
    MYSQL_ROW  Tuple;

    int val;
    char table[20];

    strcpy(table, "clients");

    sprintf(requete,"select * from %s where login = '%s';", table, user);


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
        if(strcmp(password, Tuple[2]) == 0) val = 0;
        else val = 2;
    }
    else
    {
        val = 1;
    }

    //kill(getpid(), SIGINT);

    //0: pas de probleme, login est bon
    //1: identifiant n'existe pas
    //2: mot de passe n'est pas bon

    return val;
}

int OVESP_NouveauLogin(const char* user,const char* password,MYSQL* connexion)
{
    char requete[200];
    MYSQL_RES  *resultat;
    MYSQL_ROW  Tuple;

    int val;
    char table[20];

    strcpy(table, "clients");

    sprintf(requete,"select * from %s where login = '%s';", table, user);


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
        val = 1;
    }
    else
    {
        sprintf(requete,"insert into %s (login, password) values ('%s', '%s')", table, user, password);


        if (mysql_query(connexion,requete) != 0)
        {
            fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
            exit(1);
        }

        printf("Requete INSERT réussie sur login.\n");

        val = 0;
    }

    //0: nouveau client inserer dans le BD
    //1: client deja existant avec cette identifiant

    return val;
}

int Caddie_Verification(const char* user, MYSQL* connexion)
{
    char requete[200];
    MYSQL_RES  *resultat;
    MYSQL_ROW  Tuple;

    int idFacture;
    char table[20];

    strcpy(table, "clients");

    sprintf(requete,"select * from %s where login = '%s';", table, user);


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
        strcpy(table, "factures");

        int idCli = atoi(Tuple[0]);

        sprintf(requete,"select * from %s where dateFacture is null and idClient = %d;", table, idCli);

        if (mysql_query(connexion,requete) != 0)
        {
            fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
            exit(1);
        }

        printf("Requete SELECT réussie sur facture.\n");

        // Affichage du Result Set

        if ((resultat = mysql_store_result(connexion)) == NULL)
        {
            fprintf(stderr, "Erreur de mysql_store_result: %s\n",mysql_error(connexion));
            exit(1);
        }

        if((Tuple = mysql_fetch_row(resultat)) != NULL)
        {
            idFacture = atoi(Tuple[0]);
        }
        else
        {
            sprintf(requete,"insert into %s (idClient, dateFacture, montant, paye) values (%d, NULL, NULL, 0)", table, idCli);

            if (mysql_query(connexion,requete) != 0)
            {
                fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
                exit(1);
            }

            printf("Requete INSERT réussie sur caddie.\n");

            sprintf(requete,"select max(idFacture) from %s;", table);

            if (mysql_query(connexion,requete) != 0)
            {
                fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
                exit(1);
            }

            printf("Requete SELECT réussie sur caddie.\n");

            // Affichage du Result Set

            if ((resultat = mysql_store_result(connexion)) == NULL)
            {
                fprintf(stderr, "Erreur de mysql_store_result: %s\n",mysql_error(connexion));
                exit(1);
            }

            if((Tuple = mysql_fetch_row(resultat)) != NULL)
            {
                idFacture = atoi(Tuple[0]);
            }
            else
            {
                idFacture = -1;
            }

        }
    }

    return idFacture;
}

void OVESP_Logout(int sock)
{
    retire(sock);
}

void OVESP_Consult(int id, char* rep, MYSQL* connexion)
{
    char requete[200];
    MYSQL_RES  *resultat;
    MYSQL_ROW  Tuple;

    char table[20];

    strcpy(table, "articles");

    sprintf(requete,"select * from %s where id = %d;", table, id);


    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete SELECT réussie sur Consult.\n");

    // Affichage du Result Set

    if ((resultat = mysql_store_result(connexion)) == NULL)
    {
        fprintf(stderr, "Erreur de mysql_store_result: %s\n",mysql_error(connexion));
        exit(1);
    }

    // Preparation de la reponse

    if((Tuple = mysql_fetch_row(resultat)) != NULL)
    {
        printf("%s - %s - %s - %s - %s \n", Tuple[0], Tuple[1], Tuple[2], Tuple[3], Tuple[4]);
        sprintf(rep,"CONSULT#%s#%s#%s#%s#%s", Tuple[0], Tuple[1], Tuple[3], Tuple[2], Tuple[4]);
    }
    else
    {
        sprintf(rep,"CONSULT#-1");
    }
}

void OVESP_Achat(int id, char* rep, int quant, MYSQL* connexion)
{
    char requete[200];
    MYSQL_RES  *resultat;
    MYSQL_ROW  Tuple;

    char table[20];

    strcpy(table, "articles");

    sprintf(requete,"select * from %s where id = %d;", table, id);


    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete SELECT réussie sur Achat.\n");

    // Affichage du Result Set

    if ((resultat = mysql_store_result(connexion)) == NULL)
    {
        fprintf(stderr, "Erreur de mysql_store_result: %s\n",mysql_error(connexion));
        exit(1);
    }

    // Preparation de la reponse

    if((Tuple = mysql_fetch_row(resultat)) != NULL)
    {
        if(atoi(Tuple[3]) < quant) sprintf(rep,"ACHAT#%s#0", Tuple[0]);
        else
        {
            int newStock = atoi(Tuple[3]) - quant;

            sprintf(requete,"update %s set stock = %d where id = %d;", table, newStock, id);

            if (mysql_query(connexion,requete) != 0)
            {
                fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
                exit(1);
            }

            printf("Requete UPDATE réussie.\n");

            sprintf(rep,"ACHAT#%s#%s#%s", Tuple[0], Tuple[3], Tuple[2]);
        }
    }
    else
    {
        sprintf(rep,"ACHAT#-1");
    }
}

void OVESP_Cancel(int id, int quant, char* rep, MYSQL* connexion)
{
    char requete[200];
    MYSQL_RES  *resultat;
    MYSQL_ROW  Tuple;

    char table[20];

    strcpy(table, "articles");

    sprintf(requete,"select * from %s where id = %d;", table, id);


    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete SELECT réussie sur Cancel.\n");

    // Affichage du Result Set

    if ((resultat = mysql_store_result(connexion)) == NULL)
    {
        fprintf(stderr, "Erreur de mysql_store_result: %s\n",mysql_error(connexion));
        exit(1);
    }

    // Preparation de la reponse

    if((Tuple = mysql_fetch_row(resultat)) != NULL)
    {
        int newStock = atoi(Tuple[3]) + quant;

        sprintf(requete,"update %s set stock = %d where id = %d;", table, newStock, id);

        if (mysql_query(connexion,requete) != 0)
        {
            fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
            exit(1);
        }

        printf("Requete UPDATE réussie.\n");

        sprintf(rep,"CANCEL#%s#%d", Tuple[0], atoi(Tuple[3]) + quant);
    }
    else
    {
        sprintf(rep,"CANCEL#-1");
    }
}

void OVESP_Cancel_All(char *requete, int nbArti, char* rep, MYSQL* connexion)
{
    int i = 0, id, stock;

    char *ptr = strtok(requete,"&");
    char idS[5];

    strcpy(idS, ptr);
    id = atoi(idS);
    stock = atoi(strtok(NULL,"#"));

    while(i < nbArti)
    {
        //printf("%d - %d\n",id, stock);

        OVESP_Cancel(id, stock, rep, connexion);

        if(i != nbArti-1)
        {
            strcpy(idS, strtok(NULL,"&"));
            id = atoi(idS);
            stock = atoi(strtok(NULL,"#"));
        }
        i++;
    }

    sprintf(rep, "CANCEL_ALL#ok");
}

void OVESP_Ajout_Facture(int idFacture, int NV_Article, float montant, int idArticle, int quant, MYSQL* connexion)
{
    char requete[200];

    char table[20];

    strcpy(table, "factures");

    sprintf(requete,"update %s set montant = %f where idFacture = %d;", table, montant, idFacture);

    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete UPDATE pour facture réussie.\n");
    
    strcpy(table, "ventes");

    if(NV_Article == 0) //si nouveau article
    {
        sprintf(requete,"insert into %s (idFacture, idArticle, quantite) values (%d, %d, %d)", table, idFacture, idArticle, quant);

        if (mysql_query(connexion,requete) != 0)
        {
            fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
            exit(1);
        }

        printf("Requete INSERT pour ventes réussie.\n");
    }
    else
    {
        sprintf(requete,"update %s set quantite = %d where idFacture = %d AND idArticle = %d;", table, quant, idFacture, idArticle);
        
        //printf("-- %s --", requete);

        if (mysql_query(connexion,requete) != 0)
        {
            fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
            exit(1);
        }

        printf("Requete UPDATE pour ventes réussie.\n");
    }
}

void OVESP_Supprime_Facture(int idFacture, float montant, int idArticle, MYSQL* connexion)
{
    char requete[200];

    char table[20];

    strcpy(table, "factures");

    sprintf(requete,"update %s set montant = %f where idFacture = %d;", table, montant, idFacture);

    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete UPDATE pour facture réussie.\n");

    strcpy(table, "ventes");

    sprintf(requete,"DELETE FROM %s WHERE idFacture = %d AND idArticle = %d;", table, idFacture, idArticle);

    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete DELETE pour ventes réussie.\n");
}

void OVESP_Supprime_ALL_Facture(int idFacture, MYSQL* connexion)
{
    char requete[200];

    char table[20];

    strcpy(table, "factures");

    sprintf(requete,"update %s set montant = 0 where idFacture = %d;", table, idFacture);

    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete UPDATE pour facture réussie.\n");

    strcpy(table, "ventes");

    sprintf(requete,"DELETE FROM %s WHERE idFacture = %d;", table, idFacture);

    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete DELETE pour ventes réussie.\n");
}

void OVESP_Confirmer(int idFacture, MYSQL* connexion)
{
    char requete[200];

    char table[20];

    strcpy(table, "factures");

    sprintf(requete,"update %s set dateFacture = curdate() where idFacture = %d;", table, idFacture);

    if (mysql_query(connexion,requete) != 0)
    {
        fprintf(stderr, "Erreur de mysql_query: %s\n",mysql_error(connexion));
        exit(1);
    }

    printf("Requete UPDATE pour facture réussie sur confirme.\n");
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