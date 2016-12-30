package dk.sidereal.lumm.architecture.concrete;

import dk.sidereal.lumm.architecture.LummComponent;
import dk.sidereal.lumm.architecture.LummObject;

/**
 * Conveniance class to be extended when the user does not want to have the
 * entire {@link LummComponent} API visible, the user being able to override
 * certain parts of the API if necessary.
 *
 * @author Claudiu Bele
 */
public class ConcreteLummComponent extends LummComponent {

    public ConcreteLummComponent(LummObject obj) {
        super(obj);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onRender() {

    }

    @Override
    protected void onDebug() {

    }


}
