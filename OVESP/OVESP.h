#ifndef OVESP_H
#define OVESP_H

#define NB_MAX_CLIENTS 100

bool OVESP(char* requete, char* reponse,int socket, MYSQL* conn);
int OVESP_Login(const char* user,const char* password,MYSQL* connexion);
int OVESP_NouveauLogin(const char* user,const char* password,MYSQL* connexion);
void OVESP_Logout(int sock);
void OVESP_Consult(int id, char* rep, MYSQL* connexion);
void OVESP_Achat(int id, char* rep, int quant, MYSQL* connexion);
void OVESP_Cancel(int id, int quant, char* rep, MYSQL* connexion);
void OVESP_Cancel_All(char *requete,int nbArti, char* rep, MYSQL* connexion);
void OVESP_Close();

#endif