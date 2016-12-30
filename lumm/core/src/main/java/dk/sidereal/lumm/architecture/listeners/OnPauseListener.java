package dk.sidereal.lumm.architecture.listeners;

import dk.sidereal.lumm.architecture.Lumm;

public interface OnPauseListener<T> {
    /**
     * Method that gets called whenever the game is paused (Due to application
     * focus being lost/regained, as well as the when
     * {@link Lumm#pause(boolean)} is manually called.
     * <p>
     * This method should not be manually called.
     *
     * @param caller The caller of the function.
     * @param pause  represents a pause for true, and out of pause(resume) for
     *               false.
     */
    public void onPause(T caller, boolean value);
}
