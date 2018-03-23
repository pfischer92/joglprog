//  -------------   JOGL 3D Beispiel-Programm  -------------------
import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.*;
import ch.fhnw.util.math.*;

public class MyFirst3DInteractiveCameraControl
       implements WindowListener, GLEventListener, KeyListener
{

    //  ---------  globale Daten  ---------------------------

    String windowTitle = "JOGL-Application";
    int windowWidth = 800;
    int windowHeight = 600;
    String vShader = MyShaders.vShader2;                 // Vertex-Shader
    String fShader = MyShaders.fShader0;                 // Fragment-Shader
    Frame frame;
    GLCanvas canvas;                                     // OpenGL Window
    int programId;                                       // OpenGL-Id
    MyGLBase1 mygl;
    int maxVerts = 2048;                                 // max. Anzahl Vertices im Vertex-Array

    Mat4 M = Mat4.ID;                                    // ModelView-Matrix
    Mat4 P = Mat4.ID;                                    // Projektionsmatrix

    float xleft=-4, xright=4;                            // ViewingVolume
    float ybottom, ytop;
    float znear=-100, zfar=1000;
    
    float azimut = 30, elevation = 10;


    //  ---------  Methoden  --------------------------------

    public MyFirst3DInteractiveCameraControl()                                   // Konstruktor
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


    public void zeichneStrecke(GL3 gl, float x1, float y1, float z1,
                                float x2, float y2, float z2)
    {
       mygl.rewindBuffer(gl);
       mygl.putVertex(x1,y1,z1);
       mygl.putVertex(x2,y2,z2);
       mygl.copyBuffer(gl);
       mygl.drawArrays(gl,GL3.GL_LINES);
    }


    public void zeichneDreieck(GL3 gl, Vec3 A, Vec3 B, Vec3 C)
    {  mygl.rewindBuffer(gl);
       Vec3 AB = B.subtract(A);
       Vec3 AC = C.subtract(A);
       Vec3 n = AB.cross(AC).normalize();
       mygl.setNormal(n.x, n.y, n.z);
       mygl.putVertex(A.x,A.y,A.z);
       mygl.putVertex(B.x,B.y,B.z);
       mygl.putVertex(C.x,C.y,C.z);
       mygl.copyBuffer(gl);
       mygl.drawArrays(gl,GL3.GL_TRIANGLES);
    }


    public void zeichnePyramide(GL3 gl, float a, float h)
    { a *= 0.5f;
      Vec3 A = new Vec3(a,0,a);
      Vec3 B = new Vec3(a,0,-a);
      Vec3 C = new Vec3(-a,0,-a);
      Vec3 D = new Vec3(-a,0,a);
      Vec3 S = new Vec3(0,h,0);
      //mygl.setColor(1,0,0);       // Statische Farbe setzen
      zeichneDreieck(gl,S,A,B);
      //mygl.setColor(0.8f,0,0);    // Statische Farbe setzen
      zeichneDreieck(gl,S,B,C);
      //mygl.setColor(0.6f,0,0);    // Statische Farbe setzen
      zeichneDreieck(gl,S,C,D);
      //mygl.setColor(0.5f,0,0);    // Statische Farbe setzen
      zeichneDreieck(gl,S,D,A);
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
      gl.glEnable(GL3.GL_DEPTH_TEST);
      gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

      // -----  Kamera-System
      Vec3 A = new Vec3(0,0,3);                                   // Kamera-Pos. (Auge)
      Vec3 B = new Vec3(0,0,0);                                   // Zielpunkt
      Vec3 up = new Vec3(0,1,0);                                  // up-Richtung
      
      Mat4 R1 = Mat4.rotate(-elevation, 1,0,0);
      Mat4 R2 = Mat4.rotate(azimut, 0,1,0);
      Mat4 R = R2.postMultiply(R1);                               // R2*R1
      
          
     
      Mat4 M = Mat4.lookAt(R.transform(A), R.transform(B), R.transform(up));
      mygl.setM(gl,M);
      
      // Irgendwas für die Beleuchtung....
      mygl.setLightPosition(gl, -2,  4,  2);
      mygl.setShadingParam(gl,  0.1f, 0.8f);

      // -----  Koordinatenachsen
      mygl.setShadingLevel(gl, 0);
      mygl.setColor(0.7f,0.7f,0.7f);
      zeichneStrecke(gl,0,0,0, 6,0,0);            // x-Achse
      zeichneStrecke(gl,0,0,0, 0,6,0);            // y-Achse
      zeichneStrecke(gl,0,0,0, 0,0,6);            // z-Achse

      // -----  Figur zeichnen
      float a = 3;
      float h = 0.63f * a;                      // Cheops-Pyramide
      mygl.setShadingLevel(gl, 1);
      mygl.setColor(1.0f, 0f, 0f);
      zeichnePyramide(gl,a,h);
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
    { new MyFirst3DInteractiveCameraControl();
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


    //  ---------  Keyboard-Events  --------------------
    
    @Override
    public void keyPressed(KeyEvent arg0) {
        int code = arg0.getKeyCode();
        switch(code) {
            case KeyEvent.VK_UP:
                elevation++;
                break;
            case KeyEvent.VK_RIGHT:
                azimut++;
                break;
            case KeyEvent.VK_DOWN:
                elevation--;
                break;
            case KeyEvent.VK_LEFT:
                azimut--;
                break;
        }
        canvas.repaint();
        
    }


    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

}