#ifndef MANAGERMGR_H
#define MANAGERMGR_H

#include "ActuatorsMgr.hpp"

class ManagerMgr {
private:
	ActuatorsMgr* actuatorsMgr;

public:
	ManagerMgr();

	ActuatorsMgr* getActuatorsMgr();
};
#endif /* MANAGERMGR_H */
