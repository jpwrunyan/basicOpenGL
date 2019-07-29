import display.BasicShaderProgram;
import display.DisplayObject;
import display.MainWindow;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;
import tutorial.LoaderUtil;

import java.nio.FloatBuffer;




public class App implements Runnable {
    public static void main(String[] args) {
        new Thread(new App(), "OpenGlThread").start();
    }

    //Camera projection settings.
    private static final float FOV = (float) Math.toRadians(60f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000f;

    public void run() {
        //Sets up an error callback.
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initalize GLFW");
        }

        //Initialize the window.
        long windowId = MainWindow.getInstance().id;

        LoaderUtil loader = new LoaderUtil();

        float[] testVertices = new float[] {
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f
        };

        int[] testIndices = new int[] {
            2, 1, 0, 2, 3, 1
        };

        float[] textureCoords = new float[] {
            0, 1,
            1, 1,
            0, 0,
            1, 0,
        };

        //ModelMesh model = loader.loadToVao(testVertices, textureCoords, testIndices);
        //ModelTexture texture = loader.loadTexture("/quad_texture.png");

        DisplayObject displayObject = new DisplayObject(
            loader.loadToVao(testVertices, textureCoords, testIndices),
            loader.loadTexture("/Mermaid.png")
        ).setPosition(0, 0, -2);

        //----------------------------------------
        // Set up model view matrix value.
        //----------------------------------------
        Matrix4f modelViewMatrix = new Matrix4f();

        /*
        public Matrix4f createModelViewMatrix(DisplayObject displayObject, Matrix4f viewMatrix) {
        Vector3f rotation = displayObject.getRotation();
        modelViewMatrix.set(viewMatrix).translate(displayObject.getPosition())
            .rotateX((float) Math.toRadians(-rotation.x))
            .rotateY((float) Math.toRadians(-rotation.y))
            .rotateZ((float) Math.toRadians(-rotation.z))
            .scale(displayObject.getScale());
        return modelViewMatrix;
        }
         */

        //TODO implement camera transformation.


        try {
            //Create shader program(s).
            BasicShaderProgram shaderProgram = new BasicShaderProgram();

            //----------------------------------------
            // Get the texture sampler uniform location id.
            //----------------------------------------
            int textureSamplerUniformLocation = GL20.glGetUniformLocation(shaderProgram.programId, "textureSampler");
            if (textureSamplerUniformLocation < 0) {
                throw new Exception("Could not find uniform: textureSampler");
            }

            //----------------------------------------
            // Get the projection matrix uniform location id.
            //----------------------------------------
            int projectionMatrixUniformLocation = GL20.glGetUniformLocation(shaderProgram.programId, "projectionMatrix");
            if (projectionMatrixUniformLocation < 0) {
                throw new Exception("Could not find uniform: projectionMatrix");
            }

            //----------------------------------------
            // Get the model view matrix uniform location id.
            //----------------------------------------
            int modelViewMatrixUniformLocation = GL20.glGetUniformLocation(shaderProgram.programId, "modelViewMatrix");
            if (modelViewMatrixUniformLocation < 0) {
                throw new Exception("Could not find uniform: modelViewMatrix");
            }

            //Could be moved to main window....
            GL11.glClearColor(0.20f, 0.20f, 0.20f, 0.20f);

            while (!MainWindow.getInstance().winodowShouldClose()) {

                //----------------------------------------
                //Set the model-view matrix.
                //----------------------------------------

                //update some values
                Vector3f displayObjectRotation = displayObject.getRotation();

                // Update rotation angle
                float rotation = displayObject.getRotation().z + 1.5f;
                displayObject.setRotation(0, 0, rotation);

                //To be moved to the renderer? Needs to be called every update.
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer
                GL11.glViewport(0, 0, MainWindow.getInstance().width, MainWindow.getInstance().height);

                //Set the shader program to use.
                //shaderProgram.bind();
                GL20.glUseProgram(shaderProgram.programId);

                //----------------------------------------
                //Update projection matrix:
                float aspectRatio = (float) MainWindow.getInstance().width / MainWindow.getInstance().height;
                Matrix4f projectionMatrix = new Matrix4f().identity().perspective(
                    FOV, aspectRatio, Z_NEAR, Z_FAR
                );

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    FloatBuffer floatBuffer = stack.mallocFloat(16);
                    projectionMatrix.get(floatBuffer);
                    GL41.glProgramUniformMatrix4fv(shaderProgram.programId, projectionMatrixUniformLocation, false, floatBuffer);
                }

                //----------------------------------------
                // Get the "world matrix"
                modelViewMatrix = new Matrix4f().identity();
                modelViewMatrix.translate(displayObject.getPosition())
                        //.rotateXYZ() //use for radians.
                        .rotateX((float) Math.toRadians(-displayObjectRotation.x))
                        .rotateY((float) Math.toRadians(-displayObjectRotation.y))
                        .rotateZ((float) Math.toRadians(-displayObjectRotation.z))
                        .scale(displayObject.getScale());

                //Matrix4f projectionMatrix = transformation.createProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    FloatBuffer floatBuffer = stack.mallocFloat(16);
                    modelViewMatrix.get(floatBuffer);
                    GL41.glProgramUniformMatrix4fv(shaderProgram.programId, modelViewMatrixUniformLocation, false, floatBuffer);
                }
                //----------------------------------------












                //Set the texture sampler on the program.
                //Must be done before calling bind(); ?!?!
                //This does not make sense...
                //GL20.glUniform1i(textureSamplerUniformLocation, texture.id);
                GL41.glProgramUniform1i(shaderProgram.programId, textureSamplerUniformLocation, 0); // using texture bank 0.


                //Do rendering.
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, displayObject.texture.id); //for now just bind the texture to 0...

                //Bind the VAO.
                //The shader program will read these in on its own.
                GL30.glBindVertexArray(displayObject.mesh.vaoId);
                GL30.glEnableVertexAttribArray(0); //VBO0
                GL30.glEnableVertexAttribArray(1); //VBO1

                //Draw the vertices.
                //This will trigger the shader program main() for each shader.
                //Draw arrays is for raw vertices with no indices.
                //GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount);
                GL11.glDrawElements(GL11.GL_TRIANGLES, displayObject.mesh.vertexCount, GL11.GL_UNSIGNED_INT, 0);

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
