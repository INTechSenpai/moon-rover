[![Build Status](https://travis-ci.org/INTechSenpai/moon-rover.svg?branch=master)](https://travis-ci.org/INTechSenpai/moon-rover)

# INTech Senpaï vous présente son nouveau robot : le moon rover !

![Poster de l'équipe](https://raw.githubusercontent.com/INTechSenpai/moon-rover/master/docs/poster.jpg)

## Coupe de France de robotique 2017

Le moon rover a participé à la [Coupe de France de robotique 2017](http://www.coupederobotique.fr/). Cette année-là, la compétition s'est passé sur la Lune, et notre brave rover a dû ramasser du minerai grâce au filet qu'il porte.

INTech Senpaï avait déjà participé à la Coupe de France de robotique 2016 avec le très remarqué Robot Sumo qui avait fini dans le premier tiers du classement avec ses 355 points.

Lors de la Coupe 2017, le rover est arrivé 29e avec 248 points, sur les 142 équipes participantes. Il a de plus gagné le prix de l'innovation pour sa manière unique de récupérer les balles et pour ses déplacements similaires à ceux d'une voiture autonome.

Retrouvez [la vidéo d'un de nos matchs](https://youtu.be/RRG_A8Ckaps?t=6781) ainsi qu'un [match amical en version télécommandée](https://www.youtube.com/watch?v=7kDDqBFoagk).

![Rover + prix](https://raw.githubusercontent.com/INTechSenpai/moon-rover/master/docs/prix.jpg)

## Une approche _roleplay_

Le projet fut dirigé par son objectif _roleplay_ : le moon rover est une reproduction aussi fidèle que possible du rover lunaire qui a accompagné les missions Apollo. Mais notre amour du détail ne s'est pas arrêté là, et notre rover a un tableau de bord et une multitude de phares : feux de croisement, de stop, de recul et bien sûr des clignotants.

Seul absent : le klaxon. Mais dans l'espace, personne ne vous entendra klaxonner.

## Un pathfinding courbe tentaculaire

Le moon rover se comporte comme une voiture : il a quatre roues, les deux roues avant étant à la fois motrices et directrices. Trouver un itinéraire pour le rover n'est pas une tâche facile : nous avons développé pour cela un pathfinding tentaculaire. L'idée est la suivante : le rover a une carte mentale du terrain et déploit des milliers de trajectoires différentes, appelées tentacules, pour trouver un chemin jusqu'à sa destination. Au final, le rover suit des courbes particulièrement élégantes !

Voici les tentacules déployés par le robot lorsqu'il cherche un chemin :

![Animation de la recherche tentaculaire](https://raw.githubusercontent.com/INTechSenpai/moon-rover/master/docs/cerisier.gif)

Le rover, quand il détecte un ennemi (les rectangles colorés), recherche un itinéraire alternatif tout en continuant de rouler (animation 2× plus rapide) :

![Animation de la replanification](https://raw.githubusercontent.com/INTechSenpai/moon-rover/master/docs/replanif.gif)

Si vous souhaitez utiliser ce pathfinding pour l'un de vos projets, rendez-vous sur la [page de la librairie associée](https://github.com/PFGimenez/The-Kraken-Pathfinding).

## Hardware

Le moon rover est équipé de deux processeurs :

- une [Teensy 3.5](https://www.pjrc.com/teensy/index.html) avec un processeur ARM Cortex-M4. Son rôle est de commander aux moteurs et de suivre la trajectoire qu'on lui donne. C'est le pilote du rover.
- une [Raspberry Pi 3](https://www.raspberrypi.org/products/raspberry-pi-3-model-b/) avec un processeur ARM quatre cœurs. Son rôle est de choisir où aller et de planifier les trajectoires. C'est le copilote du rover.

Le moon rover a 12 capteurs (10 capteurs _time of flight_ et 2 capteurs IR) qui lui permettent de voir dans toutes les directions. Sous le rover, on peut voir l'électronique qui le fait fonctionner :

![Électronique du rover](https://raw.githubusercontent.com/INTechSenpai/moon-rover/master/docs/elec.jpg)

## L'équipe

[Sylvain Gaultier](https://github.com/sylvaing19) est le fondateur d'INTech Senpaï. Il s'est occupé de la conception et de la réalisation de la mécanique et de l'électronique du rover. Il est également le programmeur de la Teensy.

[Pierre-François Gimenez](https://github.com/PFGimenez) a rejoint INTech Senpaï cette année. Il a conçu et programmé la recherche de chemin tentaculaire qui est le fruit de deux ans et demi de travail. C'est le programmeur de la Raspberry Pi.

