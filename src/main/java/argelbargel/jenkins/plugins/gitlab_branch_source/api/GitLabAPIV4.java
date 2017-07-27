package argelbargel.jenkins.plugins.gitlab_branch_source.api;


import org.gitlab.api.GitlabAPI;


final class GitLabAPIV4 extends GitLabAPI {
    GitLabAPIV4(GitlabAPI delegate, int apiVersion, String serverVersion) {
        super(delegate, apiVersion, serverVersion);
    }

    @Override
    public GitLabMergeRequest getMergeRequest(int projectId, String mergeRequestId) throws GitLabAPIException {
        throw new UnsupportedOperationException("not yet supported for this api level");
    }

    @Override
    public GitLabMergeRequest getMergeRequest(int projectId, int mergeRequestId) throws GitLabAPIException {
        throw new UnsupportedOperationException("not yet supported for this api level");
    }
}
