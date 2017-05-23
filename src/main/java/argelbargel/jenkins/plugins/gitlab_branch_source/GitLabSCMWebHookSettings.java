package argelbargel.jenkins.plugins.gitlab_branch_source;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import hudson.Extension;
import hudson.RelativePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabAPI;


public final class GitLabSCMWebHookSettings extends AbstractDescribableImpl<GitLabSCMWebHookSettings> {
    private boolean listenToWebHooks;
    private boolean registerWebHooks;

    @DataBoundConstructor
    public GitLabSCMWebHookSettings(boolean listenToWebHooks, boolean registerWebHooks) {
        this.listenToWebHooks = listenToWebHooks;
        this.registerWebHooks = registerWebHooks;
    }

    GitLabSCMWebHookSettings() {
        this(true, true);
    }

    public boolean getListenToWebHooks() {
        return listenToWebHooks;
    }

    public boolean getRegisterWebHooks() {
        return registerWebHooks;
    }

    String getHookUrl(int projectId) {
        return "";
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    @Extension
    public static class DescriptorImpl extends Descriptor<GitLabSCMWebHookSettings> {
        public GitLabSCMWebHookSettings getDefaults() {
            return new GitLabSCMWebHookSettings();
        }

        public AutoCompletionCandidates doAutoCompleteHookUrl(@QueryParameter @RelativePath("../sourceSettings") String connectionName, @QueryParameter @RelativePath("..") String projectPath) throws GitLabAPIException {
            AutoCompletionCandidates c = new AutoCompletionCandidates();
            c.add(getDefaults().getHookUrl(gitLabAPI(connectionName).getProject(projectPath).getId()));
            return c;
        }
    }
}
