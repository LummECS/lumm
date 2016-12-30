package dk.sidereal.lumm.architecture.core.input;

import dk.sidereal.lumm.architecture.core.Input;

/**
 * Class encapsulating an event to run when pressing, holding or releasing
 * fingers on a multi touch device. The moment at which the event is called is
 * based on the {@link Input.ActionType} parameter passed to
 * {@link Input#addTouchEvent}.
 * <p>
 * User must implement the {@link #run(TouchData)} method , getting access to
 * individual finger press position, initial position, delta position.
 *
 * @author Claudiu Bele
 */
public abstract class OnTouchListener {

    /**
     * Method to run whenever a user presses, holds or releases a finger. If the
     * user decides that the data was not handled or the purpose of the method
     * has not been achieved due to some external reasons, a false must be
     * returned, whereas if the wanted functionality has been achieved, return
     * true.
     * <p>
     * This is done in order for events to be potentially handled in the next
     * input processor found in the Input Multiplexer.
     *
     * @param inputData
     * @return whether data was handled as intended
     */
    public abstract boolean run(TouchData inputData);

}
