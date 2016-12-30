package dk.sidereal.lumm.architecture.listeners;

import dk.sidereal.lumm.architecture.LummObject;

public abstract class OnResizeListener<T> {

    /**
     * Method that gets called when the screen is being resized
     *
     * @param x    the new window width
     * @param y    the new window height
     * @param oldX old window width
     * @param oldY old window height
     */
    public abstract void onResize(T caller, float x, float y, float oldX, float oldY);

    @SuppressWarnings("unchecked")
    public void attachTo(T... callers) {
        for (int i = 0; i < callers.length; i++) {
            T caller = callers[i];

            if (caller == null) {
                throw new NullPointerException("Attempting to use OnResizeListener.attachTo on a null object ( index " + i + " )");
            }

            if (caller instanceof LummObject) {
                ((LummObject) caller).onResizeListener = (OnResizeListener<LummObject>) this;
            } else if (caller instanceof LummObject) {
                ((LummObject) caller).onResizeListener = (OnResizeListener<LummObject>) this;
            } else if (caller instanceof LummObject) {
                ((LummObject) caller).onResizeListener = (OnResizeListener<LummObject>) this;
            }

        }
    }
}
