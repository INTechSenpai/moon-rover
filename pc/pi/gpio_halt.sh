#!/bin/bash
if [ "$#" -ne 2 ]; then
    echo "Usage: ./gpio_halt.sh pinDiode pinHalt"
else
    gpio write $1 1 # allumage de la diode témoin

    # on s'arrête si la pin passe à l'état haut
    # comme ça, si le bouton se décroche à cause d'une collision avec RCVA
    # la raspbe ne s'éteint pas
    while [ $(gpio read $2) -eq 0 ]
    do
        sleep 2
    done
    # la diode s'éteint car la raspberry est coupée
    echo "Arrêt du système par GPIO !"
    halt
fi
