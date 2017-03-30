package argelbargel.jenkins.plugins.gitlab_branch_source.actions;


import jenkins.scm.api.metadata.ObjectMetadataAction;

import java.util.Objects;


public class GitLabSCMHeadMetadataAction extends ObjectMetadataAction {
    private final int projectId;
    private final String branch;
    private final String hash;

    public GitLabSCMHeadMetadataAction(String name, int projectId, String branch, String hash, String url) {
        super(name, "", url);
        this.projectId = projectId;
        this.branch = branch;
        this.hash = hash;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getBranch() {
        return branch;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GitLabSCMHeadMetadataAction)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GitLabSCMHeadMetadataAction that = (GitLabSCMHeadMetadataAction) o;
        return getProjectId() == that.getProjectId() &&
                Objects.equals(getBranch(), that.getBranch()) &&
                Objects.equals(getHash(), that.getHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getProjectId(), getBranch(), getHash());
    }
}
