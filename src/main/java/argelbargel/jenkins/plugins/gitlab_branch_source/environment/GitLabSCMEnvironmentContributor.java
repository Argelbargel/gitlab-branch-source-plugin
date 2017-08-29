package argelbargel.jenkins.plugins.gitlab_branch_source.environment;

import argelbargel.jenkins.plugins.gitlab_branch_source.actions.GitLabSCMCauseAction;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.IOException;

@Extension
public class GitLabSCMEnvironmentContributor extends EnvironmentContributor{
    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        GitLabSCMCauseAction cause = r.getAction(GitLabSCMCauseAction.class);
        if (cause != null) {
            envs.overrideAll(cause.getBuildVariables());
        }
    }
}
