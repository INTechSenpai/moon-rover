package strategie;

import java.util.ArrayList;

import scripts.Script;

public class CoupleNoteScripts {

	public float note;
	public ArrayList<Script> scripts;

	public CoupleNoteScripts(float note, ArrayList<Script> scripts) {
		this.note = note;
		this.scripts = scripts;
	}

	public CoupleNoteScripts() {
		note = 0;
		scripts = new ArrayList<Script>();
	}
		
}
