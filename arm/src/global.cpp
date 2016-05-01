#include "global.h"

volatile bool isSymmetry = false;
volatile bool marcheAvant;

Uart<2> serial_rb;
Uart<3> serial_ax;
AX<Uart<3>>* ax12[NB_AX12];

volatile double x_odo, y_odo; // abscisse et ordonn�e exprim�es en mm
volatile double orientation_odo; // exprim� en radians
volatile double cos_orientation_odo, sin_orientation_odo;
volatile double courbure_odo;

bool asserEnable;
volatile bool debugMode = false;
volatile bool needArrive = false;

SemaphoreHandle_t odo_mutex = xSemaphoreCreateMutex();
SemaphoreHandle_t consigneAsser_mutex = xSemaphoreCreateMutex();

volatile int16_t asserVitesseGauche;
volatile int16_t asserVitesseDroite;

std::vector<Hook*> listeHooks;

volatile bool ping = false;
volatile bool startOdo = false;
volatile bool matchDemarre = true; // TODO
volatile bool pauseAsser = false;

MODE_ASSER modeAsserActuel = ASSER_OFF;
