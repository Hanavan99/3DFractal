package com.functiongrapher.ui.textures;

public class Glyph extends Texture {

	private char glyph;
	private int internalwidth;
	
	public Glyph(int width, int height, int textureID) {
		super(width, height, textureID);
	}
	
	public Glyph(char glyph, int internalwidth, int width, int height, int textureID) {
		super(width, height, textureID);
		this.glyph = glyph;
		this.internalwidth = internalwidth;
	}

	public int getGlyphWidth() {
		return internalwidth;
	}
	
	public char getGlyph() {
		return glyph;
	}
	
}
