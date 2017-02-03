package argelbargel.jenkins.plugins.gitlab_branch_source;

import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import argelbargel.jenkins.plugins.gitlab_branch_source.events.GitLabSCMHeadEvent;
import argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHook;
import argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHookListener;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.browser.GitLab;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.api.SCMSourceOwner;
import org.eclipse.jgit.transport.RefSpec;
import org.gitlab.api.models.GitlabProject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabAPI;
import static argelbargel.jenkins.plugins.gitlab_branch_source.SourceHeads.ORIGIN_REF_BRANCHES;
import static argelbargel.jenkins.plugins.gitlab_branch_source.SourceHeads.ORIGIN_REF_MERGE_REQUESTS;
import static argelbargel.jenkins.plugins.gitlab_branch_source.SourceHeads.ORIGIN_REF_TAGS;
import static jenkins.scm.api.SCMEvent.Type.REMOVED;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GitLabSCMSource extends AbstractGitSCMSource {
    private static final RefSpec REFSPEC_BRANCHES = new RefSpec("+" + ORIGIN_REF_BRANCHES + "*:refs/remotes/origin/*");
    private static final RefSpec REFSPEC_TAGS = new RefSpec("+" + ORIGIN_REF_TAGS + "*:refs/remotes/origin/tags/*");
    private static final RefSpec REFSPEC_MERGE_REQUESTS = new RefSpec("+" + ORIGIN_REF_MERGE_REQUESTS + "*/head:refs/remotes/origin/merge-requests/*");
    private static final Logger LOGGER = Logger.getLogger(GitLabSCMSource.class.getName());

    private final SourceSettings settings;
    private final GitlabProject project;
    private final GitLabSCMWebHookListener hookListener;
    private final SourceHeads heads;
    private final SourceActions actions;


    GitLabSCMSource(GitlabProject project, SourceSettings settings) {
        super(project.getPathWithNamespace());
        this.settings = settings;
        this.project = project;
        this.hookListener = GitLabSCMWebHook.createListener(this);
        this.actions = new SourceActions(project, settings);
        this.heads = new SourceHeads(project, settings);
    }

    public String getConnectionName() {
        return settings.getConnectionName();
    }

    @Override
    public String getCredentialsId() {
        return settings.getCredentialsId();
    }

    @Override
    public String getRemote() {
        if (getCredentialsId() != null && getCredentials(StandardCredentials.class) instanceof SSHUserPrivateKey) {
            return project.getSshUrl();
        } else {
            return project.getHttpUrl();
        }
    }

    @Override
    public String getIncludes() {
        return settings.getIncludes();
    }

    @Override
    public String getExcludes() {
        return settings.getExcludes();
    }

    public boolean getMonitorAndBuildBranches() {
        return settings.branchMonitorStrategy().monitored();
    }

    public boolean getBuildBranchesWithMergeRequests() {
        return settings.originMonitorStrategy().monitored() && settings.getBuildBranchesWithMergeRequests();
    }

    public boolean getMonitorAndBuildMergeRequestsFromOrigin() {
        return settings.originMonitorStrategy().monitored();
    }

    public boolean getBuildMergeRequestsFromOriginMerged() {
        return settings.originMonitorStrategy().buildMerged();
    }

    public boolean getBuildMergeRequestsFromOriginUnmerged() {
        return settings.originMonitorStrategy().buildUnmerged();
    }

    public boolean getIgnoreWorkInProgressFromOrigin() {
        return settings.originMonitorStrategy().ignoreWorkInProgress();
    }

    public boolean getMonitorAndBuildMergeRequestsFromForks() {
        return settings.forksMonitorStrategy().monitored();
    }

    public boolean getBuildMergeRequestsFromForksMerged() {
        return settings.forksMonitorStrategy().buildMerged();
    }

    public boolean getBuildMergeRequestsFromForksUnmerged() {
        return settings.forksMonitorStrategy().buildUnmerged();
    }

    public boolean getIgnoreWorkInProgressFromForks() {
        return settings.forksMonitorStrategy().ignoreWorkInProgress();
    }

    public boolean getMonitorTags() {
        return settings.tagMonitorStrategy().monitored();
    }

    public boolean getBuildTags() {
        return settings.tagMonitorStrategy().buildUnmerged();
    }

    public boolean getRegisterWebHooks() {
        return settings.getRegisterWebHooks();
    }

    public boolean getUpdateBuildDescription() {
        return settings.getUpdateBuildDescription();
    }

    public int getProjectId() {
        return project.getId();
    }

    public String getHookUrl() {
        return hookListener.url().toString();
    }

    public GitLabSCMWebHookListener getHookListener() {
        return hookListener;
    }

    @Override
    public GitRepositoryBrowser getBrowser() {
        try {
            return new GitLab(project.getWebUrl(), gitLabAPI(getConnectionName()).getVersion().toString());
        } catch (GitLabAPIException e) {
            LOGGER.warning("could not determine gitlab-version:" + e.getMessage());
            return super.getBrowser();
        }
    }

    @Override
    protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @CheckForNull SCMHeadEvent<?> event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        if (event instanceof GitLabSCMHeadEvent) {
            retrieve(observer, (GitLabSCMHeadEvent) event, listener);
        } else {
            heads.retrieve(criteria, observer, listener);
        }
    }

    @Nonnull
    @Override
    protected  List<Action> retrieveActions(@CheckForNull SCMSourceEvent event, @Nonnull TaskListener listener) throws IOException {
        return actions.retrieve(event, listener);
    }

    private void retrieve(SCMHeadObserver observer, GitLabSCMHeadEvent event, TaskListener listener) throws IOException, InterruptedException {
        listener.getLogger().format(Messages.GitLabSCMSource_headFromEvent(event.getPayload().getObjectKind()) + "\n");
        if (event.getType() != REMOVED) {
            // for events emitted by us we trust the event's heads
            for (Map.Entry<SCMHead, SCMRevision> entry : event.heads(this).entrySet()) {
                listener.getLogger().format(Messages.GitLabSCMSource_createdOrUpdatedHead(entry.getKey().getName(), entry.getValue()) + "\n");
                observer.observe(entry.getKey(), entry.getValue());
            }
        } else {
            listener.getLogger().format(Messages.GitLabSCMSource_removedHead(event.getPayload().getRef()) + "\n");
        }
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
        if (settings.branchMonitorStrategy().monitored()) {
            refSpecs.add(REFSPEC_BRANCHES);
        }

        if (settings.tagMonitorStrategy().monitored()) {
            refSpecs.add(REFSPEC_TAGS);
        }

        if (settings.originMonitorStrategy().monitored() || settings.forksMonitorStrategy().monitored()) {
            refSpecs.add(REFSPEC_MERGE_REQUESTS);
        }
        return refSpecs;
    }


    @Override
    public void afterSave() {
        LOGGER.info("auto-registering system-hook for source " + getId() + "...");
        GitLabSCMWebHook.get().addListener(this);
    }

    @Nonnull
    @Override
    public SCM build(@Nonnull SCMHead head, @CheckForNull SCMRevision revision) {
        if (head instanceof SCMHeadLabel) {
            return build(((SCMHeadLabel) head).getTarget(), revision);
        }

        if (head instanceof SCMMergeRequestHead) {
            GitLabSCMHead source = ((SCMMergeRequestHead) head).getSource();
            return build(source, source.getRevision());
        }

        GitSCM scm = (GitSCM) super.build(head, revision);
        scm.setBrowser(getBrowser());
        return scm;
    }


    public GitLabSCMHead createBranch(String name) throws IOException, InterruptedException {
        return heads.createBranch(name);
    }

    public GitLabSCMHead createBranch(String name, String hash) throws IOException, InterruptedException {
        return heads.createBranch(name, hash);
    }

    public GitLabSCMHead createTag(String name, String hash) {
        return heads.createTag(name, hash);
    }

    private <T extends StandardCredentials> T getCredentials(@Nonnull Class<T> type) {
        return CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(
                type, getOwner(), ACL.SYSTEM,
                Collections.<DomainRequirement>emptyList()), CredentialsMatchers.allOf(
                CredentialsMatchers.withId(getCredentialsId()),
                CredentialsMatchers.instanceOf(type)));
    }

    @SuppressWarnings("unused")
    @Extension
    public static class DescriptorImpl extends SCMSourceDescriptor {
        @Nonnull
        public String getDisplayName() {
            return Messages.GitLabSCMSource_DisplayName();
        }

        @Override
        public String getPronoun() {
            return Messages.GitLabSCMSource_Pronoun();
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckConnectionName(@AncestorInPath SCMSourceOwner context, @QueryParameter String connectionName) {
            return DescriptorHelper.doCheckConnectionName(connectionName);
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckIncludes(@QueryParameter String includes) {
            return DescriptorHelper.doCheckIncludes(includes);
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillConnectionNameItems() {
            return DescriptorHelper.doFillConnectionNameItems();
        }

        public ListBoxModel doFillProjectPathItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String connectionName) {
            return DescriptorHelper.doFillProjectPathItems(connectionName);
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String connectionName, @QueryParameter String checkoutCredentialsId) {
            return DescriptorHelper.doFillCheckoutCredentialsIdItems(context, connectionName, checkoutCredentialsId);
        }
    }
}