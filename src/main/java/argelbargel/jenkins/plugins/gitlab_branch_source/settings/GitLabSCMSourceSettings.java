package argelbargel.jenkins.plugins.gitlab_branch_source.settings;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.AllowMergeRequestsFromForks;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.AllowMergeRequestsFromOrigin;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.FilterWorkInProgress;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.GitLabMergeRequestFilter;
import argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMMergeRequestHead;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.mixin.TagSCMHead;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

import static argelbargel.jenkins.plugins.gitlab_branch_source.settings.SettingsUtils.CHECKOUT_CREDENTIALS_ANONYMOUS;
import static argelbargel.jenkins.plugins.gitlab_branch_source.settings.SettingsUtils.DEFAULT_MERGE_COMMIT_MESSAGE;
import static argelbargel.jenkins.plugins.gitlab_branch_source.settings.SettingsUtils.defaultGitLabConnectionName;
import static argelbargel.jenkins.plugins.gitlab_branch_source.settings.SettingsUtils.gitLabConnectionNames;


public final class GitLabSCMSourceSettings extends AbstractDescribableImpl<GitLabSCMSourceSettings> {
    private final String connectionName;
    private final GitLabSCMBranchMonitorStrategy branchMonitorStrategy;
    private final GitLabSCMOriginMonitorStrategy originMonitorStrategy;
    private final GitLabSCMForksMonitorStrategy forksMonitorStrategy;
    private final GitLabSCMTagMonitorStrategy tagMonitorStrategy;
    private String checkoutCredentialsId;
    private boolean updateBuildDescription;
    private boolean publishUnstableBuildsAsSuccess;
    private String mergeCommitMessage;


    public GitLabSCMSourceSettings() {
        this(defaultGitLabConnectionName(),
                new GitLabSCMBranchMonitorStrategy(),
                new GitLabSCMOriginMonitorStrategy(),
                new GitLabSCMForksMonitorStrategy(),
                new GitLabSCMTagMonitorStrategy());
    }

    @SuppressWarnings("WeakerAccess")
    @DataBoundConstructor
    public GitLabSCMSourceSettings(String connectionName,
                                   GitLabSCMBranchMonitorStrategy branchMonitorStrategy,
                                   GitLabSCMOriginMonitorStrategy originMonitorStrategy,
                                   GitLabSCMForksMonitorStrategy forksMonitorStrategy,
                                   GitLabSCMTagMonitorStrategy tagMonitorStrategy) {
        this.connectionName = connectionName;
        this.branchMonitorStrategy = branchMonitorStrategy;
        this.originMonitorStrategy = originMonitorStrategy;
        this.forksMonitorStrategy = forksMonitorStrategy;
        this.tagMonitorStrategy = tagMonitorStrategy;
        this.checkoutCredentialsId = CHECKOUT_CREDENTIALS_ANONYMOUS;
        this.updateBuildDescription = true;
        this.publishUnstableBuildsAsSuccess = false;
        this.mergeCommitMessage = DEFAULT_MERGE_COMMIT_MESSAGE;
    }

    @Nonnull
    public String getConnectionName() {
        return connectionName;
    }

    public GitLabSCMBranchMonitorStrategy getBranchMonitorStrategy() {
        return branchMonitorStrategy;
    }

    public GitLabSCMOriginMonitorStrategy getOriginMonitorStrategy() {
        return originMonitorStrategy;
    }

    public GitLabSCMForksMonitorStrategy getForksMonitorStrategy() {
        return forksMonitorStrategy;
    }

    public GitLabSCMTagMonitorStrategy getTagMonitorStrategy() {
        return tagMonitorStrategy;
    }

    public String getCheckoutCredentialsId() {
        return !CHECKOUT_CREDENTIALS_ANONYMOUS.equals(checkoutCredentialsId) ? checkoutCredentialsId : null;
    }

    @DataBoundSetter
    public void setCheckoutCredentialsId(String checkoutCredentialsId) {
        this.checkoutCredentialsId = checkoutCredentialsId;
    }


    @DataBoundSetter
    public void setUpdateBuildDescription(boolean value) {
        updateBuildDescription = value;
    }

    public boolean getUpdateBuildDescription() {
        return updateBuildDescription;
    }

    @DataBoundSetter
    public void setPublishUnstableBuildsAsSuccess(boolean publishUnstableBuildsAsSuccess) {
        this.publishUnstableBuildsAsSuccess = publishUnstableBuildsAsSuccess;
    }

    public boolean getPublishUnstableBuildsAsSuccess() {
        return publishUnstableBuildsAsSuccess;
    }

    public String getMergeCommitMessage() {
        return mergeCommitMessage;
    }

    @DataBoundSetter
    public void setMergeCommitMessage(String value) {
        this.mergeCommitMessage = value;
    }

    @Restricted(NoExternalUse.class)
    public boolean buildUnmerged(GitLabSCMMergeRequestHead head) {
        return determineMergeRequestStrategyValue(head, originMonitorStrategy.getBuildUnmerged(), forksMonitorStrategy.getBuildUnmerged());
    }

    @Restricted(NoExternalUse.class)
    public boolean buildMerged(GitLabSCMMergeRequestHead head) {
        return determineMergeRequestStrategyValue(head, originMonitorStrategy.getBuild(), forksMonitorStrategy.getBuild());

    }

    @Restricted(NoExternalUse.class)
    public BuildStatusPublishMode determineBuildStatusPublishMode(SCMHead head) {
        if (head instanceof GitLabSCMMergeRequestHead) {
            return ((GitLabSCMMergeRequestHead) head).fromOrigin()
                    ? originMonitorStrategy.getBuildStatusPublishMode()
                    : forksMonitorStrategy.getBuildStatusPublishMode();
        } else if (head instanceof TagSCMHead) {
            return tagMonitorStrategy.getBuildStatusPublishMode();
        }

        return branchMonitorStrategy.getBuildStatusPublishMode();
    }


    public GitLabMergeRequestFilter createMergeRequestFilter(TaskListener listener) {
        GitLabMergeRequestFilter filter = GitLabMergeRequestFilter.ALLOW_NONE;
        if (originMonitorStrategy.getMonitored()) {
            GitLabMergeRequestFilter originFilter = new AllowMergeRequestsFromOrigin(listener);
            if (originMonitorStrategy.getIgnoreWorkInProgress()) {
                originFilter = originFilter.and(new FilterWorkInProgress(listener));
            }

            filter = filter.or(originFilter);
        }

        if (forksMonitorStrategy.getMonitored()) {
            GitLabMergeRequestFilter forkFilter = new AllowMergeRequestsFromForks(listener);
            if (forksMonitorStrategy.getIgnoreWorkInProgress()) {
                forkFilter = forkFilter.and(new FilterWorkInProgress(listener));
            }

            filter = filter.or(forkFilter);
        }

        return filter;
    }

    public boolean shouldMonitorMergeRequests() {
        return getOriginMonitorStrategy().getMonitored() || getForksMonitorStrategy().getMonitored();
    }


    public boolean determineMergeRequestStrategyValue(GitLabSCMMergeRequestHead head, boolean originStrategy, boolean forksStrategy) {
        boolean fromOrigin = head.fromOrigin();
        return (fromOrigin && originStrategy) || (!fromOrigin && forksStrategy);
    }


    @SuppressWarnings({"unused", "WeakerAccess"})
    @Extension
    public static class DescriptorImpl extends Descriptor<GitLabSCMSourceSettings> {
        public GitLabSCMSourceSettings getDefaults() {
            return new GitLabSCMSourceSettings();
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckConnectionName(@AncestorInPath SCMSourceOwner context, @QueryParameter String connectionName) {
            return gitLabConnectionNames().contains(connectionName) ? FormValidation.ok() : FormValidation.error(connectionName + " is not a valid GitLab Connection");
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillConnectionNameItems() {
            ListBoxModel items = new ListBoxModel();
            for (String name : gitLabConnectionNames()) {
                items.add(name, name);
            }
            return items;
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillCheckoutCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String connectionName, @QueryParameter String checkoutCredentialsId) {
            if (context == null && !Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER) ||
                    context != null && !context.hasPermission(Item.EXTENDED_READ)) {
                return new StandardListBoxModel().includeCurrentValue(checkoutCredentialsId);
            }

            StandardListBoxModel result = new StandardListBoxModel();
            result.add("- anonymous -", CHECKOUT_CREDENTIALS_ANONYMOUS);
            return result.includeMatchingAs(
                    context instanceof Queue.Task
                            ? Tasks.getDefaultAuthenticationOf((Queue.Task) context)
                            : ACL.SYSTEM,
                    context,
                    StandardUsernameCredentials.class,
                    SettingsUtils.gitLabConnectionRequirements(connectionName),
                    GitClient.CREDENTIALS_MATCHER
            );
        }

    }
}
