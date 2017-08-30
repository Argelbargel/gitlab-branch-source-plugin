package argelbargel.jenkins.plugins.gitlab_branch_source.environment;


import argelbargel.jenkins.plugins.gitlab_branch_source.actions.GitLabSCMCauseAction;
import com.dabsquared.gitlabjenkins.cause.CauseData;
import com.dabsquared.gitlabjenkins.cause.GitLabWebHookCause;
import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.StreamBuildListener;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import static com.dabsquared.gitlabjenkins.cause.CauseDataBuilder.causeData;
import static org.junit.Assert.assertEquals;


public class GitLabSCMEnvironmentContributorTest {
    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();

    private BuildListener listener;

    @Before
    public void setup() {
        listener = new StreamBuildListener(jenkins.createTaskListener().getLogger(), Charset.defaultCharset());
    }

    @Test
    public void freeStyleProjectTest() throws IOException, InterruptedException, ExecutionException {
        FreeStyleProject p = jenkins.createFreeStyleProject();
        GitLabWebHookCause cause = new GitLabWebHookCause(generateCauseData());
        FreeStyleBuild b = p.scheduleBuild2(0, cause).get();
        b.addAction(new GitLabSCMCauseAction());

        EnvVars env = b.getEnvironment(listener);
        assertEnv(env);
    }

    private CauseData generateCauseData() {
        return causeData()
                .withActionType(CauseData.ActionType.MERGE)
                .withSourceProjectId(1)
                .withTargetProjectId(1)
                .withBranch("feature")
                .withSourceBranch("feature")
                .withUserName("")
                .withSourceRepoHomepage("https://gitlab.org/test")
                .withSourceRepoName("test")
                .withSourceNamespace("test-namespace")
                .withSourceRepoUrl("git@gitlab.org:test.git")
                .withSourceRepoSshUrl("git@gitlab.org:test.git")
                .withSourceRepoHttpUrl("https://gitlab.org/test.git")
                .withMergeRequestTitle("Test")
                .withMergeRequestId(1)
                .withMergeRequestIid(1)
                .withTargetBranch("master")
                .withTargetRepoName("test")
                .withTargetNamespace("test-namespace")
                .withTargetRepoSshUrl("git@gitlab.org:test.git")
                .withTargetRepoHttpUrl("https://gitlab.org/test.git")
                .withTriggeredByUser("test")
                .withLastCommit("123")
                .withTargetProjectUrl("https://gitlab.org/test")
                .build();
    }

    private void assertEnv(EnvVars env) {
        assertEquals("1", env.get("gitlabMergeRequestId"));
        assertEquals("git@gitlab.org:test.git", env.get("gitlabSourceRepoUrl"));
        assertEquals("master", env.get("gitlabTargetBranch"));
        assertEquals("test", env.get("gitlabTargetRepoName"));
        assertEquals("feature", env.get("gitlabSourceBranch"));
        assertEquals("test", env.get("gitlabSourceRepoName"));
    }

}