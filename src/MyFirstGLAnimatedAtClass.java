//  -------------   JOGL 2D-Programm  -------------------
import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.*;
import java.nio.*;
import ch.fhnw.util.math.*;

public class MyFirstGLAnimatedAtClass
       implements WindowListener, GLEventListener
{

    //  ---------  globale Daten  ---------------------------

    String windowTitle = "JOGL-Application";
    int windowWidth = 800;
    int windowHeight = 600;
    String vShader = MyShaders.vShader1;                 // Vertex-Shader
    String fShader = MyShaders.fShader0;                 // Fragment-Shader
    Frame frame;
    GLCanvas canvas;                                     // OpenGL Window
    int programId;                                       // OpenGL-Id
    float angle;


    //  ---------  Methoden  --------------------------------

    void setupVertexBuffer(GL3 gl, int pgm)             // OpenGL VertexBuffer
    {
      // ----- generate VertexArrayObject  -------------
      int[] vaoId = new int[1];
      gl.glGenVertexArrays(1, vaoId, 0);
      gl.glBindVertexArray(vaoId[0]);

      // ----- generate BufferObject  -------------
      int[] bufId = new int[1];
      gl.glGenBuffers(1, bufId, 0);
      gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, bufId[0]);

      int vAttribSize = 4*Float.SIZE/8;                 // Anz. Bytes eines Vertex-Attributes
      int vertexSize = 3*vAttribSize;                   // Anz. Bytes eines Vertex
      defineAttribute(gl, pgm, "vPosition", vertexSize, 0);
      defineAttribute(gl, pgm, "vColor", vertexSize,  vAttribSize);
      defineAttribute(gl, pgm, "vNormal", vertexSize, 2*vAttribSize);
    }


    void defineAttribute(GL3 gl, int pgm, String attribName, int vertexSize, int offset)
    {  int attribId = gl.glGetAttribLocation(pgm, attribName);
       if ( attribId >= 0 )
       {  gl.glEnableVertexAttribArray(attribId);
          gl.glVertexAttribPointer(attribId, 4, GL3.GL_FLOAT, false, vertexSize, offset);
       }
       else
          System.out. println("Attribute " + attribName + " not enabled");
    }

    public void setUniforms(GL3 gl, int pgm,
                 Mat4 M, Mat4 P)
    {  int MId = gl.glGetUniformLocation(pgm, "M");
       int PId = gl.glGetUniformLocation(pgm, "P"); 
       gl.glUniformMatrix4fv(MId,1,false,M.toArray(),0);
       gl.glUniformMatrix4fv(PId,1,false,P.toArray(),0);
    }
    
    
    public MyFirstGLAnimatedAtClass()                                   // Konstruktor
    { createFrame();
    }


    void createFrame()                                    // Fenster erzeugen
    {  Frame f = new Frame(windowTitle);
       f.setSize(windowWidth, windowHeight);
       f.addWindowListener(this);
       GLProfile glp = GLProfile.get(GLProfile.GL3);
       GLCapabilities glCaps = new GLCapabilities(glp);
       canvas = new GLCanvas(glCaps);
       canvas.addGLEventListener(this);
       f.add(canvas);
       f.setVisible(true);
    };


    public void zeichneStrecke(GL3 gl, float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float r, float g, float b)
    {
       float[] vb = { x1,y1,z1,1, r,g,b,1, 0,0,1,0,
                      x2,y2,z2,1, r,g,b,1, 0,0,1,0};
       gl.glBufferData(GL3.GL_ARRAY_BUFFER, vb.length*Float.SIZE/8,           // Speicher allozieren
                            Buffers.newDirectFloatBuffer(vb), GL3.GL_STATIC_DRAW);
       gl.glDrawArrays(GL3.GL_LINES,0,2);
    }


    public void zeichneDreieck(GL3 gl, float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float r, float g, float b)
    {
       float[] vb = { x1,y1,z1,1, r,g,b,1, 0,0,1,0,
                      x2,y2,z2,1, r,g,b,1, 0,0,1,0,
                      x3,y3,z3,1, r,g,b,1, 0,0,1,0};
       gl.glBufferData(GL3.GL_ARRAY_BUFFER, vb.length*Float.SIZE/8,           // Speicher allozieren
                            Buffers.newDirectFloatBuffer(vb), GL3.GL_STATIC_DRAW);
       gl.glDrawArrays(GL3.GL_TRIANGLES,0,3);
    }


    //  ----------  OpenGL-Events   ---------------------------

    @Override
    public void init(GLAutoDrawable drawable)             //  Initialisierung
    {  GL3 gl = drawable.getGL().getGL3();
       System.out.println("OpenGl Version: " + gl.glGetString(gl.GL_VERSION));
       System.out.println("Shading Language: " + gl.glGetString(gl.GL_SHADING_LANGUAGE_VERSION));
       System.out.println();
       programId = MyShaders.initShaders(gl,vShader,fShader);
       setupVertexBuffer(gl, programId);                 // Vertex-Buffer
       gl.glClearColor(0,0,1,1);                         // Hintergrund-Farbe
       
       FPSAnimator anim;
       anim = new FPSAnimator(canvas, 300, true);
       anim.start();       
    }
    
    float phi = 0;

    @Override
    public void display(GLAutoDrawable drawable)
    { 
      GL3 gl = drawable.getGL().getGL3();
      gl.glClear(GL3.GL_COLOR_BUFFER_BIT);      // Bildschirm loeschen
      Mat4 M = Mat4.ID;
      Mat4 P = Mat4.ortho(-4,4,-3,3,-10,1000);
      setUniforms(gl,programId,M,P);
      
      zeichneStrecke(gl,-1,0,0, 1,0,0, 0.7f,0.7f,0.7f);               // x-Achse
      zeichneStrecke(gl,0,-1,0, 0,1,0, 0.7f,0.7f,0.7f);               // y-Achse
      M = Mat4.rotate(-phi, 0, 0, 1);
      M = M.preMultiply(Mat4.translate(2f, 0,0));                   // Bewege auf x- Achse um 2 nach rechts
      M = M.preMultiply(Mat4.rotate(phi,0,0,1));
      
      phi++;
      
      setUniforms(gl,programId,M,P);
      zeichneDreieck(gl,-0.5f,0,0, 0.5f,0,0, 0,0.8f,0, 1,0,0);
    }


    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y,
                        int width, int height)
    {  GL3 gl = drawable.getGL().getGL3();
       // Set the viewport to be the entire window
       gl.glViewport(0, 0, width, height);
    }


    @Override
    public void dispose(GLAutoDrawable drawable)  { }                  // not needed


    //  -----------  main-Methode  ---------------------------

    public static void main(String[] args)
    { new MyFirstGLAnimatedAtClass();
    }

    //  ---------  Window-Events  --------------------

    public void windowClosing(WindowEvent e)
    {   System.out.println("closing window");
        System.exit(0);
    }
    public void windowActivated(WindowEvent e) {  }
    public void windowClosed(WindowEvent e) {  }
    public void windowDeactivated(WindowEvent e) {  }
    public void windowDeiconified(WindowEvent e) {  }
    public void windowIconified(WindowEvent e) {  }
    public void windowOpened(WindowEvent e) {  }

}