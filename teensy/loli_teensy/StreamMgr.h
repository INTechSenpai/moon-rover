/*
	Gestionnaire du Stream de position et des capteurs
	(Permet notamment d'arrêter l'envoi des données via un ordre HL)
*/
#ifndef _STREAMMGR_h
#define _STREAMMGR_h

#include "Singleton.h"

class StreamMgr : public Singleton<StreamMgr>
{
public:
	StreamMgr()
	{
		running = false;
	}
	bool running;
};


#endif

