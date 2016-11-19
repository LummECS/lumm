package com.sidereal.lumm.architecture.concrete;

import com.sidereal.lumm.architecture.LummScene;

/**
 * Conveniance class to be extended when the user does not want to have the
 * entire {@link LummScene} API visible, the user being able to override certain
 * parts of the API if necessary.
 * 
 * @author Claudiu Bele
 */
public class ConcreteLummScene extends LummScene {

	@Override
	protected void onCreate(Object[] params) {

	}

	@Override
	protected void onCreateSceneLayers() {

	}

	@Override
	protected void onPause(boolean value) {

	}

}
