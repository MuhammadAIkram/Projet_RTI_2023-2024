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

int IdCourant, StockCourant;
char IntituleCourant[20], ImageCourant[20];
float PrixCourant;

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

  char texte[100];
  sprintf(texte,"LOGIN#%s#%s#%d", getNom(), getMotDePasse(), isNouveauClientChecked());
  int nbEcrits;

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

  if (strcmp(ptr,"LOGIN") == 0) 
  {
    char reponse[20], message[100];
    strcpy(reponse,strtok(NULL,"#"));

    if (strcmp(reponse,"ok") == 0) 
    {
      if(isNouveauClientChecked() == 1) dialogueMessage("Login", "Vous avez été inscrit avec succès");

      dialogueMessage("Login", "Vous êtes connecté avec succès");

      loginOK();

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
  char texte[50];
  sprintf(texte,"LOGOUT");
  int nbEcrits;

  if ((nbEcrits = Send(sClient,texte,strlen(texte))) == -1)
  {
    perror("Erreur de Send");
    exit(1);
  }

  printf("NbEcrits = %d\n",nbEcrits);
  printf("Ecrit = --%s--\n",texte);

  char buffer[50];
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

  if (strcmp(ptr,"LOGOUT") == 0) 
  {
    char reponse[20];
    strcpy(reponse,strtok(NULL,"#"));

    if (strcmp(reponse,"ok") == 0) 
    {
      dialogueMessage("Logout", "BYE BYE ;)");

      logoutOK();
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonSuivant_clicked()
{
  ConsultArticle(IdCourant+1);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonPrecedent_clicked()
{
  ConsultArticle(IdCourant-1);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonAcheter_clicked()
{
  char texte[100];
  sprintf(texte,"ACHAT#%d#%d", IdCourant, getQuantite());
  int nbEcrits;

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
        IdCourant = newID;
        StockCourant = newStock - getQuantite();
        setlocale(LC_NUMERIC, "C"); //si je ne met pas cela, il ne convertie pas bien le string en float ex: 10.33 -> 10.00
        PrixCourant = atof(strtok(NULL,"#"));

        setArticle(IntituleCourant, PrixCourant, StockCourant , ImageCourant);
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

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonViderPanier_clicked()
{

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void WindowClient::on_pushButtonPayer_clicked()
{

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
  char texte[50];
  sprintf(texte,"CONSULT#%d", Id);
  int nbEcrits;

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

      IdCourant = newID;
      StockCourant = stock;
      PrixCourant = prix;
      strcpy(IntituleCourant, intitule);
      strcpy(ImageCourant, image);
    }
  }
}