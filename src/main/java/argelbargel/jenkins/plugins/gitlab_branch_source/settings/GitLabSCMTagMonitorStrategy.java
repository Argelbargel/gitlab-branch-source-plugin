package argelbargel.jenkins.plugins.gitlab_branch_source.settings;


import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public final class GitLabSCMTagMonitorStrategy extends MonitorStrategy {
    @SuppressWarnings("WeakerAccess")
    @DataBoundConstructor
    public GitLabSCMTagMonitorStrategy(boolean monitored, boolean build, BuildStatusPublishMode buildStatusPublishMode) {
        super(monitored, build, buildStatusPublishMode);
    }

    GitLabSCMTagMonitorStrategy() {
        this(false, false, BuildStatusPublishMode.stages);
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
