import engine.BasicShaderProgram;
import engine.MainWindow;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import tutorial.ModelLoader;
import tutorial.RawModel;
import tutorial.Renderer;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class App implements Runnable {
    public static void main(String[] args) {
        new Thread(new App(), "OpenGlThread").start();
    }

    public void run() {
        //Sets up an error callback.
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initalize GLFW");
        }
        /*
        GLFW.glfwSetKeyCallback(
            MainWindow.getInstance().id,
            (long window, int key, int scanCode, int action, int mods) -> {
                //keys[key] = action == GLFW_RELEASE ? false : true;
                System.out.println("window: " + window + " key: " + key + " scanCode: " + scanCode);
            }
        );
         */

        //Initialize the window.
        long windowId = MainWindow.getInstance().id;



        boolean running = true;


        ModelLoader loader = new ModelLoader();
        Renderer renderer = new Renderer();

        float[] testVertices = new float[] {
            -0.5f, 0.5f, 0,
            -0.5f, -0.5f, 0,
            0.5f, -0.5f, 0,
            0.5f, -0.5f, 0,
            0.5f, 0.5f, 0,
            -0.5f, 0.5f, 0
        };

        RawModel model = loader.loadToVao(testVertices);
        float tempVar = 0.20f;
        try {
            //Create shader program(s).
            BasicShaderProgram shaderProgram = new BasicShaderProgram();

            while (!MainWindow.getInstance().winodowShouldClose()) {
                //Could be moved to main window....
                GL11.glClearColor(tempVar, tempVar, tempVar, tempVar);
                //tempVar += 0.1f;
                //if (tempVar > 1) {
                //    tempVar = 0;
                //}

                //To be moved to the renderer? Needs to be called every update.
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

                shaderProgram.bind();
                //Do rendering.
                // Bind to the VAO
                glBindVertexArray(model.vaoId);
                glEnableVertexAttribArray(0);

                // Draw the vertices
                glDrawArrays(GL_TRIANGLES, 0, model.vertexCount);

                // Restore state
                glDisableVertexAttribArray(0);
                glBindVertexArray(0);

                shaderProgram.unbind();


                GLFW.glfwSwapBuffers(windowId); // swap the color buffers
                // Poll for window events. The key callback above will only be
                // invoked during this call.
                GLFW.glfwPollEvents();


                //renderer.render(model);
            }

            //loader.cleanUp();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Callbacks.glfwFreeCallbacks(MainWindow.getInstance().id);
        GLFW.glfwDestroyWindow(MainWindow.getInstance().id);

        GLFW.glfwTerminate();;
        GLFW.glfwSetErrorCallback(null).free();
    }
}