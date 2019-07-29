package tutorial;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LoaderUtil {

    private List<Integer> vaoList = new ArrayList<>();
    private List<Integer> vboList = new ArrayList<>();
    private List<Integer> textureList = new ArrayList<>();

    public ModelMesh loadToVao(float[] vertices, float[] textureCoords, int[] indices) {
        //-------------------

        int vaoId = GL30.glGenVertexArrays();
        vaoList.add(vaoId);
        GL30.glBindVertexArray(vaoId);

        //-------------------
        vboList.add(
            createArrayBufferVBO(0, 3, vertices)
        );

        //-------------------
        /*
        FloatBuffer textureCoordsBuffer = MemoryUtil.memAllocFloat(textureCoords.length).put(textureCoords);
        textureCoordsBuffer.flip();
        // Create the VBO and bind to it.
        vboId = GL15.glGenBuffers();
        vboList.add(vboId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordsBuffer, GL15.GL_STATIC_DRAW);
        //Define structure of the data.
        GL20.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, 0, 0);
        // Unbind the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(textureCoordsBuffer);
        */

        vboList.add(
            createArrayBufferVBO(1, 2, textureCoords)
        );


        //-------------------
        //No special method yet for element array
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        //Create the indices VBO and bind to it.
        int vboId = GL15.glGenBuffers();
        vboList.add(vboId);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL30.GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);
        // Unbind the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(indicesBuffer);
        //-------------------

        //Unbind the VAO.
        GL30.glBindVertexArray(0);

        return new ModelMesh(vaoId, indices.length);
    }

    private int createArrayBufferVBO(int attributeIndex, int attributeSize, float[] attributeValues) {
        FloatBuffer attributeValuesBuffer = MemoryUtil.memAllocFloat(attributeValues.length).put(attributeValues);
        attributeValuesBuffer.flip();
        // Create the VBO and bind to it.
        int vboId = GL15.glGenBuffers();
        vboList.add(vboId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, attributeValuesBuffer, GL15.GL_STATIC_DRAW);
        //Define structure of the data.
        GL20.glVertexAttribPointer(attributeIndex, attributeSize, GL20.GL_FLOAT, false, 0, 0);
        // Unbind the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(attributeValuesBuffer);
        return vboId;
    }

    public ModelTexture loadTexture(String fileName) {
        //Load texture file.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            //I do not care for this way of loading files.
            URL url = LoaderUtil.class.getResource(fileName);
            File file = Paths.get(url.toURI()).toFile();
            String filePath = file.getAbsolutePath();
            System.out.println("filepath: " + filePath);
            ByteBuffer byteBuffer = STBImage.stbi_load(filePath, w, h, channels, 4);
            if (byteBuffer == null) {
                throw new Exception("File [" + filePath + "] not loaded: " + STBImage.stbi_failure_reason());
            }
            int width = w.get();
            int height = h.get();

            //Create a new OpenGL texture.
            int textureId = GL30.glGenTextures();
            //Bind the texture.
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId);
            //Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte in size.
            GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);

            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);

            //Upload the texture data.
            //Upload texture data.
            GL30.glTexImage2D(
                GL30.GL_TEXTURE_2D, //The target texture type
                0, //level-of-detail number: 0 is base image level, n is nth
                GL30.GL_RGBA, //internal format specifies the number of color components
                width,
                height,
                0, //border: this value must be 0
                GL30.GL_RGBA, //format specifies the format of pixel data
                GL30.GL_UNSIGNED_BYTE, //type specifies the data type of pixel data
                byteBuffer //the buffer that stores our data
            );
            //Generate mipmaps (as opposed to setting filtering parameters).
            GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);

            //Free the memory of the raw image data.
            STBImage.stbi_image_free(byteBuffer);

            textureList.add(textureId);
            return new ModelTexture(textureId);
        } catch (URISyntaxException e) {
            System.out.println("bad uri");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void cleanUp() {
        for (int vaoId : vaoList) {
            GL30.glDeleteVertexArrays(vaoId);
        }
        for (int vboId : vboList) {
            GL15.glDeleteBuffers(vboId);
        }
        for (int textureId : textureList) {
            GL11.glDeleteTextures(textureId);
        }
    }
}
