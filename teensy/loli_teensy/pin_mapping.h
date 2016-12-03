#ifndef _PIN_MAPPING_h
#define _PIN_MAPPING_h

/* Encodeurs arri�res */
#define PIN_A_RIGHT_BACK_ENCODER	13
#define PIN_B_RIGHT_BACK_ENCODER	14
#define PIN_B_LEFT_BACK_ENCODER		15
#define PIN_A_LEFT_BACK_ENCODER		16


/* Encodeurs des moteurs de propultion */
#define PIN_B_LEFT_MOTOR_ENCODER	7
#define PIN_A_LEFT_MOTOR_ENCODER	8
#define PIN_A_RIGHT_MOTOR_ENCODER	11
#define PIN_B_RIGHT_MOTOR_ENCODER	12


/* Ponts en H des moteurs de propultion */
#define PIN_DIRECTION_LEFT_MOTOR	3
#define PIN_DIRECTION_RIGHT_MOTOR	4
#define PIN_PWM_LEFT_MOTOR			5
#define PIN_PWM_RIGHT_MOTOR			6


/* Ponts en H des moteurs du filet */
#define PIN_VENTILATEUR		17
#define PIN_MOTOR_NET		20
#define PIN_RIGHT_MOTOR_NET	22
#define PIN_LEFT_MOTOR_NET	23
#define PIN_NET_COM			21

/* Capteurs fin de course du filet */
#define PIN_BUTEE_G			A22
#define PIN_BUTEE_D			A21


/* Capteurs ToF : pin 'enable' */
#define PIN_TOF_FLAN_AV_G	24
#define PIN_TOF_FLAN_AR_G	25
#define PIN_TOF_PHARE_AV_G	26
#define PIN_TOF_FRONT		27
#define PIN_TOF_PHARE_AV_D	28
#define PIN_TOF_FLAN_AR_D	31
#define PIN_TOF_FLAN_AV_D	32
#define PIN_TOF_PHARE_AR_D	33
#define PIN_TOF_BACK		34
#define PIN_TOF_PHARE_AR_G	39


/* DELs */
#define PIN_DEL_STATUS_1	2
#define PIN_DEL_STATUS_2	29
#define PIN_FEUX_NUIT		30
#define PIN_CLIGNOTANT_D	35
#define PIN_FEUX_FREIN		36
#define PIN_CLIGNOTANT_G	37
#define PIN_FEUX_RECUL		38


/* Divers capteurs */
#define PIN_GET_COLOR		A25
#define PIN_GET_VOLTAGE		A11
#define PIN_GET_JUMPER		A10


#endif

