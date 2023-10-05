#ifndef OVESP_H
#define OVESP_H

#define NB_MAX_CLIENTS 100

bool OVESP(char* requete, char* reponse,int socket, MYSQL* conn);
int OVESP_Login(const char* user,const char* password,MYSQL* connexion);
int OVESP_NouveauLogin(const char* user,const char* password,MYSQL* connexion);
void OVESP_Close();

#endif