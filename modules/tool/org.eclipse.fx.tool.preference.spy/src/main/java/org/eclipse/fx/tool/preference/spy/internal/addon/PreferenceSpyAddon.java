/*******************************************************************************
 * Copyright (c) Carl Zeiss Meditec AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Fahrmeyer (lauritziu_ii@yahoo.de) - initial API and implementation
*/
package org.eclipse.fx.tool.preference.spy.internal.addon;

import org.eclipse.fx.tool.preference.spy.internal.model.PreferenceTracker;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.fx.core.log.FluentLogger;
import org.eclipse.fx.core.log.Log;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Addon does some initialization  
 */
public class PreferenceSpyAddon {

	@Inject
	@Log
	private FluentLogger flogger;
	 
	/**
	 * creates PreferenceTracker and puts it to application context 
	 *  
	 * @param preferenceTracker
	 * @param application
	 * @param bundlePreferences
	 */
	@PostConstruct
	public void initialzePreferenceSpy( PreferenceTracker preferenceTracker, MApplication application, @Preference IEclipsePreferences bundlePreferences  ) {
		
		application.getContext().set( PreferenceTracker.class.getName(), preferenceTracker );

		flogger.atInfo().log( "PreferenceTracker started by PreferenceSpyAddon. Tracking is " + ( preferenceTracker.traceProperty().get() ? "ON" : "OFF" ) );
	}
	
}
