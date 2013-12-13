package strategie;

import scripts.Script;

/**
 * Classe des couples (note, script) utilisée par Stratégie
 * @author pf
 *
 */

public class CoupleNoteScript {

	public float note;
	public Script script;

	public CoupleNoteScript(float note, Script script) {
		this.note = note;
		this.script = script;
	}

	public CoupleNoteScript() {
		note = 0;
		script = null;
	}
		
}
