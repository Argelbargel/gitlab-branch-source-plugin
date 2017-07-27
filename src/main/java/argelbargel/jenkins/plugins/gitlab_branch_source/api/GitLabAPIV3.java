package argelbargel.jenkins.plugins.gitlab_branch_source.api;


import org.gitlab.api.GitlabAPI;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;


final class GitLabAPIV3 extends GitLabAPI {
    GitLabAPIV3(GitlabAPI delegate, int apiVersion, String serverVersion) {
        super(delegate, apiVersion, serverVersion);
    }

    @Override
    public GitLabMergeRequest getMergeRequest(int projectId, String mergeRequestId) throws GitLabAPIException {
        return getMergeRequest(projectId, Integer.parseInt(mergeRequestId));
    }

    @Override
    public GitLabMergeRequest getMergeRequest(int projectId, int mergeRequestId) throws GitLabAPIException {
        try {
            String tailUrl = "/projects/" + projectId + "/merge_requests/" + mergeRequestId;
            return getDelegate().retrieve().to(tailUrl, GitLabMergeRequest.class);
        } catch (FileNotFoundException e) {
            throw new NoSuchElementException("unknown merge-request for project " + projectId + ": " + mergeRequestId);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }


}
