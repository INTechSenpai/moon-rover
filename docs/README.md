[![Build Status](https://travis-ci.org/INTechSenpai/moon-rover.svg?branch=master)](https://travis-ci.org/INTechSenpai/moon-rover)

# INTech Senpaï vous présente son nouveau robot : le moon rover !

![Poster de l'équipe](https://raw.githubusercontent.com/INTechSenpai/moon-rover/master/docs/poster.png)

## Coupe de France de robotique 2017

Le moon rover participe à la [Coupe de France de robotique 2017](http://www.coupederobotique.fr/). Cette année, la compétition se passe sur la Lune, et notre brave rover va devoir ramasser du minerai grâce au filet qu'il a à l'arrière.

INTech Senpaï avait participé à la Coupe de France de robotique 2016 avec le très remarqué Robot Sumo qui avait fini dans le premier tiers du classement avec ses 355 points.

## Un pathfinding courbe tentaculaire

Le moon rover se comporte comme une voiture : il a quatre roues, les deux roues avant étant à la fois motrices et directrices. Trouver un itinéraire pour le rover n'est pas une tâche facile : nous avons développé pour cela un pathfinding tentaculaire, qui essaye plein de trajectoires différentes. Au final, le rover suit des trajectoires particulièrement élégantes !

![Animation de la recherche tentaculaire](https://raw.githubusercontent.com/INTechSenpai/moon-rover/master/docs/cerisier.gif)

## Hardware

Le moon rover a deux processeurs :

- une Teensy 3.5 avec un processeur ARM Cortex-M4. Son rôle est de commander aux moteurs et de suivre la trajectoire qu'on lui donne. C'est le pilote du rover.
- une Raspberry Pi 3 avec un processeur ARM quatre cœurs. Son rôle est de choisir où aller et de planifier les trajectoires. C'est le copilote du rover.

De plus, afin de voir son environnement, le moon rover a 10 capteurs qui lui permettent de voir dans toutes les directions.

