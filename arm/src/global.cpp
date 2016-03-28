#include "global.h"

volatile bool isSymmetry = false;
volatile bool marcheAvant;

Uart<2> serial_rb;
Uart<3> serial_ax;
AX<Uart<3>>* ax12;

volatile double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
volatile double orientation_odo; // exprimé en radians
volatile double cos_orientation_odo, sin_orientation_odo;
volatile double courbure_odo;

bool asserEnable;
volatile bool debugMode = false;
volatile bool needArrive = false;

SemaphoreHandle_t odo_mutex = xSemaphoreCreateMutex();
SemaphoreHandle_t consigneAsser_mutex = xSemaphoreCreateMutex();

std::vector<Hook*> listeHooks;

volatile bool ping = false;
volatile bool startOdo = false;
volatile bool matchDemarre = true; // TODO

MODE_ASSER modeAsserActuel = ASSER_OFF;
