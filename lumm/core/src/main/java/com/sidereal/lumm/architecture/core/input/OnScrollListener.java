package com.sidereal.lumm.architecture.core.input;

/**
 * Class encapsulating an event to run when scrolling
 *
 * @author Claudiu Bele
 */
public abstract class OnScrollListener {

    /**
     * Event to run whenever the mouse wheel is scrolled. The amount of scroll
     * is not guaranteed to be the same for the same scroll amount on multiple
     * mice.
     *
     * @param scrollAmount the amount that been scrolled
     * @return whether the action was handled, for multiplexing.
     */
    public boolean run(int scrollAmount) {
        return false;
    }

}
