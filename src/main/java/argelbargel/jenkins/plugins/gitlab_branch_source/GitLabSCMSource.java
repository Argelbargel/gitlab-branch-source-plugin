package argelbargel.jenkins.plugins.gitlab_branch_source;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProject;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.GitLabMergeRequestFilter;
import argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHook;
import argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHookListener;
import hudson.Extension;
import hudson.RelativePath;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.browser.GitLab;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.plugins.git.extensions.impl.BuildChooserSetting;
import hudson.scm.SCM;
import hudson.util.ListBoxModel;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import org.eclipse.jgit.transport.RefSpec;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabAPI;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMIcons.ICON_GITLAB;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMRefSpec.BRANCHES;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMRefSpec.MERGE_REQUESTS;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMRefSpec.TAGS;
import static argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectSelector.VISIBLE;
import static argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectVisibility.ALL;


@SuppressWarnings({"unused", "WeakerAccess"})
public class GitLabSCMSource extends AbstractGitSCMSource {
    private static final Logger LOGGER = Logger.getLogger(GitLabSCMSource.class.getName());

    private final GitLabSCMSourceSettings sourceSettings;
    private final GitLabProject project;
    private final GitLabSCMWebHookListener hookListener;
    private final SourceHeads heads;
    private final SourceActions actions;

    @DataBoundConstructor
    public GitLabSCMSource(@Nonnull GitLabSCMSourceSettings sourceSettings, String projectPath) throws GitLabAPIException {
        this(sourceSettings, gitLabAPI(sourceSettings.getConnectionName()).getProject(projectPath));
    }

    GitLabSCMSource(@Nonnull GitLabSCMSourceSettings sourceSettings, GitLabProject project) {
        super(null);
        this.sourceSettings = sourceSettings;
        this.project = project;
        this.hookListener = GitLabSCMWebHook.createListener(this);
        this.actions = new SourceActions(this);
        this.heads = new SourceHeads(this);
    }

    public GitLabSCMSourceSettings getSourceSettings() {
        return sourceSettings;
    }

    @Override
    public String getCredentialsId() {
        return sourceSettings.getCredentialsId();
    }

    @Override
    public final String getRemote() {
        return project.getRemote(this);
    }

    @Override
    public String getIncludes() {
        return sourceSettings.getBranchMonitorStrategy().getIncludes();
    }

    @Override
    public String getExcludes() {
        return sourceSettings.getBranchMonitorStrategy().getExcludes();
    }

    GitLabProject getProject() {
        return project;
    }

    public int getProjectId() {
        return (project != null) ? project.getId() : -1;
    }

    public String getProjectPath() {
        return (project != null) ? project.getPathWithNamespace() : "";
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

    public GitLabSCMWebHookListener getHookListener() {
        return hookListener;
    }

    @Override
    public GitRepositoryBrowser getBrowser() {
        try {
            return new GitLab(project.getWebUrl(), getGitLabVersion());
        } catch (GitLabAPIException e) {
            LOGGER.warning("could not determine gitlab-version:" + e.getMessage());
            return super.getBrowser();
        }
    }

    public boolean buildMerged(GitLabSCMMergeRequestHead head) {
        return sourceSettings.determineMergeRequestStrategyValue(head, sourceSettings.getOriginMonitorStrategy().getBuild(), sourceSettings.getForksMonitorStrategy().getBuild());

    }

    public boolean buildUnmerged(GitLabSCMMergeRequestHead head) {
        return sourceSettings.determineMergeRequestStrategyValue(head, sourceSettings.getOriginMonitorStrategy().getBuildUnmerged(), sourceSettings.getForksMonitorStrategy().getBuildUnmerged());
    }

    public String getMergeCommitMessage() {
        return sourceSettings.getMergeCommitMessage();
    }


    final String getGitLabVersion() throws GitLabAPIException {
        return gitLabAPI(getSourceSettings().getConnectionName()).getVersion().toString();
    }

    @Override
    protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @CheckForNull SCMHeadEvent<?> event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        listener.getLogger().format(Messages.GitLabSCMSource_retrievingHeadsForProject(project.getPathWithNamespace()) + "\n");
        heads.retrieve(criteria, observer, event, listener);
    }

    @Override
    @CheckForNull
    protected SCMRevision retrieve(@Nonnull SCMHead head, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        return heads.retrieve(head, listener);
    }

    @Nonnull
    @Override
    protected List<Action> retrieveActions(@CheckForNull SCMSourceEvent event, @Nonnull TaskListener listener) throws IOException {
        return actions.retrieve(event, listener);
    }

    @Nonnull
    @Override
    protected List<Action> retrieveActions(@Nonnull SCMHead head, @CheckForNull SCMHeadEvent event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        return actions.retrieve(head, event, listener);
    }

    @Nonnull
    @Override
    protected List<Action> retrieveActions(@Nonnull SCMRevision revision, @CheckForNull SCMHeadEvent event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        return actions.retrieve(revision, event, listener);
    }

    @Override
    protected List<RefSpec> getRefSpecs() {
        List<RefSpec> refSpecs = new LinkedList<>();

        if (sourceSettings.getBranchMonitorStrategy().getMonitored()) {
            refSpecs.add(BRANCHES.delegate());
        }
        if (sourceSettings.getTagMonitorStrategy().getMonitored()) {
            refSpecs.add(TAGS.delegate());
        }
        if (sourceSettings.getOriginMonitorStrategy().getMonitored() || sourceSettings.getForksMonitorStrategy().getMonitored()) {
            refSpecs.add(MERGE_REQUESTS.delegate());
        }

        return refSpecs;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    protected boolean isCategoryEnabled(@Nonnull SCMHeadCategory category) {
        if (!super.isCategoryEnabled(category)) {
            return false;
        }

        if (category instanceof ChangeRequestSCMHeadCategory) {
            return getSourceSettings().getOriginMonitorStrategy().getMonitored() || getSourceSettings().getForksMonitorStrategy().getMonitored();
        }

        if (category instanceof TagSCMHeadCategory) {
            return getSourceSettings().getTagMonitorStrategy().getMonitored();
        }

        return true;
    }

    String getDescription() {
        return project.getDescription();
    }

    @Nonnull
    @Override
    public SCM build(@Nonnull SCMHead head, @CheckForNull SCMRevision revision) {
        GitSCM scm;

        if (head instanceof GitLabSCMHead) {
            scm = ((GitLabSCMHead) head).createSCM(this);
            if (revision instanceof SCMRevisionImpl) {
                scm.getExtensions().add(new BuildChooserSetting(new SpecificRevisionBuildChooser((SCMRevisionImpl) revision)));
            }
        } else {
            scm = (GitSCM) super.build(head, revision);
            scm.setBrowser(getBrowser());
        }

        return scm;
    }

    GitLabMergeRequestFilter createMergeRequestFilter(TaskListener listener) {
        return sourceSettings.createMergeRequestFilter(listener);
    }

    boolean determineMergeRequestStrategyValue(GitLabSCMMergeRequestHead head, boolean originValue, boolean forksValue) {
        return sourceSettings.determineMergeRequestStrategyValue(head, originValue, forksValue);
    }

    @Override
    protected boolean isExcluded(String branchName) {
        return super.isExcluded(branchName);
    }

    @Nonnull
    @Override
    protected SCMProbe createProbe(@Nonnull SCMHead head, @CheckForNull SCMRevision revision) {
        return GitLabSCMProbe.create(this, head, revision);
    }


    @Extension
    public static class DescriptorImpl extends SCMSourceDescriptor implements IconSpec {
        @Nonnull
        public String getDisplayName() {
            return Messages.GitLabSCMSource_DisplayName();
        }

        @Override
        public String getPronoun() {
            return Messages.GitLabSCMSource_Pronoun();
        }

        @Override
        public String getIconClassName() {
            return ICON_GITLAB;
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillProjectPathItems(@QueryParameter @RelativePath("sourceSettings") String connectionName) throws GitLabAPIException {
            ListBoxModel items = new ListBoxModel();
            // TODO: respect settings in nearest GitLabSCMNavigator
            for (GitLabProject project : gitLabAPI(connectionName).findProjects(VISIBLE, ALL, "")) {
                items.add(project.getPathWithNamespace());
            }
            return items;
        }

        @Nonnull
        @Override
        protected SCMHeadCategory[] createCategories() {
            return GitLabSCMHeadCategory.ALL;
        }
    }
}
