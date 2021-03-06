/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.jst.web.tld.model;

import org.jboss.tools.jst.web.model.*;

public class FileTLDImpl extends SimpleWebFileImpl {
	private static final long serialVersionUID = 1L;

	protected boolean shouldLoadAttributeSeparately(String xmlname) {
    	if("uri.#text".equals(xmlname)) return true; //$NON-NLS-1$
    	return false;
    }
    
}

