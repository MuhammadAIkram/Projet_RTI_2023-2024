#ifndef OVESP_H
#define OVESP_H

#define NB_MAX_CLIENTS 100

bool OVESP(char* requete, char* reponse,int socket);
int OVESP_Login(const char* user,const char* password);
void OVESP_Close();

#endif