package com.sidereal.lumm.architecture.listeners;

public interface OnUpdateListener<T> {

	/**
	 * Called whenever a scene/scenelayer/object/component is updated
	 * @param newValue
	 */
	public void onUpdate(T caller);

}
