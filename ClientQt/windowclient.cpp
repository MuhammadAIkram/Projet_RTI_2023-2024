#include "windowclient.h"
#include "ui_windowclient.h"
#include <QMessageBox>
#include <string>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

using namespace std;

extern WindowClient *w;

typedef struct
{
  int   id;
  char  intitule[20];
  float prix;
  int   stock;  
  char  image[20];
} ARTICLE;

ARTICLE articleCourant;
ARTICLE Caddie[10];
int nbArticles = 0, numFacture = 0;
float totalCaddie = 0.0;
bool logged = false;

void SendReceiveReq(char* requete, char *buffer);

#include "../LibSockets/TCP.h"

#define REPERTOIRE_IMAGES "ClientQt/images/"

WindowClient::WindowClient(QWidget *parent) : QMainWindow(parent), ui(new Ui::WindowClient)
{
    ui->setupUi(this);

    // Configuration de la table du panier (ne pas modifer)
    ui->tableWidgetPanier->setColumnCount(3);
    ui->tableWidgetPanier->setRowCount(0);
    QStringList labelsTablePanier;
    labelsTablePanier << "Article" << "Prix à l'unité" << "Quantité";
    ui->tableWidgetPanier->setHorizontalHeaderLabels(labelsTablePanier);
    ui->tableWidgetPanier->setSelectionMode(QAbstractItemView::SingleSelection);
    ui->tableWidgetPanier->setSelectionBehavior(QAbstractItemView::SelectRows);
    ui->tableWidgetPanier->horizontalHeader()->setVisible(true);
    ui->tableWidgetPanier->horizontalHeader()->setDefaultSectionSize(160);
    ui->tableWidgetPanier->horizontalHeader()->setStretchLastSection(true);
    ui->tableWidgetPanier->verticalHeader()->setVisible(false);
    ui->tableWidgetPanier->horizontalHeader()->setStyleSheet("background-color: lightyellow");

    ui->pushButtonPayer->setText("Confirmer achat");
    setPublicite("!!! Bienvenue sur le Maraicher en ligne !!!");

    // Exemples à supprimer
    //setArticle("pommes",5.53,18,"pommes.jpg");
    //ajouteArticleTablePanier("cerises",8.96,2);

    //initialiser vecteur articles avec des valeurs temporaire
    for(int i = 0; i < 10; i++)
    {
      Caddie[i].id = 0; //0 utiliser pour indiquer une place libre
    }
}

WindowClient::~WindowClient()
{
    delete ui;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions utiles : ne pas modifier /////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::setNom(const char* Text)
{
  if (strlen(Text) == 0 )
  {
    ui->lineEditNom->clear();
    return;
  }
  ui->lineEditNom->setText(Text);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
const char* WindowClient::getNom()
{
  strcpy(nom,ui->lineEditNom->text().toStdString().c_str());
  return nom;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::setMotDePasse(const char* Text)
{
  if (strlen(Text) == 0 )
  {
    ui->lineEditMotDePasse->clear();
    return;
  }
  ui->lineEditMotDePasse->setText(Text);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
const char* WindowClient::getMotDePasse()
{
  strcpy(motDePasse,ui->lineEditMotDePasse->text().toStdString().c_str());
  return motDePasse;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::setPublicite(const char* Text)
{
  if (strlen(Text) == 0 )
  {
    ui->lineEditPublicite->clear();
    return;
  }
  ui->lineEditPublicite->setText(Text);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::setImage(const char* image)
{
  // Met à jour l'image
  char cheminComplet[80];
  sprintf(cheminComplet,"%s%s",REPERTOIRE_IMAGES,image);
  QLabel* label = new QLabel();
  label->setSizePolicy(QSizePolicy::Ignored, QSizePolicy::Ignored);
  label->setScaledContents(true);
  QPixmap *pixmap_img = new QPixmap(cheminComplet);
  label->setPixmap(*pixmap_img);
  label->resize(label->pixmap()->size());
  ui->scrollArea->setWidget(label);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
int WindowClient::isNouveauClientChecked()
{
  if (ui->checkBoxNouveauClient->isChecked()) return 1;
  return 0;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::setArticle(const char* intitule,float prix,int stock,const char* image)
{
  ui->lineEditArticle->setText(intitule);
  if (prix >= 0.0)
  {
    char Prix[20];
    sprintf(Prix,"%.2f",prix);
    ui->lineEditPrixUnitaire->setText(Prix);
  }
  else ui->lineEditPrixUnitaire->clear();
  if (stock >= 0)
  {
    char Stock[20];
    sprintf(Stock,"%d",stock);
    ui->lineEditStock->setText(Stock);
  }
  else ui->lineEditStock->clear();
  setImage(image);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
int WindowClient::getQuantite()
{
  return ui->spinBoxQuantite->value();
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::setTotal(float total)
{
  if (total >= 0.0)
  {
    char Total[20];
    sprintf(Total,"%.2f",total);
    ui->lineEditTotal->setText(Total);
  }
  else ui->lineEditTotal->clear();
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::loginOK()
{
  ui->pushButtonLogin->setEnabled(false);
  ui->pushButtonLogout->setEnabled(true);
  ui->lineEditNom->setReadOnly(true);
  ui->lineEditMotDePasse->setReadOnly(true);
  ui->checkBoxNouveauClient->setEnabled(false);

  ui->spinBoxQuantite->setEnabled(true);
  ui->pushButtonPrecedent->setEnabled(true);
  ui->pushButtonSuivant->setEnabled(true);
  ui->pushButtonAcheter->setEnabled(true);
  ui->pushButtonSupprimer->setEnabled(true);
  ui->pushButtonViderPanier->setEnabled(true);
  ui->pushButtonPayer->setEnabled(true);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::logoutOK()
{
  ui->pushButtonLogin->setEnabled(true);
  ui->pushButtonLogout->setEnabled(false);
  ui->lineEditNom->setReadOnly(false);
  ui->lineEditMotDePasse->setReadOnly(false);
  ui->checkBoxNouveauClient->setEnabled(true);

  ui->spinBoxQuantite->setEnabled(false);
  ui->pushButtonPrecedent->setEnabled(false);
  ui->pushButtonSuivant->setEnabled(false);
  ui->pushButtonAcheter->setEnabled(false);
  ui->pushButtonSupprimer->setEnabled(false);
  ui->pushButtonViderPanier->setEnabled(false);
  ui->pushButtonPayer->setEnabled(false);

  setNom("");
  setMotDePasse("");
  ui->checkBoxNouveauClient->setCheckState(Qt::CheckState::Unchecked);

  setArticle("",-1.0,-1,"");

  w->videTablePanier();
  w->setTotal(-1.0);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions utiles Table du panier (ne pas modifier) /////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::ajouteArticleTablePanier(const char* article,float prix,int quantite)
{
    char Prix[20],Quantite[20];

    sprintf(Prix,"%.2f",prix);
    sprintf(Quantite,"%d",quantite);

    // Ajout possible
    int nbLignes = ui->tableWidgetPanier->rowCount();
    nbLignes++;
    ui->tableWidgetPanier->setRowCount(nbLignes);
    ui->tableWidgetPanier->setRowHeight(nbLignes-1,10);

    QTableWidgetItem *item = new QTableWidgetItem;
    item->setFlags(Qt::ItemIsSelectable|Qt::ItemIsEnabled);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(article);
    ui->tableWidgetPanier->setItem(nbLignes-1,0,item);

    item = new QTableWidgetItem;
    item->setFlags(Qt::ItemIsSelectable|Qt::ItemIsEnabled);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(Prix);
    ui->tableWidgetPanier->setItem(nbLignes-1,1,item);

    item = new QTableWidgetItem;
    item->setFlags(Qt::ItemIsSelectable|Qt::ItemIsEnabled);
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(Quantite);
    ui->tableWidgetPanier->setItem(nbLignes-1,2,item);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::videTablePanier()
{
    ui->tableWidgetPanier->setRowCount(0);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
int WindowClient::getIndiceArticleSelectionne()
{
    QModelIndexList liste = ui->tableWidgetPanier->selectionModel()->selectedRows();
    if (liste.size() == 0) return -1;
    QModelIndex index = liste.at(0);
    int indice = index.row();
    return indice;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions permettant d'afficher des boites de dialogue (ne pas modifier ////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::dialogueMessage(const char* titre,const char* message)
{
   QMessageBox::information(this,titre,message);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::dialogueErreur(const char* titre,const char* message)
{
   QMessageBox::critical(this,titre,message);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////// CLIC SUR LA CROIX DE LA FENETRE /////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::closeEvent(QCloseEvent *event)
{
  if(logged == true) on_pushButtonLogout_clicked();

  exit(0);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions clics sur les boutons ////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonLogin_clicked()
{

  if(strlen(getNom()) < 3)
  {
    dialogueErreur("Login","Veuillez entrer le nom d'utilisateur, il doit comporter au moins 3 caractères!");
    return;
  }

  if(strlen(getMotDePasse()) < 5)
  {
    dialogueErreur("Login","Veuillez entrer le mot de passe, il doit comporter au moins 5 caractères!");
    return;
  }

  char texte[100], buffer[100];
  sprintf(texte,"LOGIN#%s#%s#%d", getNom(), getMotDePasse(), isNouveauClientChecked());
  
  SendReceiveReq(texte, buffer);

  char *ptr = strtok(buffer,"#");

  if (strcmp(ptr,"LOGIN") == 0) 
  {
    char reponse[20], message[100];
    strcpy(reponse,strtok(NULL,"#"));

    if (strcmp(reponse,"ok") == 0) 
    {
      if(isNouveauClientChecked() == 1) dialogueMessage("Login", "Vous avez été inscrit avec succès");

      dialogueMessage("Login", "Vous êtes connecté avec succès");

      loginOK();

      logged = true;

      numFacture = atoi(strtok(NULL,"#"));

      ConsultArticle(1);
    }
    else
    {
      strcpy(message,strtok(NULL,"#"));

      dialogueErreur("Login", message);
    }

  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonLogout_clicked()
{
  char texte[50], buffer[50];
  sprintf(texte,"LOGOUT");

  SendReceiveReq(texte, buffer);

  char *ptr = strtok(buffer,"#");

  if (strcmp(ptr,"LOGOUT") == 0) 
  {
    char reponse[20];
    strcpy(reponse,strtok(NULL,"#"));

    if (strcmp(reponse,"ok") == 0) 
    {
      if(nbArticles != 0)
      {
        bool check;

        check = VidePanier();
        
        if(check == true) printf("check vide\n");
      }

      dialogueMessage("Logout", "BYE BYE ;)");

      logoutOK();

      logged = false;
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonSuivant_clicked()
{
  ConsultArticle(articleCourant.id+1);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonPrecedent_clicked()
{
  ConsultArticle(articleCourant.id-1);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonAcheter_clicked()
{
  if(getQuantite() == 0)
  {
    dialogueErreur("Achat", "Veuillez sélectionner une valeur supérieure à 0!");
    return;
  }

  int i = 0;

  while(Caddie[i].id != articleCourant.id && i < 10) i++; //verifie si cette article existe deja dans le panier

  if(nbArticles == 10 && i == 10)
  {
    dialogueMessage("Achat", "votre panier est plein, merci d'acheter les articles ou de supprimer un article du panier !");
    return;
  }

  char texte[100], buffer[100];
  sprintf(texte,"ACHAT#%d#%d", articleCourant.id, getQuantite());
  
  SendReceiveReq(texte, buffer);

  char *ptr = strtok(buffer,"#");

  if (strcmp(ptr,"ACHAT") == 0) 
  {
    int newID;
    
    newID = atoi(strtok(NULL,"#"));

    if(newID != -1)
    {
      int newStock;

      newStock = atoi(strtok(NULL,"#"));

      if(newStock == 0) dialogueErreur("Achat", "stock insuffisant!");
      else
      {
        articleCourant.id = newID;
        articleCourant.stock = newStock - getQuantite();
        setlocale(LC_NUMERIC, "C"); //si je ne met pas cela, il ne convertie pas bien le string en float ex: 10.33 -> 10.00
        articleCourant.prix = atof(strtok(NULL,"#"));

        setArticle(articleCourant.intitule, articleCourant.prix, articleCourant.stock , articleCourant.image);

        //pour le panier

        if(i == 10) //si article n'existe pas dans le panier
        {
          i = 0;

          while(Caddie[i].id != 0 && i < 10) i++; //trouve le premier place libre

          Caddie[i].id = articleCourant.id;
          strcpy(Caddie[i].intitule, articleCourant.intitule);
          Caddie[i].prix = articleCourant.prix;
          Caddie[i].stock = getQuantite();
          strcpy(Caddie[i].image, articleCourant.image);
          
          ajouteArticleTablePanier(Caddie[i].intitule, Caddie[i].prix, Caddie[i].stock);
          
          totalCaddie = totalCaddie + (Caddie[i].stock*Caddie[i].prix);

          setTotal(totalCaddie);

          //mettre a jour le facture dans le BD
          sprintf(texte,"UPDATE_CAD#%d#0#0#%.3f#%d#%d", numFacture, totalCaddie, articleCourant.id, getQuantite());
  
          SendReceiveReq(texte, buffer);

          nbArticles++;
        }
        else
        {
          videTablePanier();

          Caddie[i].stock = Caddie[i].stock + getQuantite();
          int newStock = Caddie[i].stock;
          totalCaddie = 0.0;
          setTotal(-1.0);

          i = 0;

          while(i < nbArticles)
          {
            ajouteArticleTablePanier(Caddie[i].intitule, Caddie[i].prix, Caddie[i].stock);
            totalCaddie = totalCaddie + (Caddie[i].stock*Caddie[i].prix);

            i++;
          }

          setTotal(totalCaddie);

          //mettre a jour le facture dans le BD
          sprintf(texte,"UPDATE_CAD#%d#0#1#%.3f#%d#%d", numFacture, totalCaddie, articleCourant.id, newStock);
  
          SendReceiveReq(texte, buffer);
        }
      }
    }
    else
    {
      dialogueErreur("Achat", "Erreur");
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonSupprimer_clicked()
{
  int ind = getIndiceArticleSelectionne();

  if(ind == -1) dialogueErreur("PANIER", "AUCUN ARTICLE SELECTIONNE");
  else
  {
    char texte[100],buffer[100];
    sprintf(texte,"CANCEL#%d#%d", Caddie[ind].id, Caddie[ind].stock);
    
    int idArticle = Caddie[ind].id;

    SendReceiveReq(texte, buffer);

    char *ptr = strtok(buffer,"#");

    if (strcmp(ptr,"CANCEL") == 0) 
    {
      int newID;
    
      newID = atoi(strtok(NULL,"#"));

      if(newID != -1)
      {

        if(articleCourant.id == newID)
        {
          articleCourant.stock = atoi(strtok(NULL,"#"));

          setArticle(articleCourant.intitule, articleCourant.prix, articleCourant.stock , articleCourant.image);
        }

        // Suppression de l'aricle du panier
        if(Caddie[ind+1].id == 0 || ind == 9)
        {
          Caddie[ind].id = 0;
          nbArticles--;
        }
        else
        {
          while(Caddie[ind+1].id != 0 && ind < nbArticles-1)
          {
            Caddie[ind].id = Caddie[ind+1].id;
            strcpy(Caddie[ind].intitule, Caddie[ind+1].intitule);
            Caddie[ind].prix = Caddie[ind+1].prix;
            Caddie[ind].stock = Caddie[ind+1].stock;
            strcpy(Caddie[ind].image, Caddie[ind+1].image);

            ind++;
          }

          Caddie[ind].id = 0;
          
          nbArticles--;
        }

        //mise a jour du panier

        videTablePanier();

        totalCaddie = 0.0;
        setTotal(-1.0);

        int i = 0;

        while(i < nbArticles)
        {
          ajouteArticleTablePanier(Caddie[i].intitule, Caddie[i].prix, Caddie[i].stock);
          totalCaddie = totalCaddie + (Caddie[i].stock*Caddie[i].prix);

          i++;
        }

        setTotal(totalCaddie);

        char requeteCad[200], buf[50];

        //mettre a jour le facture dans le BD
        sprintf(requeteCad,"UPDATE_CAD#%d#1#%.3f#%d", numFacture, totalCaddie, idArticle);

        SendReceiveReq(requeteCad, buf);
      }
      else
      {
        dialogueErreur("Cancel", "Erreur");
      }
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonViderPanier_clicked()
{
  bool check;

  check = VidePanier();
  
  if(check == true) printf("check vide\n");

  char requeteCad[200], buf[50];

  //mettre a jour le facture dans le BD
  sprintf(requeteCad,"DELETE_CAD#%d", numFacture);

  SendReceiveReq(requeteCad, buf);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonPayer_clicked()
{
  char texte[1500];
  sprintf(texte,"CONFIRMER#%d", nbArticles);
  int nbEcrits;

  int i = 0;

  while(i < 10 && Caddie[i].id != 0)
  {
    char str[100];

    strcat(texte, "#");

    sprintf(str, "%d&%d&%.3f&%s", Caddie[i].id, Caddie[i].stock, Caddie[i].prix, Caddie[i].intitule);

    strcat(texte, str);

    i++;
  }

  if ((nbEcrits = Send(sClient,texte,strlen(texte))) == -1)
  {
    perror("Erreur de Send");
    exit(1);
  }

  printf("NbEcrits = %d\n",nbEcrits);
  printf("Ecrit = --%s--\n",texte);

  char buffer[100];
  int nbLus;
  
  if ((nbLus = Receive(sClient,buffer)) < 0)
  {
      perror("Erreur de Receive");
      exit(1);
  }
  
  printf("NbLus = %d\n",nbLus);
  buffer[nbLus] = 0;
  printf("Lu = --%s--\n",buffer);

  char *ptr = strtok(buffer,"#");

  if (strcmp(ptr,"CONFIRMER") == 0) 
  {
    char reponse[20];
    strcpy(reponse,strtok(NULL,"#"));

    if (strcmp(reponse,"ko") == 0) 
    {
      dialogueErreur("Payer", "Vous n'avez rien dans votre panier !");
    }
    else
    {
      if (strcmp(reponse,"-1") == 0) 
      {
        dialogueErreur("Payer", "un problème est survenu lors de l'obtention de votre numéro de reçu, mais votre commande a été confirmée !");
      }
      else
      {
        //mise a jour du panier

        videTablePanier();

        totalCaddie = 0.0;
        setTotal(-1.0);

        int i = 0;

        while(i < nbArticles)
        {
          if(Caddie[i].id == articleCourant.id)
          {
            articleCourant.stock += Caddie[i].stock;
            setArticle(articleCourant.intitule, articleCourant.prix, articleCourant.stock , articleCourant.image);
          }

          Caddie[i].id = 0;

          i++;
        }

        nbArticles = 0;

        char numFacture[50]; 

        strcpy(numFacture, "Achat confirmer! Numero de facture : ");
        strcat(numFacture, strtok(NULL,"#"));

        dialogueMessage("Payer", numFacture);
      }
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////

void WindowClient::setSocket(const int s)
{
  sClient = s;
  printf("socket creee = %d\n",sClient);
}

int WindowClient::getSocket()
{
  return sClient;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////

void WindowClient::ConsultArticle(int Id)
{
  char texte[50], buffer[100];
  sprintf(texte,"CONSULT#%d", Id);

  SendReceiveReq(texte, buffer);

  char *ptr = strtok(buffer,"#");

  if (strcmp(ptr,"CONSULT") == 0) 
  {
    int newID;
    
    newID = atoi(strtok(NULL,"#"));

    if (newID != -1) 
    {
      char intitule[20], image[20];
      float prix;
      int stock;

      strcpy(intitule,strtok(NULL,"#"));
      stock = atoi(strtok(NULL,"#"));
      setlocale(LC_NUMERIC, "C"); //si je ne met pas cela, il ne convertie pas bien le string en float ex: 10.33 -> 10.00
      prix = atof(strtok(NULL,"#"));
      strcpy(image,strtok(NULL,"#"));

      setArticle(intitule, prix, stock , image);

      articleCourant.id = newID;
      articleCourant.stock = stock;
      articleCourant.prix = prix;
      strcpy(articleCourant.intitule, intitule);
      strcpy(articleCourant.image, image);
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////

bool WindowClient::VidePanier()
{
  char texte[200], buffer[100];
  sprintf(texte,"CANCEL_ALL#%d", nbArticles);

  int i = 0;

  while(i < 10 && Caddie[i].id != 0)
  {
    char str[20];

    strcat(texte, "#");

    sprintf(str, "%d&%d", Caddie[i].id, Caddie[i].stock);

    strcat(texte, str);

    i++;
  }
  
  SendReceiveReq(texte, buffer);
  
  char *ptr = strtok(buffer,"#");

  if (strcmp(ptr,"CANCEL_ALL") == 0) 
  {
    char reponse[20];
    strcpy(reponse,strtok(NULL,"#"));

    if (strcmp(reponse,"ko") == 0) 
    {
      dialogueErreur("Vider", "Vous n'avez rien dans votre panier !");
    }
    else
    {
      //mise a jour du panier

      videTablePanier();

      totalCaddie = 0.0;
      setTotal(-1.0);

      int i = 0;

      while(i < nbArticles)
      {
        if(Caddie[i].id == articleCourant.id)
        {
          articleCourant.stock += Caddie[i].stock;
          setArticle(articleCourant.intitule, articleCourant.prix, articleCourant.stock , articleCourant.image);
        }

        Caddie[i].id = 0;

        i++;
      }

      nbArticles = 0;
    }
  }

  return true;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////

void SendReceiveReq(char* requete, char *buffer)
{
  int nbEcrits;

  if ((nbEcrits = Send(w->getSocket(),requete,strlen(requete))) == -1)
  {
    perror("Erreur de Send");
    exit(1);
  }

  printf("NbEcrits = %d\n",nbEcrits);
  printf("Ecrit = --%s--\n",requete);

  int nbLus;
  
  if ((nbLus = Receive(w->getSocket(),buffer)) < 0)
  {
      perror("Erreur de Receive");
      exit(1);
  }
  
  printf("NbLus = %d\n",nbLus);
  buffer[nbLus] = 0;
  printf("Lu = --%s--\n",buffer);
}