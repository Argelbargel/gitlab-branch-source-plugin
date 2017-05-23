package argelbargel.jenkins.plugins.gitlab_branch_source;


import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

import static argelbargel.jenkins.plugins.gitlab_branch_source.BuildStatusPublishMode.stages;


public final class GitLabSCMForksMonitorStrategy extends MergeRequestMonitorStrategy {
    @DataBoundConstructor
    public GitLabSCMForksMonitorStrategy(boolean monitored, boolean build, BuildStatusPublishMode buildStatusPublishMode) {
        super(monitored, build, buildStatusPublishMode);
    }

    GitLabSCMForksMonitorStrategy() {
        this(false, true, stages);
    }

    @Extension
    public static final class DescriptorImpl extends MonitorStrategyDescriptor<GitLabSCMForksMonitorStrategy> {
        @Nonnull
        @Override
        public GitLabSCMForksMonitorStrategy getDefaults() {
            return new GitLabSCMForksMonitorStrategy();
        }
    }
}
