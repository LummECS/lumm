package com.sidereal.lumm.architecture.core.input;

import com.sidereal.lumm.architecture.core.Input;
import com.sidereal.lumm.architecture.core.Input.Action;
import com.sidereal.lumm.architecture.core.Input.ActionType;

/**
 * Wrapper for {@link OnTouchListener}, containing all relevant data for running the
 * event at the proper times. Created internally in
 * {@link Input#addOnTouchListener(String, int, OnTouchListener, ActionType)}.
 *
 * @author Claudiu Bele
 */
public class TouchEventWrapper {

    public int code;

    public OnTouchListener event;

    public ActionType inputType;

    /**
     * Event to run when a particular finger is pressed, held, or released
     *
     * @param code      the {@link Action} value tied to a finger.
     * @param event     The event to run when something ( based on inputType) happends
     *                  to the given code
     * @param inputType The type of action to run the application on.
     *                  {@link ActionType#Up}, {@link ActionType#Down} or
     *                  {@link ActionType#Hold} are supported.
     */
    public TouchEventWrapper(int code, OnTouchListener event, ActionType inputType) {
        this.code = code;
        this.event = event;
        this.inputType = inputType;
    }

}
