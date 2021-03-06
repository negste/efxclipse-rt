/*******************************************************************************
 * Copyright (c) 2018 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adrodoc55<adrodoc55@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.ui.controls.tabpane;

/**
 * The drop type
 * 
 * @since 3.5.0
 */
public enum DropType {
	/**
	 * No dropping
	 */
	NONE,
	/**
	 * Dropped before a reference tab
	 */
	BEFORE,
	/**
	 * Dropped after a reference tab
	 */
	AFTER,
	/**
	 * Dropped in an area to detach
	 */
	DETACH
}