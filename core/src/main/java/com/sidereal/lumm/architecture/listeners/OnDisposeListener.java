package com.sidereal.lumm.architecture.listeners;

public interface OnDisposeListener<T> {

	/**
	 * Called when the application is exited. Clean up and potential recovery
	 * should be done if the dispose was not done at a proper time.
	 */
	public void onDispose(T caller);

}
