package argelbargel.jenkins.plugins.gitlab_branch_source.heads;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMSource;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.SCMHeadMixin;

import javax.annotation.Nonnull;


public abstract class GitLabSCMHead extends SCMHead implements SCMHeadMixin {
    public static final String REVISION_HEAD = "HEAD";

    public static GitLabSCMBranchHead createBranch(int projectId, String name, String hash) {
        return createBranch(projectId, name, hash, false);
    }

    public static GitLabSCMTagHead createTag(int projectId, String name, String hash, long timestamp) {
        return new GitLabSCMTagHead(projectId, name, hash, timestamp);
    }

    public static GitLabSCMMergeRequestHead createMergeRequest(int id, String name, int iid, GitLabSCMHead source, GitLabSCMBranchHead target) {
        return createMergeRequest(id, name, iid, source, target, false);
    }

    public static GitLabSCMMergeRequestHead createMergeRequest(int id, String description, int iid, GitLabSCMHead source, GitLabSCMBranchHead target, boolean mergeable) {
        return new GitLabSCMMergeRequestHead(id, "MR-" + iid, description, source, target, mergeable);
    }

    public static GitLabSCMBranchHead createBranch(int projectId, String name, String hash, boolean hasMergeRequest) {
        return new GitLabSCMBranchHead(projectId, name, hash, hasMergeRequest);
    }


    GitLabSCMHead(String name) {
        super(name);
    }

    @Nonnull
    public abstract SCMRevisionImpl getRevision();

    public abstract int getProjectId();

    @Nonnull
    public abstract String getRef();

    @Nonnull
    abstract GitLabSCMRefSpec getRefSpec();

    @Nonnull
    public abstract GitSCM createSCM(GitLabSCMSource source);
}
