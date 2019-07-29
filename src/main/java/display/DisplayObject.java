package display;

import org.joml.Vector3f;
import tutorial.ModelMesh;
import tutorial.ModelTexture;

public class DisplayObject {

    public ModelMesh mesh;
    public ModelTexture texture;

    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector3f rotation = new Vector3f(0, 0, 0);
    private float scale = 1;

    public DisplayObject(ModelMesh mesh, ModelTexture texture) {
        this.mesh = mesh;
        this.texture = texture;
    }

    public Vector3f getPosition() {
        return position;
    }

    public DisplayObject setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
        return this;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public DisplayObject setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
        return this;
    }

    public float getScale() {
        return scale;
    }

    public DisplayObject setScale(float scale) {
        this.scale = scale;
        return this;
    }
}
