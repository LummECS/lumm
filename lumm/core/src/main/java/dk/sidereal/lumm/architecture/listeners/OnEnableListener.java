package dk.sidereal.lumm.architecture.listeners;

public interface OnEnableListener<T> {

    /**
     * Called whenever an objects{@link IEnableable#setEnabled(boolean)} after
     * changing the status of enabled and before successfully returning true.
     *
     * @param newValue
     */
    public void onEnable(T caller, boolean newValue);

}
