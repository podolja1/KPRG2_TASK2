package pipeline.renderer;

import pipeline.utils.GlCamera;
import pipeline.utils.GlSound;
import utils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import static pipeline.utils.GluUtils.gluLookAt;
import static pipeline.utils.GluUtils.gluPerspective;
import static pipeline.utils.GlutUtils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glRotatef;

import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import transforms.Vec3D;

import java.io.IOException;
import java.nio.DoubleBuffer;

public class Renderer extends AbstractRenderer {

    private OGLTexture2D sun, mercury, venus, earth, moon, mars, jupiter,
            saturn, saturnRingA, saturnRingB, saturnRingC, saturnRingD, uranus, neptune, stars;

    private boolean move = true;
    private float angle = 1;
    private float speed = 1;
    private float zoom = 1;

    private float dx, dy, ox, oy;

    private boolean projection = true;
    private char perspective = 'r';

    private boolean mouseLeftButton = false;
    private boolean mouseRightButton = false;
    private boolean light = false;
    private boolean freeze = true;
    private boolean info = true;

    private GlCamera camera;
    private float zenit, azimut;
    private float[] modelMatrix = new float[16];

    private float trans;
    private float deltaTrans = 0;

    public Renderer() {
        super();

        // keyboard commands
        glfwKeyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                // close program
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
                // = zero, when you release button
                if (action == GLFW_RELEASE) {
                    trans = 0;
                    deltaTrans = 0;
                }
                // one press
                if (action == GLFW_PRESS) {
                    switch (key) {
                        // projection
                        case GLFW_KEY_P:
                            projection = !projection;
                            break;
                        // movement
                        case GLFW_KEY_M:
                            move = !move;
                            break;
                        // light
                        case GLFW_KEY_L:
                            light = !light;
                            break;
                        // freeze of Universe (effect of flying in Universe, skybox)
                        case GLFW_KEY_F:
                            freeze = !freeze;
                            break;
                        // information
                        case GLFW_KEY_I:
                            info = !info;
                            break;
                        // general perspective
                        case GLFW_KEY_R:
                            perspective = 'r';
                            break;
                        case GLFW_KEY_Q:
                            perspective = 'q';
                            break;
                        case GLFW_KEY_X:
                            perspective = 'x';
                            break;
                        case GLFW_KEY_Y:
                            perspective = 'y';
                            break;
                        case GLFW_KEY_Z:
                            perspective = 'z';
                            break;
                        // views, from Sun to Neptune
                        case GLFW_KEY_0:
                            perspective = '0';
                            GlSound.play("res/sounds/sun.wav");
                            break;
                        case GLFW_KEY_1:
                            perspective = '1';
                            GlSound.play("res/sounds/mercury.wav");
                            break;
                        case GLFW_KEY_2:
                            perspective = '2';
                            GlSound.play("res/sounds/venus.wav");
                            break;
                        case GLFW_KEY_3:
                            perspective = '3';
                            GlSound.play("res/sounds/earth.wav");
                            break;
                        case GLFW_KEY_4:
                            perspective = '4';
                            GlSound.play("res/sounds/mars.wav");
                            break;
                        case GLFW_KEY_5:
                            perspective = '5';
                            GlSound.play("res/sounds/jupiter.wav");
                            break;
                        case GLFW_KEY_6:
                            perspective = '6';
                            GlSound.play("res/sounds/saturn.wav");
                            break;
                        case GLFW_KEY_7:
                            perspective = '7';
                            GlSound.play("res/sounds/uranus.wav");
                            break;
                        case GLFW_KEY_8:
                            perspective = '8';
                            GlSound.play("res/sounds/neptune.wav");
                            break;
                        case GLFW_KEY_BACKSPACE:
                            perspective = 'r';
                            projection = true;
                            zoom = 1;
                            init();
                            break;
                    }
                }
                // press and hold
                switch (key) {
                    // speed of movement
                    case GLFW_KEY_KP_SUBTRACT:
                        if (speed > 0.02 && speed <= 1) {
                            speed = speed - 0.02f;
                        } else if (speed > 1 && speed <= 10) {
                            speed = speed - 0.5f;
                        }
                        break;
                    case GLFW_KEY_KP_ADD:
                        if (speed > 0 && speed < 1) {
                            speed = speed + 0.02f;
                        } else if (speed >= 1 && speed < 10) {
                            speed = speed + 0.5f;
                        }
                        break;
                    // move
                    case GLFW_KEY_W:
                            camera.forward(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.02;
                            break;
                    case GLFW_KEY_S:
                            camera.backward(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.02;
                            break;
                    case GLFW_KEY_A:
                            camera.left(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.02;
                            break;
                    case GLFW_KEY_D:
                            camera.right(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.02;
                            break;
                    // zoom
                    case GLFW_KEY_UP:
                        if (zoom >= 0 && zoom <= 4.2)
                        zoom += 0.1;
                        break;
                    case GLFW_KEY_DOWN:
                        if (zoom >= 0.1 && zoom <= 4.3)
                        zoom -= 0.1;
                        break;
                }
            }
        };

        // mouse commands
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);

                mouseLeftButton = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
                mouseRightButton = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS;

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    ox = (float) x;
                    oy = (float) y;
                }
                if (button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS) {
                    ox = (float) x;
                    oy = (float) y;
                }
            }
        };

        // turn in space
        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseLeftButton) {
                    dx = (float) x - ox;
                    dy = (float) y - oy;
                    ox = (float) x;
                    oy = (float) y;
                    zenit -= dy / width * 180;
                    if (zenit > 90)
                        zenit = 90;
                    if (zenit <= -90)
                        zenit = -90;
                    azimut += dx / height * 180;
                    azimut = azimut % 360;
                    dx = 0;
                    dy = 0;
                }
                if (mouseRightButton) {
                    dx = (float) x - ox;
                    dy = (float) y - oy;
                    ox = (float) x;
                    oy = (float) y;
                }
            }
        };

        // zoom
        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                if (dx >= dy) {
                    camera.forward(trans);
                    if (deltaTrans < 0.001f)
                        deltaTrans = 0.001f;
                    else
                        deltaTrans *= 1.02;
                } else if (dx < dy) {

                    camera.backward(trans);
                    if (deltaTrans < 0.001f)
                        deltaTrans = 0.001f;
                    else
                        deltaTrans *= 1.02;
                }
            }
        };
    }

    @Override
    public void init() {
        super.init(); // TODO neviem ci budem potrebovat
        //background color
        glClearColor(0f, 0f, 0f, 1.0f);

        glEnable(GL_DEPTH_TEST);

        glDisable(GL_CULL_FACE);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_NONE);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrix);
        glDisable(GL_TEXTURE_2D);
        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();

        /**
         * @Textures
         * Textures are defined this "primitive way". If you use them directly in methods, you can see lag of program.
         */
        System.out.println("Loading texture from file: " + "textures/sun.jpg");
        try {
            sun = new OGLTexture2D("textures/sun.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/mercury.jpg");
        try {
            mercury = new OGLTexture2D("textures/mercury.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/venus.jpg");
        try {
            venus = new OGLTexture2D("textures/venus.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/earth.jpg");
        try {
            earth = new OGLTexture2D("textures/earth.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/moon.jpg");
        try {
            moon = new OGLTexture2D("textures/moon.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/mars.jpg");
        try {
            mars = new OGLTexture2D("textures/mars.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/jupiter.jpg");
        try {
            jupiter = new OGLTexture2D("textures/jupiter.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/saturn.jpg");
        try {
            saturn = new OGLTexture2D("textures/saturn.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/saturn_ring_a.jpg");
        try {
            saturnRingA = new OGLTexture2D("textures/saturn_ring_a.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/saturn_ring_b.jpg");
        try {
            saturnRingB = new OGLTexture2D("textures/saturn_ring_b.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/saturn_ring_c.jpg");
        try {
            saturnRingC = new OGLTexture2D("textures/saturn_ring_c.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/saturn_ring_d.jpg");
        try {
            saturnRingD = new OGLTexture2D("textures/saturn_ring_d.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/uranus.jpg");
        try {
            uranus = new OGLTexture2D("textures/uranus.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/neptune.jpg");
        try {
            neptune = new OGLTexture2D("textures/neptune.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading texture from file: " + "textures/stars.jpg");
        try {
            stars = new OGLTexture2D("textures/stars.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera = new GlCamera();
        camera.setPosition(new Vec3D(20, 20, 20));
        azimut = (float) Math.toDegrees(-0.5);
        zenit = (float) Math.toDegrees(-0.6);
        camera.setFirstPerson(true);    // it is not directly used
    }

    /**
     * Creation of objects
     */
    public void createSun() {
        glRotatef(angle * 3 , 0, 0, 1);  // orbital velocity
        sun.bind(); // texture mapping
        glutSolidSphere(3f, 30,30);   // diameter and "number of triangle" - edges, shape
    }

    public void createMoon() {
        glTranslatef(0.27f, 0, 0);
        moon.bind(); // texture mapping
        glutSolidSphere(0.0273f, 30,30);   // diameter and "number of triangle" - edges, shape
    }

    private void createPlanet(OGLTexture2D texture, float diameter, float axialTill,
            float orbitalVelocityAngle, float orbitalVelocityX, float orbitalVelocityY, // year
            float distanceFromSunX, float distanceFromSunY,
            float rotationPeriodAngle, float rotationPeriodX, float rotationPeriodY) {  // day

        glRotatef(angle * orbitalVelocityAngle, orbitalVelocityX, orbitalVelocityY, 1f);
        glRotatef(axialTill,1,0,0);
        glTranslatef(distanceFromSunX, distanceFromSunY, 0f);
        glRotatef(angle * rotationPeriodAngle, rotationPeriodX, rotationPeriodY, 1f);

        texture.bind(); // texture mapping
        glutSolidSphere(diameter, 30,30);   // diameter and number of triangles
    }

    // lines
    private void createRings() {
        double n = 1;
        int i, j;
        for (j = 0; j <= 50; j++) {
            n += Math.pow(0.04,n);
            glBegin(GL_LINE_STRIP);            ;
            for (i = 0; i <= 100; i++) {
                double angle = 2 * 3.14 * i / 100;
                double x = Math.cos(angle);
                double y = Math.sin(angle);
                glVertex2d(n * x, n * y);
            }
            glEnd();
        }
    }

    // from planets
    public void createRing(OGLTexture2D texture, float diameter, float distanceFromSaturn, float orbitalVelocityAngle) {
        glRotatef(angle * orbitalVelocityAngle, 0, 0, 1f);
        glTranslatef(distanceFromSaturn, 0, 0);
        texture.bind(); // texture mapping
        glutSolidSphere(diameter, 30,30);   // diameter and "number of triangle" - edges, shape
    }

    // skybox
    private void createStars() {
        stars.bind();
        glutSolidSphere(250, 50, 50);
    }

    // lights from Sun
    private void createLight () {
        float[] lightPosition = {0,0,0,1};
        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
        glShadeModel(GL_SMOOTH);
    }

    /**
     * Display scene
     */
    public void display() {
        camera.setAzimuth(Math.toRadians(azimut));
        camera.setZenith(Math.toRadians(zenit));
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);

//        // borders for skybox, its applicable but doesnt work clean
//        if (camera.getPosition().getX() < -80 && camera.getPosition().getX() > 80 ||
//            camera.getPosition().getY() < -130 && camera.getPosition().getY() > 130 ||
//            camera.getPosition().getZ() < -160 && camera.getPosition().getZ() > 160) {
//            // do something
//        }

        // movement W, S, A , D
        trans += deltaTrans;

        // projection
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if (projection)
            gluPerspective(40 * zoom, width / (float) height, 0.1f, 500.0f);
        else
            glOrtho((-20 * width / (float) height) * zoom,
                    (20 * width / (float) height) * zoom,
                    -20 * zoom, 20 * zoom, 0.1f, 500.0f);

        // rotation of planet
        if (move) {
            angle += speed;
        }

        // special effect of camera for skybox
        GlCamera cameraSkyBox = new GlCamera(camera);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glRotated(dx, 0, 1, 0); // movement in x ax
        glRotated(dy, 1, 0, 0); // movement in y ax
        glMultMatrixf(modelMatrix);
        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrix);
        dx = 0;
        dy = 0;

        switch (perspective) {
            // general perspective
            case 'r':
                glLoadIdentity();
                camera.setMatrix();
                gluLookAt(20, 0, 0, 0, 0, 0, 0, 0, 0.1);
                break;
            case 'q':
                glLoadIdentity();
                camera.setMatrix();
                gluLookAt(3, 0, 0, 20, 0, 0, 0, 0, 0.1);
                break;
            case 'x':
                glLoadIdentity();
                gluLookAt(32, 0, 0, 0, 0, 0, 0, 0, 0.1);
                break;
            case 'y':
                glLoadIdentity();
                gluLookAt(0, 50, 0, 0, 0, 0, 0, 0, 0.1);
                break;
            case 'z':
                glLoadIdentity();
                gluLookAt(0, 0.1, 60, 0, 0, 0, 0, 0, 0.1);
                break;
            // planets perspective, from Sun to Neptune
            case '0':
                glLoadIdentity();
                gluLookAt(3.1, 0, 0, 20, 0, 0, 0, 0, 0.1);
                break;
            case '1':
                glLoadIdentity();
                gluLookAt(3.5, 0, 0, 20, 0, 0, 0, 0, 0.1);
                break;
            case '2':
                glLoadIdentity();
                gluLookAt(4.8, 0, 0, 20, 0, 0, 0, 0, 0.1);
                break;
            case '3':
                glLoadIdentity();
                gluLookAt(5.6, 0, 0, 20, 0, 0, 0, 0, 0.1);
                break;
            case '4':
                glLoadIdentity();
                gluLookAt(6.9, 0, 0, 20, 0, 0, 0, 0, 0.1);
                break;
            case '5':
                glLoadIdentity();
                gluLookAt(14, 0, 0, 50, 0, 0, 0, 0, 0.1);
                break;
            case '6':
                glLoadIdentity();
                gluLookAt(22.5, 0, 0, 60, 0, 0, 0, 0, 0.1);
                break;
            case '7':
                glLoadIdentity();
                gluLookAt(28.5, 0, 0, 75, 0, 0, 0, 0, 0.1);
                break;
            case '8':
                glLoadIdentity();
                gluLookAt(31, 0, 0, 80, 0, 0, 0, 0, 0.1);
                break;
        }
        /**
         * Bonding of scene
         * Very important is order of objects, transformations
         */
        glMultMatrixf(modelMatrix);

        // A - Sun is shining
        // B - light drop in right direction on planets

        glEnable(GL_LIGHT1);    // A

        glPushMatrix();
            createSun();
        glPopMatrix();

        glDisable(GL_LIGHT1);   // A

        if (light) {
            glEnable(GL_LIGHTING);  // A
        }

        glPushMatrix();
            createPlanet(mercury,0.0383f, 0.01f,
                1.123f,0,0,
                3.4f,0,
                0.1699f,0, 0);
        glPopMatrix();

        glPushMatrix();
            createPlanet(venus,0.0949f,170.3f,
                0.438f,0,0,
                4.7f,0,
                -0.0411f,0, 0);
        glPopMatrix();

        glPushMatrix();
            createPlanet(earth,0.1f, 23.6f,             // diameter of Earth = 1
                0.274f,0,0,    // (1 year / 365 day) * 100, year - around Sun
                5.5f,0,
                10f,0, 0);     // (1 day) * 10, day - around the axis
                glPushMatrix();
                    createMoon();
                glPopMatrix();
        glPopMatrix();

        glPushMatrix();
            createPlanet(mars,0.0532f, 25.19f,
                0.137f,0,0,
                6.8f,0,
                9.73f,0, 0);
        glPopMatrix();

        glPushMatrix();
            createPlanet(jupiter,1.12f, 3.13f,
                0.0256f,0,0,
                12.5f,0,
                24.37f,0, 0);
        glPopMatrix();

        glPushMatrix();
            createPlanet(saturn,0.945f, 26.27f,
                0.0103f,0,0,
                17.2f,0,
                22.2f,0, 0);
                glPushMatrix();
                    createRings();
                    createRing(saturnRingA,0.025f,1.05f,2);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingB,0.02f,1.1f,5);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingC,0.015f,1.15f,9.5f);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingD,0.01f,1.2f,6);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingB,0.015f,1.25f,10);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingD,0.012f,1.3f,7);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingC,0.02f,1.35f,4);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingA,0.018f,1.4f,8);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingD,0.02f,1.45f,3);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingA,0.025f,1.5f,9);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingB,0.01f,1.55f,7.5f);
                glPopMatrix();
                glPushMatrix();
                    createRing(saturnRingC,0.012f,1.6f,1);
                glPopMatrix();
        glPopMatrix();

        glPushMatrix();
            createPlanet(uranus,0.4f, 97.77f,
                0.0036f,0,0,
                24f,0,
                -13.97f,0, 0);
        glPopMatrix();

        glPushMatrix();
            createPlanet(neptune,0.39f, 28.32f,
                0.0018f,0,0,
                30f,0,
                14.92f,0, 0);
        glPopMatrix();

        glDisable(GL_LIGHTING); // A
        glEnable(GL_LIGHT1);    // A

        glPushMatrix();
            if (freeze) {
                cameraSkyBox.setMatrix();
            }
            createStars();
        glPopMatrix();

        glEnable(GL_LIGHT0);    // B

        glPushMatrix();         // B
            createLight();      // B
        glPopMatrix();          // B

        String position = "Position: " + camera.getPosition().toString();
        position += String.format(" | Azimuth: %3.1f | Zenith: %3.1f", azimut, zenit);

        if (info) {
            textRenderer.addStr2D(5, 20, "CONTROL (SWITCH ON / OFF):");
            textRenderer.addStr2D(5, 60, "Information panel: I");
            textRenderer.addStr2D(5, 80, "Movement: W, S, A , D");
            textRenderer.addStr2D(5, 100, "Rotation: right or left mouse button");
            textRenderer.addStr2D(5, 120, "Zoom: arrows up and down, or mouse wheel");
            textRenderer.addStr2D(5, 140, "Speed of objects: plus (+) or minus (-)");
            textRenderer.addStr2D(5, 160, "Light from Sun: L");
            textRenderer.addStr2D(5, 180, "Freeze of Universe: F");
            textRenderer.addStr2D(5, 200, "Projection: P");
            textRenderer.addStr2D(5, 220, "General view: R, Q");
            textRenderer.addStr2D(5, 240, "Perspective view: X, Y, Z");
            textRenderer.addStr2D(5, 280, "View from the Sun direction for objects, with sound:");
            textRenderer.addStr2D(5, 300, "Sun: 0");
            textRenderer.addStr2D(5, 320, "Mercury: 1");
            textRenderer.addStr2D(5, 340, "Venus: 2");
            textRenderer.addStr2D(5, 360, "Earth: 3");
            textRenderer.addStr2D(5, 380, "Mars: 4");
            textRenderer.addStr2D(5, 400, "Jupiter: 5");
            textRenderer.addStr2D(5, 420, "Saturn: 6");
            textRenderer.addStr2D(5, 440, "Uranus: 7");
            textRenderer.addStr2D(5, 460, "Neptune: 8");

            textRenderer.addStr2D(5, 500, "Reset: Backspace (<-)");

            textRenderer.addStr2D(5, height -5, position);
            textRenderer.addStr2D(width - 190, height - 5, " (c) PGRF UHK: Jaroslav Podolak");
        }
    }
}
