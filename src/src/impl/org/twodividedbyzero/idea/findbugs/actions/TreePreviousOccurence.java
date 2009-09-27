/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.twodividedbyzero.idea.findbugs.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.BugTree;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.82-dev
 */
public class TreePreviousOccurence extends BaseAction {

	private static final Logger LOGGER = Logger.getInstance(TreePreviousOccurence.class.getName());

	private boolean _enabled;
	private AnActionEvent _actionEvent;
	private DataContext _dataContext;


	@Override
	public void actionPerformed(final AnActionEvent e) {
		_actionEvent = e;
		_dataContext = e.getDataContext();

		final Project project = DataKeys.PROJECT.getData(_actionEvent.getDataContext());
		if (project == null) {
			return;
		}

		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}

		final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(getPluginInterface(project).getInternalToolWindowId());
		final Content content = toolWindow.getContentManager().getContent(0);

		if (content != null) {
			final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
			panel.getBugTreePanel().getBugTree().goPreviousOccurence();
		}

	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			_actionEvent = event;
			_dataContext = event.getDataContext();

			final Project project = DataKeys.PROJECT.getData(_dataContext);
			final Presentation presentation = event.getPresentation();

			// check a project is loaded
			if (isProjectLoaded(project, presentation)) {
				return;
			}

			isPluginAccessible(project);

			// check if tool window is registered
			final ToolWindow toolWindow = isToolWindowRegistred(project);
			if (toolWindow == null) {
				presentation.setEnabled(false);
				presentation.setVisible(false);

				return;
			}

			// enable ?
			final Content content = toolWindow.getContentManager().getContent(0);

			if (content != null) {
				final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
				final BugTree tree = panel.getBugTreePanel().getBugTree();
				_enabled = tree.hasPreviousOccurence();
				presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
				presentation.setVisible(true);
			}
		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);// NON-NLS
			if (processed != null) {
				LOGGER.error("Action update failed", processed);
			}
		}
	}


	@Override
	protected boolean isEnabled() {
		return _enabled;
	}


	@Override
	protected boolean setEnabled(final boolean enabled) {
		final boolean was = _enabled;
		if (_enabled != enabled) {
			_enabled = enabled;
		}
		return was;
	}
}