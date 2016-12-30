package dk.sidereal.lumm.architecture.listeners;

public interface OnParentDisposeListener<T, U> {

    /**
     * Called when the parent LummObject/LummScene is about to be disposed, before they are disposing other things (in case of LummObject the components attached to it)
     */
    public void onDispose(T parent, U object);


}
