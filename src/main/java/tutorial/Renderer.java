package tutorial;

import display.BasicShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Renderer {

    private BasicShaderProgram shaderProgram;

    //Prepare Open GL for a new render. Done each frame we render.
    public void prepare() {
        GL11.glClearColor(1, 0, 0, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    public void render(ModelMesh model) {
        //shaderProgrambind();

        GL30.glBindVertexArray(model.vaoId);
        GL20.glEnableVertexAttribArray(0);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        //shaderProgram.unbind();
    }
}
