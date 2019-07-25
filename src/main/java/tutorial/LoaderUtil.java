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

    public RawModel loadToVao(float[] vertices) {
        //-------------------

        int vaoId = GL30.glGenVertexArrays();
        vaoList.add(vaoId);
        GL30.glBindVertexArray(vaoId);

        //-------------------
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length).put(vertices);
        verticesBuffer.flip();
        // Create the VBO and bind to it.
        int vboId = GL15.glGenBuffers();
        vboList.add(vboId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);

        // Define structure of the data
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 0, 0);

        // Unbind the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        if (verticesBuffer != null) {
            MemoryUtil.memFree(verticesBuffer);
        }
        //-------------------

        //Unbind the VAO.
        GL30.glBindVertexArray(0);

        return new RawModel(vaoId, vertices.length / 3);
    }

    public int loadTexture(String fileName) {
        ByteBuffer byteBuffer;

        //Load texture file.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            //I do not care for this way of loading files.
            URL url = LoaderUtil.class.getResource(fileName);
            File file = Paths.get(url.toURI()).toFile();
            String filePath = file.getAbsolutePath();
            byteBuffer = STBImage.stbi_load(filePath, w, h, channels, 4);
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
            textureList.add(textureId);
            return textureId;
        } catch (URISyntaxException e) {
            System.out.println("bad uri");
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
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
