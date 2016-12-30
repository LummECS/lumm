package dk.sidereal.lumm.architecture.core.input;

import dk.sidereal.lumm.architecture.core.Input;

/**
 * Wrapper for {@link OnTouchListener}, containing all relevant data for running the
 * event at the proper times. Created internally in
 * {@link Input#addOnTouchListener(String, int, OnTouchListener, Input.ActionType)}.
 *
 * @author Claudiu Bele
 */
public class TouchEventWrapper {

    public int code;

    public OnTouchListener event;

    public Input.ActionType inputType;

    /**
     * Event to run when a particular finger is pressed, held, or released
     *
     * @param code      the {@link Input.Action} value tied to a finger.
     * @param event     The event to run when something ( based on inputType) happends
     *                  to the given code
     * @param inputType The type of action to run the application on.
     *                  {@link Input.ActionType#Up}, {@link Input.ActionType#Down} or
     *                  {@link Input.ActionType#Hold} are supported.
     */
    public TouchEventWrapper(int code, OnTouchListener event, Input.ActionType inputType) {
        this.code = code;
        this.event = event;
        this.inputType = inputType;
    }

}
