package argelbargel.jenkins.plugins.gitlab_branch_source;


import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

import static argelbargel.jenkins.plugins.gitlab_branch_source.BuildStatusPublishMode.stages;


public final class GitLabSCMTagMonitorStrategy extends MonitorStrategy {
    @DataBoundConstructor
    public GitLabSCMTagMonitorStrategy(boolean monitored, boolean build, BuildStatusPublishMode buildStatusPublishMode) {
        super(monitored, build, buildStatusPublishMode);
    }

    GitLabSCMTagMonitorStrategy() {
        this(false, false, stages);
    }

    @Extension
    public static final class DescriptorImpl extends MonitorStrategyDescriptor<GitLabSCMTagMonitorStrategy> {
        @Nonnull
        @Override
        public GitLabSCMTagMonitorStrategy getDefaults() {
            return new GitLabSCMTagMonitorStrategy();
        }
    }
}
