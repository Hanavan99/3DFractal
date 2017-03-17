package com.functiongrapher.ui.textures;

import java.util.HashMap;

public class GlyphSet {

	private HashMap<Character, Glyph> glyphs = new HashMap<Character, Glyph>();
	
	public void add(char c, Glyph g) {
		glyphs.put(c, g);
	}
	
	public Glyph get(char c) {
		return glyphs.get(c);
	}
	
}
