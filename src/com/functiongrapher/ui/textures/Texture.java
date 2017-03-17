package com.functiongrapher.ui.textures;

import org.lwjgl.opengl.GL11;

public class Texture {

	private final int width;
	private final int height;
	private final int textureID;

	public Texture(int width, int height, int textureID) {
		super();
		this.width = width;
		this.height = height;
		this.textureID = textureID;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getTextureID() {
		return textureID;
	}
	
	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	}

}
