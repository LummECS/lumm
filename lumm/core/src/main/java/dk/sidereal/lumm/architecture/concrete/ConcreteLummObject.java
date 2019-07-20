package dk.sidereal.lumm.architecture.concrete;

import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.LummScene;

/**
 * Conveniance class to be extended when the user does not want to have the
 * entire {@link LummObject} API visible, the user being able to override
 * certain parts of the API if necessary.
 *
 * @author Claudiu Bele
 */
public class ConcreteLummObject extends LummObject {

    public ConcreteLummObject() {
        this(Lumm.getScene(), (Object[]) null);
    }

    public ConcreteLummObject(Object... params) {
        this(Lumm.getScene(), params);
    }

    /**
     * Default LummObject constructor.
     *
     * @param scene
     * @param params parameters passed to {@link #onCreate(Object...)}
     */
    public ConcreteLummObject(LummScene scene, Object... params) {

        super(scene, params);
    }

    @Override
    protected void onCreate(Object... params) {

    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onRender() {

    }

    @Override
    protected void onPause(Boolean running) {

    }

    @Override
    protected void onSceneChange() {

    }


}
