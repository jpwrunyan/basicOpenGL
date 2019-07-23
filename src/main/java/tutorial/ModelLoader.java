package tutorial;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ModelLoader {

    private List<Integer> vaoList = new ArrayList<>();
    private List<Integer> vboList = new ArrayList<>();

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

    public void cleanUp() {
        for (int vaoId : vaoList) {
            GL30.glDeleteVertexArrays(vaoId);
        }
        for (int vboId : vboList) {
            GL15 .glDeleteBuffers(vboId);
        }
    }
}
