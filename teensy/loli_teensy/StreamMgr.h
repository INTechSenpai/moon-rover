/*
	Gestionnaire du Stream de position et des capteurs
	(Permet notamment d'arr�ter l'envoi des donn�es via un ordre HL)
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

