package argelbargel.jenkins.plugins.gitlab_branch_source.settings;


import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public final class GitLabSCMForksMonitorStrategy extends MergeRequestMonitorStrategy {
    @SuppressWarnings("WeakerAccess")
    @DataBoundConstructor
    public GitLabSCMForksMonitorStrategy(boolean monitored, boolean build, BuildStatusPublishMode buildStatusPublishMode) {
        super(monitored, build, buildStatusPublishMode);
    }

    GitLabSCMForksMonitorStrategy() {
        this(false, true, BuildStatusPublishMode.stages);
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
