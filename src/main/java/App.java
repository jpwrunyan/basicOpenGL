import engine.BasicShaderProgram;
import engine.MainWindow;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import tutorial.LoaderUtil;
import tutorial.RawModel;
import tutorial.Renderer;
import tutorial.Texture;


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


        LoaderUtil loader = new LoaderUtil();
        Renderer renderer = new Renderer();


        float[] testVertices = new float[] {
            -0.5f, 0.5f, 0,
            -0.5f, -0.5f, 0,
            0.5f, -0.5f, 0,
            0.5f, 0.5f, 0
        };

        int[] testIndices = new int[] {
            0, 1, 3, 3, 1, 2
        };

        //TODO: need to add the indices. Duh.
        //do this in tutorial 3
        //https://www.youtube.com/watch?v=z2yFlvkBbmk


        float[] textureCoords = new float[] {
            0, 1, 3,
            3, 1, 2
        };

        RawModel model = loader.loadToVao(testVertices, testIndices);
        //Why not return a texture class???
        int textureId = loader.loadTexture("/grassblock.png");
        Texture texture = new Texture(textureId);

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

                //Set the shader program to use.
                shaderProgram.bind();

                //Do rendering.

                //Bind the VAO.
                //The shader program will read these in on its own.
                GL30.glBindVertexArray(model.vaoId);
                GL30.glEnableVertexAttribArray(0);

                //Draw the vertices.
                //This will trigger the shader program main() for each shader.
                //Draw arrays is for raw vertices with no indices.
                //GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount);
                GL11.glDrawElements(GL11.GL_TRIANGLES, model.vertexCount, GL11.GL_UNSIGNED_INT, 0);
                // Restore state
                GL30.glDisableVertexAttribArray(0);
                GL30.glBindVertexArray(0);

                //Release the program.
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

        loader.cleanUp();

        Callbacks.glfwFreeCallbacks(MainWindow.getInstance().id);
        GLFW.glfwDestroyWindow(MainWindow.getInstance().id);

        GLFW.glfwTerminate();;
        GLFW.glfwSetErrorCallback(null).free();
    }
}
