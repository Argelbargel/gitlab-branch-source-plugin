package argelbargel.jenkins.plugins.gitlab_branch_source.actions;


import com.dabsquared.gitlabjenkins.cause.GitLabWebHookCause;
import hudson.model.Cause;
import hudson.model.CauseAction;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyMap;


public final class GitLabSCMCauseAction extends CauseAction {
    public GitLabSCMCauseAction(Cause... causes) {
        super(causes);
    }

    public String getDescription() {
        GitLabWebHookCause cause = findCause(GitLabWebHookCause.class);
        return (cause != null) ? cause.getShortDescription() : null;
    }

    public Map<String, String> getBuildVariables() {
        GitLabWebHookCause cause = findCause(GitLabWebHookCause.class);
        return (cause != null) ? cause.getData().getBuildVariables() : Collections.<String, String>emptyMap();
    }
}
