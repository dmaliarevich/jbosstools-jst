/*******************************************************************************
 * Copyright (c) 2007-2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jst.web.ui.wizards.newfile;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.html.ui.internal.editor.IHelpContextIds;
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;
import org.jboss.tools.jst.web.model.handlers.CreateJSPFileSupport;
import org.jboss.tools.jst.web.ui.Messages;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.editor.pref.template.TemplateContextTypeIdsXHTML;

/**
 * Copied from NewHTMLTemplatesWizardPage, because the class is internal and not provide possibility 
 * to inherit from this
 * 
 * 
 * @author mareshkau
 *
 */
public class NewXHTMLTemplatesWizardPage extends WizardPage {
	/**
	 * Content provider for templates
	 */
	private class TemplateContentProvider implements IStructuredContentProvider {
		/** The template store. */
		private TemplateStore fStore;

		/*
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
			this.fStore = null;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object input) {
			return this.fStore.getTemplates(TemplateContextTypeIdsXHTML.NEW);
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.fStore = (TemplateStore) newInput;
		}
	}

	/**
	 * Label provider for templates.
	 */
	private class TemplateLabelProvider extends LabelProvider implements ITableLabelProvider {

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			Template template = (Template) element;

			switch (columnIndex) {
				case 0 :
					return template.getName();
				case 1 :
					return template.getDescription();
				default :
					return ""; //$NON-NLS-1$
			}
		}
	}

	/** Last selected template name */
	private String fLastSelectedTemplateName;
	/** The viewer displays the pattern of selected template. */
	private SourceViewer fPatternViewer;
	/** The table presenting the templates. */
	private TableViewer fTableViewer;
	/** Template store used by this wizard page */
	private TemplateStore fTemplateStore;
	/** Checkbox for using templates. */
	private Button fUseTemplateButton;
	
	private CreateJSPFileSupport fJspFileSupport;

	public NewXHTMLTemplatesWizardPage(CreateJSPFileSupport jspFileSupport) {
		super("NewXHTMLTemplatesWizardPage", Messages.NewXHTMLTemplatesWizardPage_0, null); //$NON-NLS-1$
		this.fJspFileSupport = jspFileSupport;
		setDescription(Messages.NewXHTMLTemplatesWizardPage_0);
	}

	/**
	 * Correctly resizes the table so no phantom columns appear
	 * 
	 * @param parent
	 *            the parent control
	 * @param buttons
	 *            the buttons
	 * @param table
	 *            the table
	 * @param column1
	 *            the first column
	 * @param column2
	 *            the second column
	 * @param column3
	 *            the third column
	 */
	private void configureTableResizing(final Composite parent, final Table table, final TableColumn column1, final TableColumn column2) {
		parent.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area = parent.getClientArea();
				Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width - 2 * table.getBorderWidth();
				if (preferredSize.y > area.height) {
					// Subtract the scrollbar width from the total column
					// width
					// if a vertical scrollbar will be required
					Point vBarSize = table.getVerticalBar().getSize();
					width -= vBarSize.x;
				}

				Point oldSize = table.getSize();
				if (oldSize.x > width) {
					// table is getting smaller so make the columns
					// smaller first and then resize the table to
					// match the client area width
					column1.setWidth(width / 2);
					column2.setWidth(width / 2);
					table.setSize(width, area.height);
				}
				else {
					// table is getting bigger so make the table
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(width, area.height);
					column1.setWidth(width / 2);
					column2.setWidth(width / 2);
				}
			}
		});
	}

	public void createControl(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		// create checkbox for user to use HTML Template
		fUseTemplateButton = new Button(parent, SWT.CHECK);
		fUseTemplateButton.setText(Messages.NewXHTMLTemplatesWizardPage_4);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		fUseTemplateButton.setLayoutData(data);
		fUseTemplateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableTemplates();
			}
		});

		// create composite for Templates table
		Composite innerParent = new Composite(parent, SWT.NONE);
		GridLayout innerLayout = new GridLayout();
		innerLayout.numColumns = 2;
		innerLayout.marginHeight = 0;
		innerLayout.marginWidth = 0;
		innerParent.setLayout(innerLayout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		innerParent.setLayoutData(gd);

		Label label = new Label(innerParent, SWT.NONE);
		label.setText(Messages.NewXHTMLTemplatesWizardPage_7);
		data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		label.setLayoutData(data);

		// create table that displays templates
		Table table = new Table(innerParent, SWT.BORDER | SWT.FULL_SELECTION);

		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(2);
		data.heightHint = convertHeightInCharsToPixels(10);
		data.horizontalSpan = 2;
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(Messages.NewXHTMLTemplatesWizardPage_2);

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(Messages.NewXHTMLTemplatesWizardPage_3);

		fTableViewer = new TableViewer(table);
		fTableViewer.setLabelProvider(new TemplateLabelProvider());
		fTableViewer.setContentProvider(new TemplateContentProvider());

		fTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object object1, Object object2) {
				if ((object1 instanceof Template) && (object2 instanceof Template)) {
					Template left = (Template) object1;
					Template right = (Template) object2;
					int result = left.getName().compareToIgnoreCase(right.getName());
					if (result != 0)
						return result;
					return left.getDescription().compareToIgnoreCase(right.getDescription());
				}
				return super.compare(viewer, object1, object2);
			}

			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});

		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				updateViewerInput();
			}
		});

		// create viewer that displays currently selected template's contents
		fPatternViewer = doCreateViewer(parent);

		fTemplateStore = WebUiPlugin.getDefault().getTemplateStore();
		fTableViewer.setInput(fTemplateStore);

		// Create linked text to just to templates preference page
		Link link = new Link(parent, SWT.NONE);
		link.setText(Messages.NewXHTMLTemplatesWizardPage_6);
		data = new GridData(SWT.END, SWT.FILL, true, false, 2, 1);
		link.setLayoutData(data);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				linkClicked();
			}
		});

		configureTableResizing(innerParent, table, column1, column2);
		loadLastSavedPreferences();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IHelpContextIds.HTML_NEWWIZARD_TEMPLATE_HELPID);
		Dialog.applyDialogFont(parent);
		setControl(parent);
	}

	/**
	 * Creates, configures and returns a source viewer to present the template
	 * pattern on the preference page. Clients may override to provide a
	 * custom source viewer featuring e.g. syntax coloring.
	 * 
	 * @param parent
	 *            the parent control
	 * @return a configured source viewer
	 */
	private SourceViewer createViewer(Composite parent) {
		SourceViewerConfiguration sourceViewerConfiguration = new StructuredTextViewerConfiguration() {
			StructuredTextViewerConfiguration baseConfiguration = new StructuredTextViewerConfigurationHTML();

			public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
				return baseConfiguration.getConfiguredContentTypes(sourceViewer);
			}

			public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
				return baseConfiguration.getLineStyleProviders(sourceViewer, partitionType);
			}
		};
		SourceViewer viewer = new StructuredTextViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		((StructuredTextViewer) viewer).getTextWidget().setFont(JFaceResources.getFont("org.eclipse.wst.sse.ui.textfont")); //$NON-NLS-1$
		IStructuredModel scratchModel = StructuredModelManager.getModelManager().createUnManagedStructuredModelFor("org.eclipse.wst.html.core.htmlsource");
		IDocument document = scratchModel.getStructuredDocument();
		viewer.configure(sourceViewerConfiguration);
		viewer.setDocument(document);
		return viewer;
	}

	private SourceViewer doCreateViewer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.NewXHTMLTemplatesWizardPage_5);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		SourceViewer viewer = createViewer(parent);
		viewer.setEditable(false);

		Control control = viewer.getControl();
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.heightHint = convertHeightInCharsToPixels(5);
		// [261274] - source viewer was growing to fit the max line width of the template
		data.widthHint = convertWidthInCharsToPixels(2);
		control.setLayoutData(data);

		return viewer;
	}

	/**
	 * Enable/disable controls in page based on fUseTemplateButton's current
	 * state.
	 */
	void enableTemplates() {
		boolean enabled = fUseTemplateButton.getSelection();

		if (!enabled) {
			// save last selected template
			Template template = getSelectedTemplate();
			if (template != null)
				fLastSelectedTemplateName = template.getName();
			else
				fLastSelectedTemplateName = ""; //$NON-NLS-1$

			fTableViewer.setSelection(null);
			updateViewerInput();
		}
		else {
			setSelectedTemplate(fLastSelectedTemplateName);
		}

		fTableViewer.getControl().setEnabled(enabled);
		fPatternViewer.getControl().setEnabled(enabled);
	}

	/**
	 * Return the template preference page id
	 * 
	 * @return
	 */
	private String getPreferencePageId() {
		return "org.jboss.tools.jsf.ui.editor.pref.template"; //$NON-NLS-1$
	}

	/**
	 * Get the currently selected template.
	 * 
	 * @return
	 */
	private Template getSelectedTemplate() {
		Template template = null;
		IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();

		if (selection.size() == 1) {
			template = (Template) selection.getFirstElement();
		}
		return template;
	}

	/**
	 * Returns template string to insert.
	 * 
	 * @return String to insert or null if none is to be inserted
	 */
	String getTemplateString() {
		String templateString = null;

		Template template = getSelectedTemplate();
		if (template != null) {
			TemplateContextType contextType = WebUiPlugin.getDefault().getTemplateContextRegistry().getContextType(TemplateContextTypeIdsXHTML.NEW);
			IDocument document = new Document();
			TemplateContext context = new DocumentTemplateContext(contextType, document, 0, 0);
			try {
				TemplateBuffer buffer = context.evaluate(template);
				templateString = buffer.getString();
			}
			catch (Exception e) {
				WebUiPlugin.getDefault().logWarning("Could not create template for new html", e); //$NON-NLS-1$
			}
		}

		return templateString;
	}

	void linkClicked() {
		String pageId = getPreferencePageId();
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), pageId, new String[]{pageId}, null);
		dialog.open();
		fTableViewer.refresh();
	}

	/**
	 * Load the last template name used in New HTML File wizard.
	 */
	private void loadLastSavedPreferences() {
		String templateName = WebUiPlugin.getDefault().getPreferenceStore().getString(HTMLUIPreferenceNames.NEW_FILE_TEMPLATE_NAME);
		if (templateName == null || templateName.length() == 0) {
			fLastSelectedTemplateName = ""; //$NON-NLS-1$
			fUseTemplateButton.setSelection(false);
		}
		else {
			fLastSelectedTemplateName = templateName;
			fUseTemplateButton.setSelection(true);
		}
		enableTemplates();
	}

	/**
	 * Save template name used for next call to New HTML File wizard.
	 */
	void saveLastSavedPreferences() {
		String templateName = ""; //$NON-NLS-1$

		Template template = getSelectedTemplate();
		if (template != null) {
			templateName = template.getName();
		}

		WebUiPlugin.getDefault().getPreferenceStore().setValue(HTMLUIPreferenceNames.NEW_FILE_TEMPLATE_NAME, templateName);
		WebUiPlugin.getDefault().savePluginPreferences();
	}

	/**
	 * Select a template in the table viewer given the template name. If
	 * template name cannot be found or templateName is null, just select
	 * first item in table. If no items in table select nothing.
	 * 
	 * @param templateName
	 */
	private void setSelectedTemplate(String templateName) {
		Object template = null;

		if (templateName != null && templateName.length() > 0) {
			// pick the last used template
			template = fTemplateStore.findTemplate(templateName, TemplateContextTypeIdsXHTML.NEW);
		}

		// no record of last used template so just pick first element
		if (template == null) {
			// just pick first element
			template = fTableViewer.getElementAt(0);
		}

		if (template != null) {
			IStructuredSelection selection = new StructuredSelection(template);
			fTableViewer.setSelection(selection, true);
		}
	}

	/**
	 * Updates the pattern viewer.
	 */
	void updateViewerInput() {
		String pattern = ""; //$NON-NLS-1$
		Template template = getSelectedTemplate();
		if (template != null) {
			pattern = template.getPattern();
		}
		fJspFileSupport.setTaglibsFromTemplateBody(pattern);
		fPatternViewer.getDocument().set(pattern);
	}
}
