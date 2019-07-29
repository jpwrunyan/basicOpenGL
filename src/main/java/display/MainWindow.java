package display;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;

public class MainWindow {

    private static MainWindow instance;

    public int width = 1280;
    public int height = 720;
    public boolean resized = false;

    public final long id;

    public static MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private MainWindow() {
        //This is optional, current window hints are already default.
        GLFW.glfwDefaultWindowHints();
        //The window will stay hidden after creation.
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        //The window will be resizable.
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        //This will make the program use the highest OpenGL version possible between 3.2 and 4.1. If those lines are not included, a Legacy version of OpenGL is used.
        //GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        //GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        //
        //GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        //GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        id = GLFW.glfwCreateWindow(width, height, "title", MemoryUtil.NULL, MemoryUtil.NULL);
        if (id == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window.");
        }

        // Setup resize callback
        GLFW.glfwSetFramebufferSizeCallback(id, (windowId, width, height) -> {
            this.width = width;
            this.height = height;
            resized = true;
        });

        //Default callback:
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetKeyCallback(id, (windowId, key, scancode, action, mods) -> {
            if ( key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE )
                GLFW.glfwSetWindowShouldClose(windowId, true); // We will detect this in the rendering loop
        });



        //A bit overkill...
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(id, w, h);
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            //Center the window.
            GLFW.glfwSetWindowPos(
                id,
                (vidMode.width() - w.get(0)) / 2,
                (vidMode.height() - h.get(0)) / 2
            );
        }

        //Make the OpenGL context current.
        GLFW.glfwMakeContextCurrent(id);

        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        //Make the window visible.
        GLFW.glfwShowWindow(id);



        //Not strictly related to this window, but needs to be initialized regardless.

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        //GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        System.out.println("Main Window initialized OpenGL: " + GL11.glGetString(GL11.GL_VERSION));
    }

    public boolean winodowShouldClose() {
        return GLFW.glfwWindowShouldClose(id);
    }
}
