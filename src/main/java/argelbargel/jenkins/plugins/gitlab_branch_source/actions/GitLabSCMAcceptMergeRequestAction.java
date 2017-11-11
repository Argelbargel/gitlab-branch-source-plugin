package argelbargel.jenkins.plugins.gitlab_branch_source.actions;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabMergeRequest;
import com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty;
import com.dabsquared.gitlabjenkins.gitlab.api.GitLabClient;
import com.dabsquared.gitlabjenkins.gitlab.api.model.MergeRequest;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.gitlab.api.models.GitlabMergeRequest;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;


public final class GitLabSCMAcceptMergeRequestAction extends InvisibleAction implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(GitLabSCMAcceptMergeRequestAction.class.getName());

    private final GitlabMergeRequest mergeRequest;
    private final String commitMessage;
    private final boolean removeSourceBranch;

    public GitLabSCMAcceptMergeRequestAction(GitLabMergeRequest mr, String commitMessage, boolean removeSourceBranch) {
        this.mergeRequest = mr;
        this.commitMessage = commitMessage;
        this.removeSourceBranch = removeSourceBranch;
    }

    public void acceptMergeRequest(Run<?, ?> build, TaskListener listener) {
        GitLabClient client = GitLabConnectionProperty.getClient(build);
        if (client == null) {
            listener.getLogger().format("cannot publish build-status pending as no gitlab-connection is configured!");
        } else {
            try {
                MergeRequest mr = new MergeRequest(mergeRequest.getIid(), mergeRequest.getIid(),
                                                   mergeRequest.getSourceBranch(), mergeRequest.getTargetBranch(),
                                                   mergeRequest.getTitle(), mergeRequest.getSourceProjectId(), mergeRequest.getTargetProjectId(),
                                                   mergeRequest.getDescription(), mergeRequest.getMergeStatus());
                client.acceptMergeRequest(mr, MessageFormat.format(commitMessage, mergeRequest.getIid(), build.getFullDisplayName()), removeSourceBranch);
            } catch (Exception e) {
                listener.getLogger().format("failed to accept merge-request: " + e.getMessage());
                LOGGER.log(SEVERE, "failed to accept merge-request", e);
            }
        }
    }
}
