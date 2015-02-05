#include "ManagerMgr.h"

ManagerMgr::ManagerMgr() {
	actuatorsMgr = &ActuatorsMgr::Instance();
}

ActuatorsMgr* ManagerMgr::getActuatorsMgr() {
	return actuatorsMgr;
}
