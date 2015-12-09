/*
 * Fonction haut niveau de déplacement (fonctions bloquantes, gestion des blocages, de la symétrie, …).
 */

void computeDirection(int x_consigne, int y_consigne)
{
    if(strategy == FORCE_BACK_MOTION)
        marcheAvant = false;
    else if(strategy == FORCE_FORWARD_MOTION)
        marcheAvant = true;
    else
    {
        marcheAvant = (x_consigne - x_odo) * cos_orientation_odo + (y_consigne - y_odo) * sin_orientation_odo > 0;
    }
}


void gestionCollision()
{
    


}
