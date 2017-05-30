package argelbargel.jenkins.plugins.gitlab_branch_source;


import argelbargel.jenkins.plugins.gitlab_branch_source.actions.GitLabLinkAction;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectSelector;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectVisibility;
import argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHook;
import argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHookListener;
import argelbargel.jenkins.plugins.gitlab_branch_source.settings.GitLabSCMSourceSettings;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMNavigatorEvent;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSourceCategory;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabConnection;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMIcons.ICON_GITLAB;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMIcons.iconFilePathPattern;


@SuppressWarnings({"unused", "WeakerAccess"})
public class GitLabSCMNavigator extends SCMNavigator {
    private static final Logger LOGGER = Logger.getLogger(GitLabSCMNavigator.class.getName());
    private static final String DEFAULT_SEARCH_PATTERN = "";

    private final GitLabSCMSourceSettings sourceSettings;
    private final GitLabSCMWebHookListener hookListener;
    private String projectSearchPattern;
    private String projectSelectorId;
    private String projectVisibilityId;
    private String projectGroup;
    private boolean saved;


    @DataBoundConstructor
    public GitLabSCMNavigator(GitLabSCMSourceSettings sourceSettings) {
        this.sourceSettings = sourceSettings;
        this.hookListener = GitLabSCMWebHook.createListener(this);
        this.projectSearchPattern = DEFAULT_SEARCH_PATTERN;
        this.projectSelectorId = GitLabProjectSelector.VISIBLE.id();
        this.projectVisibilityId = GitLabProjectVisibility.ALL.id();
        this.projectGroup = null;
        this.saved = false;
    }

    public GitLabSCMSourceSettings getSourceSettings() {
        return sourceSettings;
    }

    public String getProjectGroup() {
        return (projectGroup != null) ? projectGroup : "";
    }

    @DataBoundSetter
    public void setProjectGroup(String group) {
        projectGroup = group;
    }

    public String getProjectSearchPattern() {
        return projectSearchPattern;
    }

    @DataBoundSetter
    public void setProjectSearchPattern(String pattern) {
        projectSearchPattern = (pattern != null) ? pattern : DEFAULT_SEARCH_PATTERN;
    }

    public String getProjectSelectorId() {
        return projectSelectorId;
    }

    @DataBoundSetter
    public void setProjectSelectorId(String id) {
        projectSelectorId = id;
    }

    public String getProjectVisibilityId() {
        return projectVisibilityId;
    }

    @DataBoundSetter
    public void setProjectVisibilityId(String id) {
        projectVisibilityId = id;
    }

    public String getHookUrl() {
        return hookListener.url().toString();
    }

    public boolean getListenToWebHooks() {
        return hookListener.getListen();
    }

    public void setListenToWebHooks(boolean value) {
        hookListener.setListen(value);
    }

    public boolean getRegisterWebHooks() {
        return hookListener.getRegister();
    }

    public void setRegisterWebHooks(boolean value) {
        hookListener.setRegister(value);
    }

    @Nonnull
    @Override
    protected List<Action> retrieveActions(@Nonnull SCMNavigatorOwner owner, @CheckForNull SCMNavigatorEvent event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        return Collections.<Action>singletonList(GitLabLinkAction.create(getPronoun(), ICON_GITLAB, gitLabConnection(getSourceSettings().getConnectionName()).getUrl()));
    }

    @Override
    public void visitSources(@Nonnull SCMSourceObserver observer) throws IOException, InterruptedException {
        LOGGER.info("visiting sources for context " + observer.getContext().getFullName() + "...");
        createVisitor(observer).visitSources();
    }

    @Override
    public void visitSource(@Nonnull String sourceName, @Nonnull SCMSourceObserver observer) throws IOException, InterruptedException {
        LOGGER.info("visiting " + sourceName + " for context " + observer.getContext().getFullName());
        createVisitor(observer).visitProject(sourceName);
    }

    @Override
    public void afterSave(@Nonnull SCMNavigatorOwner owner) {
        if (getListenToWebHooks()) {
            GitLabSCMWebHook.get().addListener(this, owner);
            saved = true;
        } else {
            GitLabSCMWebHook.get().removeListener(this, owner);
        }
    }

    @Restricted(NoExternalUse.class)
    public boolean saved() {
        return saved;
    }

    private SourceVisitor createVisitor(@Nonnull SCMSourceObserver observer) {
        return new SourceVisitor(this, observer);
    }

    @Nonnull
    @Override
    protected String id() {
        return getSourceSettings().getConnectionName();
    }

    public GitLabSCMWebHookListener getHookListener() {
        return hookListener;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    @Extension
    public static class Descriptor extends SCMNavigatorDescriptor implements IconSpec {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.GitLabSCMNavigator_DisplayName();
        }

        @Override
        public String getPronoun() {
            return Messages.GitLabSCMNavigator_Pronoun();
        }

        @Nonnull
        @Override
        public String getDescription() {
            return Messages.GitLabSCMNavigator_Description();
        }

        @Override
        public String getIconClassName() {
            return ICON_GITLAB;
        }

        @Override
        public String getIconFilePathPattern() {
            return iconFilePathPattern(getIconClassName());
        }

        @Override
        public SCMNavigator newInstance(String name) {
            return new GitLabSCMNavigator(new GitLabSCMSourceSettings());
        }

        @Nonnull
        @Override
        protected SCMSourceCategory[] createCategories() {
            return new SCMSourceCategory[]{
                    new UncategorizedSCMSourceCategory(Messages._GitLabSCMNavigator_UncategorizedCategory())
            };
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillProjectSelectorIdItems() {
            ListBoxModel items = new ListBoxModel();
            for (String id : GitLabProjectSelector.ids()) {
                items.add(id, id);
            }
            return items;
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillProjectVisibilityIdItems() {
            ListBoxModel items = new ListBoxModel();
            for (String id : GitLabProjectVisibility.ids()) {
                items.add(id, id);
            }
            return items;
        }
    }
}
