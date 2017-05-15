-------------------
      Résumé
-------------------

Ce projet a été réalisé uniquement par CHARPIGNON Thibault.

Le but de ce projet était reproduire un jeu de societé nommé "Bubblee Pop" en intégrant 
l'OpenGL vue en cours avec de l'Android natif.

-------------------
 Comment jouer ?
-------------------

ATTENTION: Si lorsque vous cliquez sur le bouton jouer et qu'un écran noir s'affiche relancez
l'application.

Lorsque vous cliquez sur l'application vous arrivez directement une une page de menu où
il vous est possible de choisir d'activer ou non les pouvoir et de jouer.

Lorsque vous cliquez sur jouer, vous arrivez sur le plateau contenant toutes les billes.
Si l'écran est rose c'est au joueur du haut (à gauche quand vous tenez votre tablette 
horizontalement) si il est bleu c'est à celui qui est en bas (à droite quand vous tenez 
votre tablette verticalement).

chaque joueur dispose d'une zone de score sur le coté et du contenu de sa planète.
Entre les deux planètes se trouve le ciel le ciel et le bouton multicolor est la pioche.

Un emplacement vide est de couleur grise, si il est plein il a la couleur du bubblee qui l'occupe.

Un tour d'un joueur correspond à :
	- il pioche pour remplir le ciel
	- il touche un bubblee du ciel pour soit faire tomber verticalement soit horizontalement un 
	couple de bubblee. Si le joueur touche l'étage du dessus du ciel il fait tomber le bubblee 
	touché et celui du dessous. Si il touche l'étage du bas il fait tomber le bubblee touché et 
	celui à sa gauche.
	- lorsque un bubblee est touché il tombe sur la planète du joueur. Si une réduction est 
	possible elle est faite automatiquement et c'est au joueur suivant de jouer.

Dans le cas ou les pouvoirs sont activés si un joueur aligne deux fois de suite 3 bubblee ou plus  
il a la possibilité d'activer un pouvoir en cliquant sur la couleur du pouvoir souhaité dans la 
zone de score. Il active sont pouvoir (la règle du pouvoir s'affiche) et il l'execute puis c'est 
à l'autre joueur de jouer.

ATTENTION: le message du pouvoir n'apparait qu'une seul fois.

La partie se termine quand la pioche est vide ou qu'une planète est vide. Vous voyez à la fin 
la couleur du joueur qui a gagné, soit violet soit bleu.

ATTENTION: une fois arrivé sur l'écran de fin pour relancer une partie il faut fermer completement 
l'application et la relancer entièrement pour rejouer.

-------------------
Structure du Projet
-------------------

Le projet à été structuré en suivant le pattern MVC qui colle parfaitement au develloppement de jeux 
et qui permet à court terme d'agrémenter le jeu de fonctionnalités diverses très facilement.
Grace à MVC il vous est possible de changer la vue sans vous soucier du modèle il faudra juste 
réimplementer un controller et la vue souhaité.

-------------------
  Fonctionnalité
-------------------

Toutes les fonctionnalités du jeux multijoueur de BubblePop ont été implémentés. A celles-ci j'ai 
rajouté une gestion des messages d'erreurs et d'informations sur le jeux qui sont orientés en 
fonction de celui qui joue et qui permettent aux joueurs de bien suivre le déroulement du jeux. 
J'ai également pris le soint d'ajouter une page de menu pour gerer les pouvoirs et le debut de 
la partie. Je n'ai pas voulu rajouter d'historique des partie car je pensais coller au plus près 
de l'esprit jeu de société, c'est à dire à la fin de la partie on efface tous et on recommence.

-------------------
 Problème Rencontré
-------------------

Les majeurs problêmes ont été de bien comprendre la librairie OpenGl dans un premier temps et 
surtout de comprendre comment allier le pattern MVC à l'OpenGl facillement. Une fois que j'ai 
trouvé comment faire, il m'a suffit d'implementer petit à petit les fonctionnalités jusqu'à avoir 
un jeu normalement parfaitement fonctionnel.

-------------------
Idée d'amélioration
-------------------

Pour améliorer le jeu on pourrait créer une seconde vue avec un controller approprié pour implementer 
le mode solo. Les méthodes étant déjà implémentés il suffirait d'ajouter un bouton pour jouer en solo 
avec une gestion des niveaux et on aurait ce mode jouable et fonctionnel. C'est pour cela que j'ai 
choisis le pattern MVC.
