#ifndef ACTUATORSMGR_HPP
#define ACTUATORSMGR_HPP

#include <ax12.hpp>
#include <Uart.hpp>
#include <Singleton.hpp>

class ActuatorsMgr: public Singleton<ActuatorsMgr> {
private:
	typedef Uart<2> serial_ax;
	AX<serial_ax>* ax12;
public:
	ActuatorsMgr()
	{
		ax12 = new AX<serial_ax>(0, 1, 1023);

	}

	inline void monterBras() {
		ax12->goTo(100);
	}
};

#endif /* ACTUATORSMGR_HPP */
