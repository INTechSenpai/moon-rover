#ifndef TH_SERIE
#define TH_SERIE

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include "diag/Trace.h"

#include "math.h"
#include "Timer.h"
#include "FreeRTOS.h"
#include "task.h"
#include "Hook.h"
#include "Uart.hpp"
#include "global.h"
#include "ax12.hpp"
#include "serialProtocol.h"
#include "asserSimple.hpp"
#include "serie.h"

using namespace std;

/**
 * Thread qui écoute la série
 */
void thread_ecoute_serie(void*)
{

	/**
	 * Initialisation des séries
	 */
	serial_rb.init(460800, UART_MODE_TX_RX);
	serial_ax.init(57600, UART_MODE_TX);
	ax12 = new AX<Uart<3>>(0, 0, 1023);

	uint16_t idDernierPaquet = -1;
	Hook* hookActuel;
	uint8_t nbcallbacks;
	uint16_t id;

		while(1)
		{
			if(serial_rb.available())
			{
				unsigned char lecture[50];
				unsigned char entete;
				uint8_t index = 0;
				// Vérification de l'entête

//				sendPong();
//				askResend(0);
//				serial_rb.send_char(0x99);
				serial_rb.read_char(&entete);
//				serial_rb.send_char(entete);
				if(entete != 0x55)
				{
//				serial_rb.send_char(0x88);
					continue;
				}
//				serial_rb.send_char(0x10);

				serial_rb.read_char(&entete);
				if(entete != 0xAA)
				{
//					serial_rb.send_char(0x89);

					continue;
				}
//			   serial_rb.send_char(0x11);

//				serial_rb.send_char(0x10);
				// Récupération de l'id
				serial_rb.read_char(lecture); // id point fort
//				serial_rb.send_char(0x11);
				serial_rb.read_char(lecture+(++index)); // id point faible
//				serial_rb.send_char(0x12);
				uint16_t idPaquet = (lecture[ID_FORT] << 8) + lecture[ID_FAIBLE];

				// On redemande les paquets manquants si besoin est
				if(idPaquet > idDernierPaquet)
				{
					idDernierPaquet++; // id paquet théoriquement reçu
					while(idPaquet > idDernierPaquet)
						askResend(idDernierPaquet++);
				}

				// Si on reçoit un ID ancien… on fait comme si de rien n'était

				serial_rb.read_char(lecture+(++index)); // lecture de la commande

//				serial_rb.send_char(lecture[COMMANDE]);

				if(lecture[COMMANDE] == IN_PING_NEW_CONNECTION)
				{
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						idDernierPaquet = idPaquet; // on réinitialise le numéro des paquets
						sendPong();
					}
				}
				else if(lecture[COMMANDE] == IN_PING)
				{
					serial_rb.read_char(lecture+(++index));
					// Cas particulier. Pas de checksum
					sendPong();
					if(ping == false)
					{
						ping = true;
						HAL_GPIO_WritePin(GPIOC, GPIO_PIN_15, GPIO_PIN_SET); // on allume la led de ping C15
					}
				}
				else if(lecture[COMMANDE] == IN_DEBUG_MODE)
				{
					serial_rb.read_char(lecture+(++index));
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
						debugMode = true;
				}
				else if(lecture[COMMANDE] == IN_ACTIONNEURS)
				{
					serial_rb.read_char(lecture+(++index));
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
						ax12->goTo(lecture[PARAM]);
				}
				else if(lecture[COMMANDE] == IN_STOP)
				{
					serial_rb.read_char(lecture+(++index));
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						while(xSemaphoreTake(consigneAsser_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
						modeAsserActuel = STOP;
						xSemaphoreGive(consigneAsser_mutex);
					}
				}

				else if(lecture[COMMANDE] == IN_TOURNER)
				{
					serial_rb.read_char(lecture+(++index));
					serial_rb.read_char(lecture+(++index));
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						uint16_t angle = (lecture[PARAM] << 8) + lecture[PARAM + 1];

						while(xSemaphoreTake(consigneAsser_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
						rotationSetpoint = angle;
						needArrive = true;
						modeAsserActuel = ROTATION;
						xSemaphoreGive(consigneAsser_mutex);
//						vTaskDelay(1000);
//						sendArrive();
					}
				}
				else if(lecture[COMMANDE] == IN_AVANCER)
				{
					serial_rb.read_char(lecture+(++index));
					serial_rb.read_char(lecture+(++index));
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						uint16_t distance = (lecture[PARAM] << 8) + lecture[PARAM + 1];

						while(xSemaphoreTake(consigneAsser_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
						needArrive = true;
						modeAsserActuel = VA_AU_POINT;
						consigneX = cos_orientation_odo * distance + x_odo;
						consigneY = sin_orientation_odo * distance + y_odo;
						xSemaphoreGive(consigneAsser_mutex);
					}
				}
				else if(lecture[COMMANDE] == IN_VA_POINT)
				{
					serial_rb.read_char(lecture+(++index)); // x
					serial_rb.read_char(lecture+(++index)); // xy
					serial_rb.read_char(lecture+(++index)); // y
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						int16_t x = (lecture[PARAM] << 4) + (lecture[PARAM + 1] >> 4);
						x -= 1500;
						int16_t y = ((lecture[PARAM + 1] & 0x0F) << 8) + lecture[PARAM + 2];

						while(xSemaphoreTake(consigneAsser_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
						needArrive = true;
						modeAsserActuel = VA_AU_POINT;
						consigneX = x;
						consigneY = y;
						xSemaphoreGive(consigneAsser_mutex);
					}
				}
				else if((lecture[COMMANDE] & 0xFE) == IN_ARC)
				{
					serial_rb.read_char(lecture+(++index)); // x
					serial_rb.read_char(lecture+(++index)); // xy
					serial_rb.read_char(lecture+(++index)); // y
					serial_rb.read_char(lecture+(++index)); // orientation
					serial_rb.read_char(lecture+(++index)); // orientation
					serial_rb.read_char(lecture+(++index)); // courbure
					serial_rb.read_char(lecture+(++index)); // courbure
					serial_rb.read_char(lecture+(++index)); // vitesse

					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						while(xSemaphoreTake(consigneAsser_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);

						int16_t x = (lecture[PARAM] << 4) + (lecture[PARAM + 1] >> 4);
						x -= 1500;
						int16_t y = ((lecture[PARAM + 1] & 0x0F) << 8) + lecture[PARAM + 2];
						uint32_t angle = (lecture[PARAM + 3] << 8) + lecture[PARAM + 4];
						float courbure = lecture[PARAM + 3] + lecture[PARAM + 4] / 16.;
						uint8_t vitesse = lecture[PARAM + 5];
						trajectoire[indiceTrajectoireEcriture].x = x;
						trajectoire[indiceTrajectoireEcriture].y = y;
						trajectoire[indiceTrajectoireEcriture].courbure = courbure;
						trajectoire[indiceTrajectoireEcriture].orientation = angle; // TODO si marche arrière ajouter PI/2 ?
						trajectoire[indiceTrajectoireEcriture].vitesse = vitesse;
						trajectoire[indiceTrajectoireEcriture].dir_x = 1000 * cos(angle);
						trajectoire[indiceTrajectoireEcriture].dir_y = 1000 * sin(angle);

						// TODO réinitialiser les indices indiceArretEcriture et indiceArretLecture

						// On conserve l'arc précédent en arc d'arrêt si c'est explicitement demandé et
						// si l'arc qu'on vient d'obtenir n'est pas le premier
						if(lecture[COMMANDE] != IN_ARC && modeAsserActuel == COURBE)
							indiceArretEcriture++;

						// Dans tous les cas, on s'arrête au dernier arc reçu
						arcsArret[indiceArretEcriture] = &trajectoire[indiceTrajectoireEcriture];
						needArrive = true;

						modeAsserActuel = COURBE;
						xSemaphoreGive(consigneAsser_mutex);
					}
				}
                else if((lecture[COMMANDE] & IN_PID_CONST_MASQUE) == IN_PID_CONST_VIT_GAUCHE)
				{
					serial_rb.read_char(lecture+(++index)); // kp
					serial_rb.read_char(lecture+(++index)); // kp
					serial_rb.read_char(lecture+(++index)); // kd
					serial_rb.read_char(lecture+(++index)); // kd
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						float kp = ((lecture[PARAM] << 8) + lecture[PARAM + 1])/1000.;
						float kd = ((lecture[PARAM + 2] << 8) + lecture[PARAM + 3])/1000.;
						if(lecture[COMMANDE] == IN_PID_CONST_VIT_GAUCHE)
							leftSpeedPID.setTunings(kp, 0., kd);
						else if(lecture[COMMANDE] == IN_PID_CONST_VIT_DROITE)
							rightSpeedPID.setTunings(kp, 0., kd);
						else if(lecture[COMMANDE] == IN_PID_CONST_TRANSLATION)
							translationPID.setTunings(kp, 0., kd);
						else if(lecture[COMMANDE] == IN_PID_CONST_ROTATION)
							rotationPID.setTunings(kp, 0., kd);
						else if(lecture[COMMANDE] == IN_PID_CONST_COURBURE)
							PIDvit.setTuningsC(kp, 0., kd);
						else if(lecture[COMMANDE] == IN_PID_CONST_VIT_LINEAIRE)
							PIDvit.setTuningsV(kp, 0., kd);
						else if(lecture[COMMANDE] == IN_CONST_SAMSON)
						{
							k1 = kp;
							k2 = kp;
						}
					}
				}
				else if(lecture[COMMANDE] == IN_INIT_ODO)
				{
					serial_rb.read_char(lecture+(++index)); // x
					serial_rb.read_char(lecture+(++index)); // xy
					serial_rb.read_char(lecture+(++index)); // y
					serial_rb.read_char(lecture+(++index)); // o
					serial_rb.read_char(lecture+(++index)); // o

					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						int16_t x = (lecture[PARAM] << 4) + (lecture[PARAM + 1] >> 4);
						x -= 1500;
						uint16_t y = ((lecture[PARAM + 1] & 0x0F) << 8) + lecture[PARAM + 2];
						uint16_t o = (lecture[PARAM + 3] << 8) + lecture[PARAM + 4];

						if(!startOdo)
						{
							x_odo = x;
							y_odo = y;
							orientation_odo = o/1000.;

							// On l'asservit sur place
							modeAsserActuel = VA_AU_POINT;
							consigneX = x;
							consigneY = y;
		//					orientationTick = RAD_TO_TICK(parseInt(lecture, &(++index))/1000.);
	//						serial_rb.printfln("%d",(int)orientationTick);
	//						serial_rb.printfln("%d",(int)(TICK_TO_RAD(orientationTick)*1000));
							startOdo = true;
						}
					}
//					else
//						serial_rb.printfln("ERR_ODO",(int)orientationTick);
				}

				// POUR TEST UNIQUEMENT
				else if(lecture[COMMANDE] == IN_GET_XYO)
				{
// TODO
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
				}
				else if(lecture[COMMANDE] == IN_RESEND_PACKET)
				{
					serial_rb.read_char(lecture+(++index)); // id
					serial_rb.read_char(lecture+(++index)); // id
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
					{
						uint16_t id = (lecture[PARAM] << 8) + lecture[PARAM + 1];
						resend(id);
					}
				}
				else if(lecture[COMMANDE] == IN_REMOVE_ALL_HOOKS)
				{
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
					else
						listeHooks.clear();
				}
				else if(lecture[COMMANDE] == IN_REMOVE_SOME_HOOKS)
				{
					serial_rb.read_char(lecture+(++index)); // nb
					uint8_t nbIds = lecture[PARAM];
					vector<Hook*>::iterator it = listeHooks.begin();

					for(uint8_t i = 0; i < nbIds; i++)
					{
						serial_rb.read_char(lecture+(++index)); // date
						uint16_t id = lecture[PARAM + 1 + i];

						for(uint8_t j = 0; j < listeHooks.size(); j++)
						{
							Hook* hook = listeHooks[j];
							if(hook->getId() == id)
							{
								vPortFree(hook);
								listeHooks[j] = listeHooks.back();
								listeHooks.pop_back();
								break; // l'id est unique
							}
						}

					}
					if(!verifieChecksum(lecture, index))
						askResend(idPaquet);
				}
				else if((lecture[COMMANDE] & IN_HOOK_MASK) == IN_HOOK_GROUP)
				{
					if(lecture[COMMANDE] == IN_HOOK_DATE)
					{
						serial_rb.read_char(lecture+(++index)); // date
						serial_rb.read_char(lecture+(++index)); // date
						serial_rb.read_char(lecture+(++index)); // date
						serial_rb.read_char(lecture+(++index)); // id
						serial_rb.read_char(lecture+(++index)); // nb_callback

						uint32_t date = (lecture[PARAM] << 16) + (lecture[PARAM + 1] << 8) + lecture[PARAM + 2];
						id = lecture[PARAM + 3];
						nbcallbacks = lecture[PARAM + 4];
						hookActuel = new(pvPortMalloc(sizeof(HookTemps))) HookTemps(id, nbcallbacks, date);
						listeHooks.push_back(hookActuel);
					}
					else if((lecture[COMMANDE] & 0xFE) == IN_HOOK_CONTACT)
					{
						serial_rb.read_char(lecture+(++index)); // nb_capt
						serial_rb.read_char(lecture+(++index)); // id
						serial_rb.read_char(lecture+(++index)); // nb_callback
						uint8_t nbContact = lecture[PARAM];
						bool unique = lecture[COMMANDE] == IN_HOOK_CONTACT_UNIQUE;
						id = lecture[PARAM + 1];
						nbcallbacks = lecture[PARAM + 2];
						hookActuel = new(pvPortMalloc(sizeof(HookContact))) HookContact(id, unique, nbcallbacks, nbContact);
						listeHooks.push_back(hookActuel);
					}
					else if(lecture[COMMANDE] == IN_HOOK_DEMI_PLAN)
					{
						serial_rb.read_char(lecture+(++index)); // x point
						serial_rb.read_char(lecture+(++index)); // xy point
						serial_rb.read_char(lecture+(++index)); // y point
						serial_rb.read_char(lecture+(++index)); // x direction
						serial_rb.read_char(lecture+(++index)); // xy direction
						serial_rb.read_char(lecture+(++index)); // y direction
						serial_rb.read_char(lecture+(++index)); // id
						serial_rb.read_char(lecture+(++index)); // nb_callback

						int16_t x = (lecture[PARAM] << 4) + (lecture[PARAM + 1] >> 4);
						x -= 1500;
						uint16_t y = ((lecture[PARAM + 1] & 0x0F) << 8) + lecture[PARAM + 2];

						int16_t dir_x = (lecture[PARAM + 3] << 4) + (lecture[PARAM + 4] >> 4);
						dir_x -= 1500;
						uint16_t dir_y = ((lecture[PARAM + 4] & 0x0F) << 8) + lecture[PARAM + 5];
						id = lecture[PARAM + 6];
						nbcallbacks = lecture[PARAM + 7];
						hookActuel = new(pvPortMalloc(sizeof(HookDemiPlan))) HookDemiPlan(id, nbcallbacks, x, y, dir_x, dir_y);
						listeHooks.push_back(hookActuel);
					}
					else if((lecture[COMMANDE] & 0xFE) == IN_HOOK_POSITION)
					{
						serial_rb.read_char(lecture+(++index)); // x
						serial_rb.read_char(lecture+(++index)); // xy
						serial_rb.read_char(lecture+(++index)); // y
						serial_rb.read_char(lecture+(++index)); // rayon
						serial_rb.read_char(lecture+(++index)); // rayon

						int16_t x = (lecture[PARAM] << 4) + (lecture[PARAM + 1] >> 4);
						x -= 1500;
						uint16_t y = ((lecture[PARAM + 1] & 0x0F) << 8) + lecture[PARAM + 2];

						uint32_t tolerance = (lecture[PARAM + 3] << 8) + lecture[PARAM + 4];
						id = lecture[PARAM + 5];
						nbcallbacks = lecture[PARAM + 6];
						hookActuel = new(pvPortMalloc(sizeof(HookPosition))) HookPosition(id, nbcallbacks, x, y, tolerance);
						listeHooks.push_back(hookActuel);
					}
					else
						continue;

					for(int i = 0; i < nbcallbacks; i++)
					{
						serial_rb.read_char(lecture+(++index)); // callback
						if(((lecture[index]) & IN_CALLBACK_MASK) == IN_CALLBACK_ELT)
						{
							uint8_t nbElem = lecture[index] & ~IN_CALLBACK_MASK;
							Exec_Update_Table* tmp = new(pvPortMalloc(sizeof(Exec_Update_Table))) Exec_Update_Table(nbElem);
							hookActuel->insert(tmp, i);
						}
						else if(((lecture[index]) & IN_CALLBACK_MASK) == IN_CALLBACK_SCRIPT)
						{
							uint8_t nbScript = lecture[index] & ~IN_CALLBACK_MASK;
							Exec_Script* tmp = new(pvPortMalloc(sizeof(Exec_Script))) Exec_Script(nbScript);
							hookActuel->insert(tmp, i);
						}
						else if(((lecture[index]) & IN_CALLBACK_MASK) == IN_CALLBACK_AX12)
						{
//							uint8_t nbAct = lecture[index] & ~IN_CALLBACK_MASK;
							serial_rb.read_char(lecture+(++index));
							serial_rb.read_char(lecture+(++index));
							uint16_t angle = (lecture[index - 1] << 8) + lecture[index];
							Exec_Act* tmp = new(pvPortMalloc(sizeof(Exec_Act))) Exec_Act(ax12, angle);
							hookActuel->insert(tmp, i);
						}
					}
				}
			}

				//					serial_rb.printfln("color rouge");

			else // on n'attend que s'il n'y avait rien. Ainsi, si la série prend du retard elle n'attend pas pour traiter toutes les données entrantes suivantes
			{
//				vTaskDelay(1);
			}
		}
//			serial_rb.printfln("%d", TIM5->CNT);

}

#endif
