//  -------------   Euler'sche Winkel -------------------
//                                      E.Gutknecht, Maerz 2018
// Tastenfunktionen:
//
// Pfeiltasten  : Azimut/Elevation fuer Kamera-System
// 'a', 'b', 'c': Erhoehung Euler-Winkel alpha, beta, gamma
// 'r'          : Ruecksetzung Euler-Winkel

import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.*;
import ch.fhnw.util.math.*;

public class EulerAngles
       implements WindowListener, GLEventListener, KeyListener
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
    MyGLBase1 mygl;
    int maxVerts = 2048;                                 // max. Anzahl Vertices im Vertex-Array

    float azimut=30, elevation=10;                       // Kamera-System
    float alpha=0, beta=0, gamma=0;                      // Euler-Winkel

    float xleft=-2, xright=2;                            // ViewingVolume
    float ybottom, ytop;
    float znear=-100, zfar=1000;


    //  ---------  Methoden  --------------------------------

    public EulerAngles()                                   // Konstruktor
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
       f.addKeyListener(this);
       canvas.addKeyListener(this);
   };


    public void zeichneAchsen(GL3 gl, float a, float b, float c)
    {  Vec3 O = new Vec3(0,0,0);
       Vec3 A = new Vec3(a,0,0);
       Vec3 B = new Vec3(0,b,0);
       Vec3 C = new Vec3(0,0,c);
       mygl.rewindBuffer(gl);
       mygl.putVertex(O.x,O.y,O.z);  // x-Achse
       mygl.putVertex(A.x,A.y,A.z);
       mygl.putVertex(O.x,O.y,O.z);  // y-Achse
       mygl.putVertex(B.x,B.y,B.z);
       mygl.putVertex(O.x,O.y,O.z);  // z=Achse
       mygl.putVertex(C.x,C.y,C.z);
       mygl.copyBuffer(gl);
       mygl.drawArrays(gl,GL3.GL_LINES);
    }


    //  ----------  OpenGL-Events   ---------------------------

    @Override
    public void init(GLAutoDrawable drawable)             //  Initialisierung
    {  GL3 gl = drawable.getGL().getGL3();
       System.out.println("OpenGl Version: " + gl.glGetString(gl.GL_VERSION));
       System.out.println("Shading Language: " + gl.glGetString(gl.GL_SHADING_LANGUAGE_VERSION));
       System.out.println();
       programId = MyShaders.initShaders(gl,vShader,fShader);
       mygl = new MyGLBase1(gl, programId, maxVerts);     // OpenGL Hilfsfunktiontn
       gl.glClearColor(0,0,1,1);                          // Hintergrundfarbe
    }


    @Override
    public void display(GLAutoDrawable drawable)
    { GL3 gl = drawable.getGL().getGL3();

      // -----  Sichtbarkeitstest
///////////////////      gl.glEnable(GL3.GL_DEPTH_TEST);
      gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

      // -----  Kamera-System
      Vec3 A = new Vec3(0,0,3);                                   // Kamera-Pos. (Auge)
      Vec3 B = new Vec3(0,0,0);                                   // Zielpunkt
      Vec3 up = new Vec3(0,1,0);                                  // up-Richtung
      Mat4 R1 = Mat4.rotate(-elevation,1,0,0);
      Mat4 R2 = Mat4.rotate(azimut,0,1,0);
      Mat4 R = R2.postMultiply(R1);   // R2*R1
      Mat4 V = Mat4.lookAt(R.transform(A),R.transform(B),
                           R.transform(up));
      mygl.setM(gl,V);

      // -----  absolute Koordinatenachsen
      mygl.setColor(1,0,0);
      zeichneAchsen(gl,3,3,3);

      // -----  Euler-Drehungen
      Mat4 R_alpha = Mat4.rotate(alpha,0,1,0);
      Mat4 R_beta = Mat4.rotate(beta,0,0,1);
      Mat4 R_gamma = Mat4.rotate(gamma,0,1,0);
      Mat4 U = R_alpha;                          // Objekt-Matrix
      U = U.preMultiply(R_beta);
      U = U.preMultiply(R_gamma);

      Mat4 M = U.preMultiply(V);                 // ModelView-Matrix  M = V*U
      mygl.setM(gl,M);

      // -----  gedrehte Koordinatenachsen
      mygl.setColor(1,1,1);
      zeichneAchsen(gl,1,1,1);
    }


    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y,
                        int width, int height)
    {  GL3 gl = drawable.getGL().getGL3();
       // Set the viewport to be the entire window
       gl.glViewport(0, 0, width, height);
       // -----  Projektionsmatrix
       float aspect = (float)height / width;
       ytop = aspect * xright;
       ybottom = aspect * xleft;
       Mat4 P = Mat4.ortho(xleft,xright,ybottom,ytop,znear,zfar);
       mygl.setP(gl,P);
    }


    @Override
    public void dispose(GLAutoDrawable drawable)  { }                  // not needed


    //  -----------  main-Methode  ---------------------------

    public static void main(String[] args)
    { new EulerAngles();
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


    // -----  Keyboard-Ereignisse  ------
    public void keyPressed(KeyEvent e)
    {  int code = e.getKeyCode();
       switch(code)
       { case KeyEvent.VK_UP:    elevation++;   // Kamera-System
                                 break;
         case KeyEvent.VK_DOWN:  elevation--;
                                 break;
         case KeyEvent.VK_RIGHT: azimut++;
                                 break;
         case KeyEvent.VK_LEFT:  azimut--;
                                 break;
       }
       canvas.repaint();
     }
     public void keyTyped(KeyEvent e)
     { char key = e.getKeyChar();
       switch (key)
       {  case 'a' :
          case 'A' : alpha++;
                     break;
          case 'b' :
          case 'B' : beta++;
                     break;
          case 'c' :
          case 'C' : gamma++;
                     break;
          case 'r' :
          case 'R' : alpha=0;
                     beta=0;
                     gamma=0;
                     break;
          case 'q':
              // Reset all 
              azimut=30;
              elevation=10;                       
              alpha=0; 
              beta=0;
              gamma=0; 
              break;
        }
        canvas.repaint();
     }

      public void keyReleased(KeyEvent e){}

}