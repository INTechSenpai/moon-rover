#include "global.h"

bool isSymmetry;
bool marcheAvant;
Uart<2> serial_rb;
double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
double orientation_odo; // exprimé en radians
double cos_orientation_odo, sin_orientation_odo;
bool asserEnable;
bool debugMode = false;
bool needArrive = false;
SemaphoreHandle_t odo_mutex = xSemaphoreCreateMutex();
SemaphoreHandle_t consigneAsser_mutex = xSemaphoreCreateMutex();
double courbure_odo;
std::vector<Hook*> listeHooks;
Uart<3> serial_ax;
AX<Uart<3>>* ax12;
volatile bool ping = false;
volatile bool startOdo = false;
volatile bool matchDemarre = true; // TODO

MODE_ASSER modeAsserActuel = ASSER_OFF;
