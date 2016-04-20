package enums;

/**
 * Protocole série entre la STM et la Java
 * @author pf
 *
 */

public enum SerialProtocol {
	/**
	 * Protocole Java vers STM
	 */
	OUT_TOURNER(0x04),
	OUT_VA_AU_POINT(0x05),
	OUT_ACTIONNEUR(0x06),
	OUT_STOP(0x07),
	OUT_INIT_ODO(0x08),

	OUT_DEBUG_MODE(0x0B),
	OUT_AVANCER(0x0C),
	OUT_AVANCER_NEG(0x0D),
	OUT_AVANCER_IDEM(0x0E),
	OUT_AVANCER_REVERSE(0x0F),
	OUT_REMOVE_ALL_HOOKS(0x10),
	OUT_REMOVE_SOME_HOOKS(0x11),

	OUT_PID_CONST_VIT_GAUCHE(0x20),
	OUT_PID_CONST_VIT_DROITE(0x21),
	OUT_PID_CONST_TRANSLATION(0x22),
	OUT_PID_CONST_ROTATION(0x23),
	OUT_PID_CONST_COURBURE(0x24),
	OUT_PID_CONST_VIT_LINEAIRE(0x25),
	OUT_CONST_SAMSON(0x26),

	OUT_ASSER_OFF(0x28),

	OUT_SEND_ARC(0x2E),
	OUT_SEND_ARC_ARRET(0x2F),

	OUT_SUSPEND_MOVE(0x30),
	OUT_RESUME_MOVE(0x31),
	OUT_ASSER_POS_ACTUELLE(0x32),

	OUT_PING(0x3F),
	OUT_PING_NEW_CONNECTION(0x40),

	OUT_PONG1(0x42),
		OUT_PONG2(0x57),
		
	OUT_HOOK_DATE(0x44),
	OUT_HOOK_DEMI_PLAN(0x45),
	OUT_HOOK_POSITION(0x46),
	OUT_HOOK_CONTACT(0x48),
	OUT_HOOK_CONTACT_UNIQUE(0x49),

	OUT_RESEND_PACKET(0xFF),
	
	CALLBACK_ELEMENT(0x00),
	CALLBACK_SCRIPT(0x40),
	CALLBACK_AX12(0x80),

	/**
	 * Protocole STM vers Java
	 */
	
	IN_ROBOT_ARRIVE(0x02),
	IN_PB_DEPLACEMENT(0x03),
	IN_DEBUT_MATCH(0x04),
	IN_MATCH_FINI(0x05),
	IN_COULEUR_ROBOT(0x06),
	IN_PRESENCE_BALISE(0x08),
	IN_INFO_CAPTEURS(0x0A),
	IN_CODE_COQUILLAGES(0x0C),
	IN_ELT_SHOOT(0x0D),
	IN_XYO(0x0E),
	IN_DEBUG_ASSER(0x10),
	IN_PING(0x3F),
	IN_PONG1(0x54),
		IN_PONG2(0x33),
	IN_RESEND_PACKET(0xFF),

	MASK_LAST_BIT(0xFE);

	public final byte code;
	public final int codeInt;
	private SerialProtocol(int code)
	{
		codeInt = code;
		this.code = (byte) code;
	}
	
}
