package com.fractal.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.fractal.model.CVertex3;
import com.fractal.model.Complex;
import com.fractal.model.Debouncer;
import com.fractal.model.EvalResult;
import com.fractal.model.FractalMode;
import com.fractal.model.fractal.AbstractFractal;
import com.fractal.model.fractal.Mandelbrot;
import com.functiongrapher.ui.textures.GlyphManager;
import com.functiongrapher.ui.textures.TextureManager;

public class Main {

	private static long window;
	private static boolean running = true;
	private static int iterations = 100;
	private static double delta = 1 / 64d;
	private static double yaw = 0;
	private static double pitch = 0;
	private static double fscale = 1;
	private static double xpos = 0;
	private static double ypos = 0;
	private static double juliax = 0;
	private static double juliay = 0;
	private static double zoom = 60;
	private static int viewmode = 0;
	private static int offset = 0;
	private static int mode = 0;
	private static int smode = 0;
	private static String[] smodes = { "Move Camera", "Move Julia", "Zoom" };
	private static String[] viewmodes = { "Mandelbrot", "Julia", "Static" };
	private static int fps = 0;
	private static int frames = 0;
	private static int vvboptr;
	private static int cvboptr;
	private static ArrayList<CVertex3> staticvs = new ArrayList<CVertex3>();
	private static Debouncer modeswitch = new Debouncer();
	private static Debouncer stickmode = new Debouncer();
	private static Debouncer viewermode = new Debouncer();
	private static AbstractFractal f = new Mandelbrot();
	private static String font = "default";
	private static int vshader;
	private static int fshader;
	private static int tshader;
	private static int teshader;

	public static void main(String[] args) {

		Thread fpsthread = new Thread(() -> {
			while (running) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fps = frames;
				frames = 0;
			}
		});
		fpsthread.start();

		Thread inputthread = new Thread(() -> {
			while (running) {
				// FloatBuffer axes = GLFW.glfwGetJoystickAxes(0);
				ByteBuffer buttons = GLFW.glfwGetJoystickButtons(0);
				if (iterations > 5) {
					iterations -= buttons.get(12) * 5;
				}
				iterations += buttons.get(10) * 5;
				if (delta < 1) {
					delta *= buttons.get(11) + 1;
				}
				if (delta > 1 / 1024d) {
					delta /= buttons.get(13) + 1;
				}
				mode += modeswitch.debounce(buttons.get(1));
				mode %= 2;
				smode += stickmode.debounce(buttons.get(0));
				smode %= 3;
				viewmode += viewermode.debounce(buttons.get(6));
				viewmode %= 3;
				if (buttons.get(7) == 1) {

				}
				if (offset > 0) {
					offset -= buttons.get(2);
				}
				offset += buttons.get(3);
				try {
					Thread.sleep(100);
				} catch (Exception e) {

				}
			}
		});

		GLFW.glfwInit();

		inputthread.start();

		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 8);
		window = GLFW.glfwCreateWindow(1280, 1024, "Test", 0, 0);

		GLFW.glfwSetKeyCallback(window, GLFWKeyCallback.create((long window, int key, int scancode, int action, int mods) -> {
			if (action == GLFW.GLFW_PRESS) {
				switch (key) {
				case GLFW.GLFW_KEY_F11:
					long mon = GLFW.glfwGetWindowMonitor(window);
					GLFW.glfwSetWindowMonitor(window, mon != 0 ? 0 : GLFW.glfwGetPrimaryMonitor(), 50, 50, 1280, 1024, 1);
					break;
				case GLFW.GLFW_KEY_SPACE:
					staticvs.clear();
					for (double x = -1; x <= 1; x += delta) {
						for (double y = -1; y <= 1; y += delta) {
							Complex z = mode == 0 ? new Complex(0, 0) : new Complex((x * fscale) + xpos, (y * fscale) + ypos);
							Complex c = mode == 0 ? new Complex((x * fscale) + xpos, (y * fscale) + ypos) : new Complex(juliax, juliay);
							int i;
							boolean inset = true;
							for (i = 0; i < iterations; i++) {
								z.multiply(z);
								z.add(c);
								if (z.abs() > 2) {
									inset = false;
									break;
								}
							}
							if (!inset) {
								Color color = new Color(Color.HSBtoRGB(i / (float) iterations, 1f, 1f));
								double zpos = ((0.25 * offset) / (float) i);
								CVertex3 v = new CVertex3(x, y, zpos, color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d);
								staticvs.add(v);
							}
						}
					}
					break;
				}
			}
		}));

		GLFW.glfwShowWindow(window);
		GLFW.glfwMakeContextCurrent(window);

		GL.createCapabilities();

		try {
			BufferedImage img = ImageIO.read(Main.class.getResourceAsStream("/com/fractal/texture/colors.png"));
			TextureManager.createTexture("test", img);
		} catch (Exception e) {

		}

		int textsize = 64;
		GlyphManager.generateGlyphSet(font, new Font("Times New Roman", Font.PLAIN, textsize), Color.BLACK, Color.BLUE, textsize, textsize);

		vvboptr = GL15.glGenBuffers();
		cvboptr = GL15.glGenBuffers();

		int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vvboptr);
		GL30.glVertexAttribIPointer(0, 3, GL11.GL_FLOAT, GL11.GL_FALSE, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cvboptr);
		GL30.glVertexAttribIPointer(1, 3, GL11.GL_FLOAT, GL11.GL_FALSE, 0);

		int program = ARBShaderObjects.glCreateProgramObjectARB();

		// vshader =
		// ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
		// ARBShaderObjects.glShaderSourceARB(vshader,
		// getResourceAsString("/com/fractal/shader/vertex_shader.txt"));
		// ARBShaderObjects.glCompileShaderARB(vshader);
		// ARBShaderObjects.glAttachObjectARB(program, vshader);

		fshader = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		ARBShaderObjects.glShaderSourceARB(fshader, getResourceAsString("/com/fractal/shader/fragment_shader.txt"));
		ARBShaderObjects.glCompileShaderARB(fshader);
		ARBShaderObjects.glAttachObjectARB(program, fshader);

		// tshader =
		// ARBShaderObjects.glCreateShaderObjectARB(ARBTessellationShader.GL_TESS_EVALUATION_SHADER);
		// ARBShaderObjects.glShaderSourceARB(tshader,
		// getResourceAsString("/com/fractal/shader/tessellation_shader.txt"));
		// ARBShaderObjects.glCompileShaderARB(tshader);
		// ARBShaderObjects.glAttachObjectARB(program, tshader);
		ARBShaderObjects.glLinkProgramARB(program);

		System.out.println(ARBShaderObjects.glGetInfoLogARB(program));

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_FRONT);
		GL11.glBlendFunc(GL11.GL_SMOOTH, GL11.GL_NICEST);
		while (!GLFW.glfwWindowShouldClose(window)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GL20.glUseProgram(0);

			int[] width = new int[1];
			int[] height = new int[1];
			GLFW.glfwGetWindowSize(window, width, height);
			GL11.glViewport(0, 0, width[0], height[0]);
			double aspect = width[0] / (double) height[0];
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, 1, 0, 1, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glPushMatrix();
			GL11.glColor3d(1, 1, 1);
			GlyphManager.drawString(font, "Focus point: " + new Complex(xpos, ypos).toString(), 0, 0.97, 0, aspect, 1 / 48d);
			GlyphManager.drawString(font, "Zoom: " + (1 / fscale) + "x", 0, 0.94, 0, aspect, 1 / 48d);
			GlyphManager.drawString(font, "Fractal mode: " + viewmodes[mode], 0, 0.91, 0, aspect, 1 / 48d);
			GlyphManager.drawString(font, "RSTICK mode: " + smodes[smode], 0, 0.88, 0, aspect, 1 / 48d);
			GlyphManager.drawString(font, "Iterations: " + iterations, 0, 0.85, 0, aspect, 1 / 48d);
			GlyphManager.drawString(font, "FPS: " + fps, 0, 0.82, 0, aspect, 1 / 48d);
			GlyphManager.drawString(font, "Resolution: " + delta, 0, 0.79, 0, aspect, 1 / 48d);
			GlyphManager.drawString(font, "Render mode: " + (viewmode == 0 ? "Quads" : "Fragment Shader"), 0, 0.76, 0, aspect, 1 / 48d);
			// GL11.glEnable(GL11.GL_TEXTURE_2D);
			// GL11.glBegin(GL11.GL_QUADS);
			// GL11.glVertex3d(0, 0, 0);
			// GL11.glVertex3d(0, 1, 0);
			// GL11.glVertex3d(1, 1, 0);
			// GL11.glVertex3d(1, 0, 0);
			// GL11.glEnd();
			// GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			// GL11.glOrtho(-2.5, 1.5, -1.5, 1.5, 0, 10);
			// GL11.glFrustum(-2.5, 1.5, -1.5, 1.5, 1, 10);
			createPerspective(zoom, width[0] / (double) height[0], 1, 10);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glPushMatrix();

			GL11.glTranslated(0, 0, -3);

			FloatBuffer axes = GLFW.glfwGetJoystickAxes(0);
			ByteBuffer buttons = GLFW.glfwGetJoystickButtons(0);
			xpos += (axes.get(0) / 10d) * fscale;
			ypos += (axes.get(1) / 10d) * fscale;
			// System.out.println(axes.get(4));
			fscale *= ((axes.get(4) * 0.02) + 1.02);
			fscale /= ((axes.get(5) * 0.02) + 1.02);
			// fscale += 0.000000001;
			// System.out.println(fscale);

			if (smode == 0) {
				yaw += axes.get(2);
				pitch += axes.get(3);
			} else if (smode == 1) {
				juliax += axes.get(2) / 100d;
				juliay += axes.get(3) / 100d;
			} else if (smode == 2) {
				zoom += axes.get(3);
			}

			GL11.glRotated(pitch, 1, 0, 0);
			GL11.glRotated(yaw, 0, 0, 1);

			double scale = buttons.get(4) * 4 + 1;
			// ByteBuffer vvbo = ByteBuffer.allocate(1000000);
			// ByteBuffer cvbo = ByteBuffer.allocate(1000000);
			if (viewmode == 0) {
				for (double x = -1 * scale; x <= 1 * scale; x += delta) {
					for (double y = -1 * scale; y <= 1 * scale; y += delta) {
						EvalResult r = f.evaluate(FractalMode.DOUBLE, iterations, new Object[] { x, y });
						if (r.isValid()) {
							GL11.glColor3bv(ByteBuffer.wrap(r.getColor()));
							double zpos = ((0.25 * offset) / (float) r.getIterations());
							GL11.glBegin(GL11.GL_QUADS);
							GL11.glVertex3d(x, y, zpos);
							GL11.glTexCoord2d(0, 0);
							GL11.glVertex3d(x, y + delta, zpos);
							GL11.glVertex3d(x + delta, y + delta, zpos);
							GL11.glVertex3d(x + delta, y, zpos);
							GL11.glEnd();
						}
					}
				}
			} else if (viewmode == 1) {
				ARBShaderObjects.glUseProgramObjectARB(program);
				int shader_iterptr = ARBShaderObjects.glGetUniformLocationARB(program, "iter");
				ARBShaderObjects.glUniform1iARB(shader_iterptr, iterations);
				TextureManager.getTexture("test").bind();
				double xmin = -2 * fscale + xpos;
				double xmax = 1 * fscale + xpos;
				double ymin = -1 * fscale + ypos;
				double ymax = 1 * fscale + ypos;
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2d(xmin, ymin);
				GL11.glVertex3d(-1, -1, 0);

				GL11.glTexCoord2d(xmin, ymax);
				GL11.glVertex3d(-1, 1, 0);

				GL11.glTexCoord2d(xmax, ymax);
				GL11.glVertex3d(1, 1, 0);

				GL11.glTexCoord2d(xmax, ymin);
				GL11.glVertex3d(1, -1, 0);

				GL11.glEnd();
				ARBShaderObjects.glUseProgramObjectARB(0);
			} else if (viewmode == 2) {
				GL11.glBegin(GL11.GL_QUADS);
				for (CVertex3 v : staticvs) {
					GL11.glColor3d(v.getR(), v.getG(), v.getB());
					GL11.glVertex3d(v.getX(), v.getY(), v.getZ());
					GL11.glVertex3d(v.getX(), v.getY() + delta, v.getZ());
					GL11.glVertex3d(v.getX() + delta, v.getY() + delta, v.getZ());
					GL11.glVertex3d(v.getX() + delta, v.getY(), v.getZ());
				}
				GL11.glEnd();
			}
			// vvbo.flip();

			// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vvboptr);
			// GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 12 * Float.BYTES,
			// GL15.GL_STATIC_DRAW);

			// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cvboptr);
			// GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 12 * Float.BYTES,
			// GL15.GL_STATIC_DRAW);

			// GL45.glEnableVertexArrayAttrib(vao, 0);
			// GL45.glEnableVertexArrayAttrib(vao, 1);

			GL11.glPopMatrix();
			GLFW.glfwSwapInterval(1);
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
			frames++;
		}
		running = false;
		GL.destroy();
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();

	}

	private static void createPerspective(double fovy, double aspect, double znear, double zfar) {
		double fh = Math.tan(fovy / 360 * Math.PI) * znear;
		double fw = fh * aspect;
		GL11.glFrustum(-fw, fw, -fh, fh, znear, zfar);
	}

	private static String getResourceAsString(String path) {
		try {
			String data = "";
			InputStream stream = Main.class.getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null) {
				data += line + "\r\n";
			}
			reader.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
