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
package org.jboss.tools.jst.web.tiles.ui.attribute.adapter;

import java.util.*;
import org.jboss.tools.common.model.*;
import org.jboss.tools.common.model.filesystems.FileSystemsHelper;
import org.jboss.tools.common.model.ui.attribute.adapter.DefaultXAttributeListContentProvider;

public class DefinitionListContentProvider extends DefaultXAttributeListContentProvider {
	private XModelObject context;
	
	public void setContext(XModelObject context) {
		this.context = context;
	}

	protected void loadTags() {
		XModelObject file = FileSystemsHelper.getFile(context);
		if(file == null) return;
		XModelObject[] os = file.getChildren("TilesDefinition"); //$NON-NLS-1$
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < os.length; i++) {
			if(os[i] == context) continue;
			list.add(os[i].getAttributeValue("name")); //$NON-NLS-1$
		} 
		tags = list.toArray(new String[0]);
	}

}
