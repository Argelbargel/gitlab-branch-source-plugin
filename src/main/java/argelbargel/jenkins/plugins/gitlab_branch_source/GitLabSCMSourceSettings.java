package argelbargel.jenkins.plugins.gitlab_branch_source;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.AllowMergeRequestsFromForks;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.AllowMergeRequestsFromOrigin;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.FilterWorkInProgress;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.GitLabMergeRequestFilter;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
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
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.defaultGitLabConnectionName;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabConnection;
import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabConnectionNames;


public final class GitLabSCMSourceSettings extends AbstractDescribableImpl<GitLabSCMSourceSettings> {
    private final String connectionName;
    private final GitLabSCMBranchMonitorStrategy branchMonitorStrategy;
    private final GitLabSCMOriginMonitorStrategy originMonitorStrategy;
    private final GitLabSCMForksMonitorStrategy forksMonitorStrategy;
    private final GitLabSCMTagMonitorStrategy tagMonitorStrategy;
    private String credentialsId;
    private boolean updateBuildDescription;
    private boolean publishUnstableBuildsAsSuccess;
    private String mergeCommitMessage;

    GitLabSCMSourceSettings() {
        this(defaultGitLabConnectionName(),
                new GitLabSCMBranchMonitorStrategy(),
                new GitLabSCMOriginMonitorStrategy(),
                new GitLabSCMForksMonitorStrategy(),
                new GitLabSCMTagMonitorStrategy());
    }

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
        this.credentialsId = DescriptorImpl.CHECKOUT_CREDENTIALS_ANONYMOUS;
        this.updateBuildDescription = true;
        this.publishUnstableBuildsAsSuccess = false;
        this.mergeCommitMessage = DescriptorImpl.DEFAULT_MERGE_COMMIT_MESSAGE;
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

    public String getCredentialsId() {
        return !DescriptorImpl.CHECKOUT_CREDENTIALS_ANONYMOUS.equals(credentialsId) ? credentialsId : null;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
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

    GitLabMergeRequestFilter createMergeRequestFilter(TaskListener listener) {
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

    boolean determineMergeRequestStrategyValue(GitLabSCMMergeRequestHead head, boolean originStrategy, boolean forksStrategy) {
        boolean fromOrigin = head.fromOrigin();
        return (fromOrigin && originStrategy) || (!fromOrigin && forksStrategy);
    }


    @SuppressWarnings({"unused", "WeakerAccess"})
    @Extension
    public static class DescriptorImpl extends Descriptor<GitLabSCMSourceSettings> {
        public static final String CHECKOUT_CREDENTIALS_ANONYMOUS = "ANONYMOUS";
        public static final String DEFAULT_MERGE_COMMIT_MESSAGE = "Accepted Merge-Request #{0} after build {1} succeeded";
        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

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
                    gitLabConnectionRequirements(connectionName),
                    GitClient.CREDENTIALS_MATCHER
            );
        }

        private static List<DomainRequirement> gitLabConnectionRequirements(String connectioName) {
            URIRequirementBuilder builder = URIRequirementBuilder.create();

            try {
                URL connectionURL = new URL(gitLabConnection(connectioName).getUrl());
                builder.withHostnamePort(connectionURL.getHost(), connectionURL.getPort());
            } catch (Exception ignored) {
                LOGGER.fine("ignoring invalid gitlab-connection: " + connectioName);
            }

            return builder.build();
        }

    }
}
