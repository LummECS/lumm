package com.sidereal.lumm.architecture.core.input;

/**
 * Class encapsulating an event to run when writing from the keyboard.
 *
 * @author Claudiu Bele
 */
public abstract class OnKeyTypedListener {

    /**
     * Called whenever a key is typed, passing the character representation of
     * the character to this method. This does not guarantee that all of the
     * characters could be readable ( such as Shift, PageUp, etc.)
     *
     * @param character The character tied to the key pressed
     * @return whether the action was handled or not, for multiplexing
     */
    public boolean run(char character) {
        return false;
    }

}
