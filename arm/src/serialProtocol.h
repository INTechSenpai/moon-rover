#ifndef PROTOCOLE
#define PROTOCOLE

#define IN_PING 0x3F
#define IN_PING_NEW_CONNECTION 0x40
#define IN_PONG1 0x42
#define IN_PONG2 0x57
#define IN_VITESSE 0x03
#define IN_TOURNER 0x04
#define IN_VA_POINT 0x05
#define IN_ACTIONNEURS 0x06
#define IN_STOP 0x07
#define IN_INIT_ODO 0x08
#define IN_GET_XYO 0x09
#define IN_SET_VITESSE 0x0A

#define IN_AVANCER_MASQUE 0xFC
#define IN_AVANCER 0x0C
#define IN_AVANCER_NEG 0x0D
#define IN_AVANCER_IDEM 0x0E
#define IN_AVANCER_REVERSE 0x0F

#define IN_PID_CONST_MASQUE 0xF8
#define IN_PID_CONST_VIT_GAUCHE 0x20
#define IN_PID_CONST_VIT_DROITE 0x21
#define IN_PID_CONST_TRANSLATION 0x22
#define IN_PID_CONST_ROTATION 0x23
#define IN_PID_CONST_COURBURE 0x24
#define IN_PID_CONST_VIT_LINEAIRE 0x25
#define IN_CONST_SAMSON 0x26
// 0x27 est r�serv� pour un pid (cf masque)

#define IN_ASSER_OFF 0x28
#define IN_RESEND_PACKET 0xFF

#define IN_REMOVE_ALL_HOOKS 0x10
#define IN_REMOVE_SOME_HOOKS 0x11

#define IN_HOOK_DATE 0x44
#define IN_HOOK_DEMI_PLAN 0x45
#define IN_HOOK_POSITION 0x46
#define IN_HOOK_POSITION_UNIQUE 0x47
#define IN_HOOK_CONTACT 0x48
#define IN_HOOK_CONTACT_UNIQUE 0x49
// reste des 0x4X est r�serv� pour les hooks

#define IN_HOOK_MASK 0xF0
#define IN_HOOK_GROUP 0x40
#define IN_CALLBACK_ELT 0x00
#define IN_CALLBACK_SCRIPT 0x40
#define IN_CALLBACK_AX12 0x80
#define IN_CALLBACK_MASK 0xC0
#define IN_ARC 0x2E
#define IN_DEBUG_MODE 0x0F

#define OUT_PING 0x3F
#define OUT_PONG1 0x54
#define OUT_PONG2 0x33
#define OUT_ROBOT_ARRIVE 0x02
#define OUT_PROBLEME_MECA 0x03
#define OUT_DEBUT_MATCH 0x04
#define OUT_FIN_MATCH 0x05
#define OUT_COULEUR_ROBOT_SANS_SYMETRIE 0x06
#define OUT_COULEUR_ROBOT_AVEC_SYMETRIE 0x07
#define OUT_BALISE_PRESENTE 0x08
#define OUT_BALISE_NON_PRESENTE 0x09
#define OUT_CAPTEURS 0x0A
#define OUT_XYO 0x0E
#define OUT_CODE_COQUILLAGES 0x0C
#define OUT_DEBUG_ASSER 0x10
#define OUT_RESEND_PACKET 0xFF
#define OUT_ELEMENT_SHOOTE 0x0D

#define ID_FORT 0x00
#define ID_FAIBLE 0x01
#define COMMANDE 0x02
#define PARAM 0x03

#endif
