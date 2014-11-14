Bienvenue dans le moteur de recherche old-gen D’lul programmé par Francky, Pierre et Yohann

Pour l’utiliser, vous avez besoin :
- du .jar executable, ou de l’arborescence avec les fichiers sources .java
- un fichier de mots vides stop.txt (aux cotés du .jar)
- un fichier config.ini (aux cotés du .jar) (il peut être vide mais doit exister)
- de l’accès au répertoire des ressources
( /projet/iri/blondy_barussaud_petiot/ par défaut, configurable dans le .ini)
C’est dans ce dossier que l’index sera écrit
- d’un corpus de documents HTML (extension en .html) accompagnés de leur version épurée des balises en .txt
(/public/iri/projetIRI/corpus/ par défaut, configurable dans le .ini)
- de Mozilla Firefox pour ouvrir les pages renvoyées par la recherche

Pour une utilisation simple et rapide (si le corpus a déjà été indexé): 
- Lancer le programme
- Appuyer sur 4 puis Entrée
- Enjoy
- If you like it, buy it !

Si le corpus (par défaut) n’a pas été indexé :
- Lancer le programme
- Appuyer sur 1 puis Entrée
- Attendre
- Appuyer sur 2 puis Entrée
- Attendre
- Appuyer sur 3 puis Entrée
- Attendre
- Appuyer sur 4 puis Entrée

Utilisation Avancée :

Le fichier config.ini, peut être utilisé pour modifier  :
- le nombre de documents à indéxer.
- le nombre de threads.
- le chemin vers le corpus.
- le chemin vers le fichier des mots vides.
- le chemin vers le dossier où est/sera stocker l’index.
Il est lu au lancement du programme et certaines variables sont remplies 

Une fois le programme lancé, il lit le fichier .ini puis vous avez accès à un menu en console :
Il faut alors rentrer un chiffre et appuyer sur entrée:
- 1 Création des fichiers .corpus (liste des fichier à traiter).
- 2 lance l’indexation d’un corpus de la taille spécifiée dans le .ini utilisant le nombre de threads spécifiés au même endroit 
(1 000 000 et 4 par défaut)
!!Le temps requis pour cette opération varie grandement en fonction de la machine et de la taille du corpus
(environ 1h30 pour la configuration par défaut)!!
- 3 ajoute les poids des mots pour chaque document dans l’index, crée le fichier contenant le poids total de chaque document
- 4 ouvre la fenêtre de recherche et ferme le menu
- 5 supprime tout ce que contient le répertoire des ressources 
- 6 quitte le menu et le programme

Utilisation de la fenetre :
Dans la fenêtre vous avez le choix entre une recherche sans normalisation  (tokenizer) ou avec racinisation (stemmer).
La racinisation utilise uniquement la racine des mots, ce qui permet une recherche sur les mots de la même famille.
Vous pouvez rentrez les mots désirés dans l’espace prévu à cet effet puis cliquer sur le bouton recherche.
Les résultats seront triés par score décroissant et les 30 premiers affichés dans la fenêtre.
En double-cliquant sur une adresse, la page html s’ouvre dans le navigateur Firefox.

Bon voyage !
