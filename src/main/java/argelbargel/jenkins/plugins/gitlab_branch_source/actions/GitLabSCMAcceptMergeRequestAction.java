package argelbargel.jenkins.plugins.gitlab_branch_source.actions;


import com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty;
import com.dabsquared.gitlabjenkins.gitlab.api.GitLabClient;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabMergeRequest;
import com.dabsquared.gitlabjenkins.gitlab.api.model.MergeRequest;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;


public final class GitLabSCMAcceptMergeRequestAction extends InvisibleAction implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(GitLabSCMAcceptMergeRequestAction.class.getName());

    private final int mergeRequestID;
    private final int mergeRequestPID;
    private final int mergeRequestScopedId;
    private final String commitMessage;
    private final boolean removeSourceBranch;

    public GitLabSCMAcceptMergeRequestAction(GitLabMergeRequest MR, int mergeRequestScopedId, String commitMessage, boolean removeSourceBranch) {
        this.mergeRequestPID = MR.getProjectId();
        this.mergeRequestID = MR.getIid();
        this.mergeRequestScopedId = mergeRequestScopedId;
        this.commitMessage = commitMessage;
        this.removeSourceBranch = removeSourceBranch;
    }

    public void acceptMergeRequest(Run<?, ?> build, TaskListener listener) {
        MergeRequest mergeRequest = new MergeRequest();
        mergeRequest.setProjectId(mergeRequestPID);
        mergeRequest.setId(mergeRequestID);
        GitLabClient client = GitLabConnectionProperty.getClient(build);
        if (client == null) {
            listener.getLogger().format("cannot publish build-status pending as no gitlab-connection is configured!");
        } else {
            try {
                client.acceptMergeRequest(mergeRequest, MessageFormat.format(commitMessage, mergeRequestScopedId, build.getFullDisplayName()), removeSourceBranch);
            } catch (Exception e) {
                listener.getLogger().format("failed to accept merge-request: " + e.getMessage());
                LOGGER.log(SEVERE, "failed to accept merge-request", e);
            }
        }
    }
}
