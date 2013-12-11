package strategie;

import scripts.Script;

public class CoupleNoteScripts {

	public float note;
	public Script script;

	public CoupleNoteScripts(float note, Script script) {
		this.note = note;
		this.script = script;
	}

	public CoupleNoteScripts() {
		note = 0;
		script = null;
	}
		
}
