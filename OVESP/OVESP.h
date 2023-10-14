#ifndef OVESP_H
#define OVESP_H

#define NB_MAX_CLIENTS 100

bool OVESP(char* requete, char* reponse,int socket, MYSQL* conn);
int OVESP_Login(const char* user,const char* password,MYSQL* connexion);
int OVESP_NouveauLogin(const char* user,const char* password,MYSQL* connexion);
int Caddie_Verification(const char* user, MYSQL* connexion);
void OVESP_Logout(int sock);
void OVESP_Consult(int id, char* rep, MYSQL* connexion);
void OVESP_Achat(int id, char* rep, int quant, MYSQL* connexion);
void OVESP_Cancel(int id, int quant, char* rep, MYSQL* connexion);
void OVESP_Cancel_All(char *requete,int nbArti, char* rep, MYSQL* connexion);
void OVESP_Ajout_Facture(int idFacture, int NV_Article, float montant, int idArticle, int quant, MYSQL* connexion);
void OVESP_Supprime_Facture(int idFacture, float montant, int idArticle, MYSQL* connexion);
void OVESP_Supprime_ALL_Facture(int idFacture, MYSQL* connexion);
void OVESP_Confirmer(char *requete,int nbArti, char* rep, MYSQL* connexion);
void OVESP_Close();

#endif