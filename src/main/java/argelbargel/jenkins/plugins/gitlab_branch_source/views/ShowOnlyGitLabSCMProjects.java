package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMBranchHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMMergeRequestHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMTagHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.Messages;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.views.ViewJobFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.ADD;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.REMOVE;


public class ShowOnlyGitLabSCMProjects extends AbstractGitLabSCMViewJobFilter {
    private boolean showBranches = true;
    private boolean showMergeRequests = true;
    private boolean showTags = true;

    @DataBoundConstructor
    public ShowOnlyGitLabSCMProjects() {
        this(DEFAULT_FINDER);
    }

    ShowOnlyGitLabSCMProjects(GitLabSCMHeadFinder finder) {
        super(finder);
    }

    @Override
    protected FilterMode filter(Item item, GitLabSCMHead head) {
        if (head == null) {
            return REMOVE;
        }

        if (head instanceof GitLabSCMTagHead && !showTags) {
            return REMOVE;
        }

        if (head instanceof GitLabSCMMergeRequestHead && !showMergeRequests) {
            return REMOVE;
        }

        if (head instanceof GitLabSCMBranchHead && ! showBranches) {
            return REMOVE;
        }

        return ADD;
    }

    public boolean getShowBranches() {
        return showBranches;
    }

    @DataBoundSetter
    public void setShowBranches(boolean showBranches) {
        this.showBranches = showBranches;
    }

    public boolean getShowMergeRequests() {
        return showMergeRequests;
    }

    @DataBoundSetter
    public void setShowMergeRequests(boolean showMergeRequests) {
        this.showMergeRequests = showMergeRequests;
    }

    public boolean getShowTags() {
        return showTags;
    }

    @DataBoundSetter
    public void setShowTags(boolean showTags) {
        this.showTags = showTags;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<ViewJobFilter> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ViewJobFilter_ShowOnlyProjects_DisplayName();
        }
    }
}
