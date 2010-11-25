/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.team.ResourceRuleFactory;

/**
 * A resource rule factory for clearcase operations.
 * 
 * @author Gunnar Wagenknecht (gunnar@wagenknecht.org)
 */
class ClearCaseResourceRuleFactory extends ResourceRuleFactory {

	/**
	 * Creates a new instance.
	 */
	ClearCaseResourceRuleFactory() {
		super();
	}

}
