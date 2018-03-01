//  -------------   JOGL 2D-Programm  -------------------
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import ch.fhnw.util.math.Mat4;

public class OlympischeRinge implements WindowListener, GLEventListener {
    String windowTitle = "Olympische Ringe";
    int windowWidth  = 640;
    int windowHeight = 640;
    String vShader = MyShaders.vShader1;                 
    String fShader = MyShaders.fShader0;                 
    Frame frame;
    GLCanvas canvas;                                     
    int programId;                                       
    
    public static void main(String[] args) { 
    	new OlympischeRinge().createFrame();
    }
    
    public void zeichneKreisring(GL3 gl, double r1, double r2, double red, double green, double blue, int nPunkte) {
    	double alpha = (2*Math.PI)/(double)nPunkte;
    	nPunkte += 2; //man braucht 2 Vertices, die das letzte dreieck zeichnen
    	float[] vb = new float[nPunkte*12];
    	float x, y;
    	
    	for(int i = 0; i < nPunkte; i++) {
    		if(i%2 == 0) {
    			x = (float) (r1*Math.cos(alpha*i));
    			y = (float) (r1*Math.sin(alpha*i));
    		} else {
    			x = (float) ((r1+r2)*Math.cos(alpha*i));
    			y = (float) ((r1+r2)*Math.sin(alpha*i));
    		}
    		vb[i*12]    = x;
    		vb[i*12+ 1] = y;
    		vb[i*12+ 2] = 0; // z- Achse
    		vb[i*12+ 3] = 1;
    		vb[i*12+ 4] = (float) red;
    		vb[i*12+ 5] = (float) green;
    		vb[i*12+ 6] = (float) blue;
    		vb[i*12+ 7] = 1;  // Alpha
    		vb[i*12+ 8] = 0;  // Norm X
    		vb[i*12+ 9] = 0;  // Norm Y
    		vb[i*12+10] = 1;  // Norm Z
    		vb[i*12+11] = 0;
    	}
    	gl.glBufferData(GL3.GL_ARRAY_BUFFER, vb.length*Float.SIZE/8,Buffers.newDirectFloatBuffer(vb), GL3.GL_STATIC_DRAW);
        gl.glDrawArrays(GL3.GL_TRIANGLE_STRIP, 0, nPunkte);
    }

    void setupVertexBuffer(GL3 gl, int pgm) { 
      int[] vaoId = new int[1];
      gl.glGenVertexArrays(1, vaoId, 0);
      gl.glBindVertexArray(vaoId[0]);

      int[] bufId = new int[1];
      gl.glGenBuffers(1, bufId, 0);
      gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, bufId[0]);

      int vAttribSize = 4*Float.SIZE/8;
      int vertexSize = 3*vAttribSize;                   
      defineAttribute(gl, pgm, "vPosition", vertexSize, 0);
      defineAttribute(gl, pgm, "vColor", vertexSize,  vAttribSize);
      defineAttribute(gl, pgm, "vNormal", vertexSize, 2*vAttribSize);
    }

    void defineAttribute(GL3 gl, int pgm, String attribName, int vertexSize, int offset) {  
       int attribId = gl.glGetAttribLocation(pgm, attribName);
       if(attribId >= 0) { 
    	   gl.glEnableVertexAttribArray(attribId);
           gl.glVertexAttribPointer(attribId, 4, GL3.GL_FLOAT, false, vertexSize, offset);
       }
       else System.out. println("Attribute " + attribName + " not enabled");
    }
    
    public void setUniforms(GL3 gl, int pgm, Mat4 M, Mat4 P) {
    	int MId = gl.glGetUniformLocation(pgm, "M");
    	int PId = gl.glGetUniformLocation(pgm, "P");
    	gl.glUniformMatrix4fv(MId, 1, false, M.toArray(), 0);
    	gl.glUniformMatrix4fv(PId, 1, false, P.toArray(), 0);
    }

    public void createFrame() {  
    	Frame f = new Frame(windowTitle);
        f.setSize(windowWidth, windowHeight);
        //f.setResizable(false);
        f.addWindowListener(this);
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities glCaps = new GLCapabilities(glp);
        canvas = new GLCanvas(glCaps);
        canvas.addGLEventListener(this);
        f.add(canvas);
        f.setVisible(true);
    }

	@Override
    public void init(GLAutoDrawable drawable) {
	    GL3 gl = drawable.getGL().getGL3();
        System.out.println("OpenGl Version: " + gl.glGetString(GL3.GL_VERSION));
        System.out.println("Shading Language: " + gl.glGetString(GL3.GL_SHADING_LANGUAGE_VERSION));
        System.out.println();
        programId = MyShaders.initShaders(gl,vShader,fShader);
        setupVertexBuffer(gl, programId); 
        gl.glClearColor(1,1,1,1);
    }

    @Override
    public void display(GLAutoDrawable drawable) { 
       GL3 gl = drawable.getGL().getGL3();
       gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
       
       Mat4 P = Mat4.ortho(-2,4,-3,3,-10,360);
       Mat4 M;
       
       // Blue Ring
       M = Mat4.translate(-1f + 1,0,0);
       setUniforms(gl, programId, M, P);
       zeichneKreisring(gl, 0.4, 0.075, 0, 0, 1, 360);
       
       // Black Ring
       M = Mat4.translate(-1f + 2,0,0);
       setUniforms(gl, programId, M, P);
       zeichneKreisring(gl, 0.4, 0.075, 0, 0, 0, 360);
       
       // Red Ring
       M = Mat4.translate(-1f + 3,0,0);
       setUniforms(gl, programId, M, P);
       zeichneKreisring(gl, 0.4, 0.075, 1, 0, 0, 360);
       
       // Yellow Ring
       M = Mat4.translate(0.5f,-0.5f,0);
       setUniforms(gl, programId, M, P);
       zeichneKreisring(gl, 0.4, 0.075, 1, 1, 0, 360);
       
       // Green Ring
       M = Mat4.translate(1.5f,-0.5f,0);
       setUniforms(gl, programId, M, P);
       zeichneKreisring(gl, 0.4, 0.075, 0, 1, 0, 360);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {  
    	GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(0, 0, width, height);
    }

    public void windowClosing(WindowEvent e) {   
    	System.out.println("closing window");
        System.exit(0);
    }
    
    @Override public void dispose(GLAutoDrawable drawable) {}
    @Override public void windowActivated(WindowEvent e)   {}
    @Override public void windowClosed(WindowEvent e)      {} 
    @Override public void windowDeactivated(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e)   {}
    @Override public void windowOpened(WindowEvent e)      {}
}