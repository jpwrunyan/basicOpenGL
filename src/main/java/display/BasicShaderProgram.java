package display;

import org.lwjgl.opengl.GL20;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 1. Create an OpenGL program.
 * 2. Load the vertex and fragment shader code files.
 * 3. For each shader, create a new shader program and specify its type (vertex, fragment).
 * 4. Compile the shader.
 * 5. Attach the shader to the program.
 * 6. Link the program.
 */
public class BasicShaderProgram {

    public final int programId;
    //private final int vertexShaderId;
    //private final int fragmentShaderId;
    private final Map<String, Integer> uniforms = new HashMap<>();

    private static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Class.forName(BasicShaderProgram.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

    /**
     * Creates the OpenGL program.
     * @throws Exception
     */
    public BasicShaderProgram() throws Exception {
        programId = GL20.glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create shader program!");
        }
        //If we're deleting these right away and not re-using them... why keep them?
        int vertexShaderId = createShader(loadResource("/basic.vs"), GL20.GL_VERTEX_SHADER);
        int fragmentShaderId = createShader(loadResource("/basic.fs"), GL20.GL_FRAGMENT_SHADER);

        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking shader code: " + GL20.glGetProgramInfoLog(programId, 1024));
        }
        //Once the shader program has been linked, the compiled vertex and fragment shaders can be freed up.
        if (vertexShaderId != 0) {
            GL20.glDetachShader(programId, vertexShaderId);
            GL20.glDeleteShader(vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            GL20.glDetachShader(programId, fragmentShaderId);
            GL20.glDeleteShader(fragmentShaderId);
        }
        //TODO: try deleting the shaders also since we shouldn't need them anymore.
        //Validate the program from debugging. Remove later.
        GL20.glValidateProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0) {
            throw new Exception("Warning validating shader code: " + GL20.glGetProgramInfoLog(programId, 1024));
        }
        System.out.println("shader program created");
    }

    /**
     * Create a new shader program (by type), compile it, and attach it to the program.
     * @param shaderCode The shader code to compile
     * @param shaderType The type of shader program (vertex or fragment)
     * @return The id of the shader program created
     * @throws Exception
     */
    private int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader type: " + shaderType);
        }

        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024));
        }

        GL20.glAttachShader(programId, shaderId);

        return shaderId;
    }

    /**
     * Activate the program for rendering.
     */
    public void bind() {
        GL20.glUseProgram(programId);
    }

    /**
     * Deactivate the program from rendering.
     */
    public void unbind() {
        GL20.glUseProgram(0);
    }

    /**
     * Free all resources once no longer needed.
     */
    public void cleanup() {
        unbind();
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }
}
