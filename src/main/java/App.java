import engine.BasicShaderProgram;
import engine.MainWindow;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.*;
import tutorial.LoaderUtil;
import tutorial.RawModel;
import tutorial.Renderer;
import tutorial.RawTexture;


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

        float[] textureCoords = new float[] {
            0, 0,
            1, 0,
            1, 1,
            0, 1
        };

        RawModel model = loader.loadToVao(testVertices, textureCoords, testIndices);
        RawTexture texture = loader.loadTexture("/grassblock.png");


        try {
            //Create shader program(s).
            BasicShaderProgram shaderProgram = new BasicShaderProgram();

            //Create texture sampler uniform.
            int textureSamplerUniformLocation = GL20.glGetUniformLocation(shaderProgram.programId, "textureSampler");
            if (textureSamplerUniformLocation < 0) {
                throw new Exception("Could not find uniform: textureSampler");
            }

            //GL41.glProgramUniform1i(shaderProgram.programId, textureSamplerUniformLocation, texture.id);

            System.out.println("texture id: " + texture.id);
            System.out.println("textureSamplerUniformLocation " + textureSamplerUniformLocation);
            //GL20.glUniform1i(textureSamplerUniformLocation, texture.id);

            while (!MainWindow.getInstance().winodowShouldClose()) {
                //Could be moved to main window....
                GL11.glClearColor(0.20f, 0.20f, 0.20f, 0.20f);

                //To be moved to the renderer? Needs to be called every update.
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer


                //Set the shader program to use.
                //shaderProgram.bind();
                GL20.glUseProgram(shaderProgram.programId);

                //Set the texture sampler on the program.
                //Must be done before calling bind(); ?!?!
                //This does not make sense...
                //GL20.glUniform1i(textureSamplerUniformLocation, texture.id);
                GL41.glProgramUniform1i(shaderProgram.programId, textureSamplerUniformLocation, 0); // using texture bank 0.


                //Do rendering.
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id); //for now just bind the texture to 0...

                //Bind the VAO.
                //The shader program will read these in on its own.
                GL30.glBindVertexArray(model.vaoId);
                GL30.glEnableVertexAttribArray(0); //VBO0
                GL30.glEnableVertexAttribArray(1); //VBO1

                //Draw the vertices.
                //This will trigger the shader program main() for each shader.
                //Draw arrays is for raw vertices with no indices.
                //GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount);
                GL11.glDrawElements(GL11.GL_TRIANGLES, model.vertexCount, GL11.GL_UNSIGNED_INT, 0);

                // Restore state
                GL30.glDisableVertexAttribArray(0);
                GL30.glDisableVertexAttribArray(1);
                GL30.glBindVertexArray(0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

                //Release the program.
                //shaderProgram.unbind();
                GL20.glUseProgram(0);

                GLFW.glfwSwapBuffers(windowId); // swap the color buffers
                // Poll for window events. The key callback above will only be
                // invoked during this call.
                GLFW.glfwPollEvents();


                //renderer.render(model);
            }
            GL20.glDeleteProgram(shaderProgram.programId);
            //loader.cleanUp();
        } catch (Exception e) {
            e.printStackTrace();
        }

        loader.cleanUp();

        Callbacks.glfwFreeCallbacks(MainWindow.getInstance().id);
        GLFW.glfwDestroyWindow(MainWindow.getInstance().id);

        GLFW.glfwTerminate();;
        GLFW.glfwSetErrorCallback(null).free();
        System.out.println("all done");
    }
}
