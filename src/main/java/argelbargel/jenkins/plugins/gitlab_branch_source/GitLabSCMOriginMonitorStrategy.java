package argelbargel.jenkins.plugins.gitlab_branch_source;


import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

import static argelbargel.jenkins.plugins.gitlab_branch_source.BuildStatusPublishMode.stages;


public final class GitLabSCMOriginMonitorStrategy extends MergeRequestMonitorStrategy {
    private boolean removeSourceBranch;

    @DataBoundConstructor
    public GitLabSCMOriginMonitorStrategy(boolean monitored, boolean build, BuildStatusPublishMode buildStatusPublishMode) {
        super(monitored, build, buildStatusPublishMode);
        this.removeSourceBranch = false;
    }

    GitLabSCMOriginMonitorStrategy() {
        this(true, true, stages);
    }

    public final boolean getRemoveSourceBranch() {
        return removeSourceBranch;
    }

    @DataBoundSetter
    public final void setRemoveSourceBranch(boolean value) {
        removeSourceBranch = value;
    }


    @Extension
    public static final class DescriptorImpl extends MonitorStrategyDescriptor<GitLabSCMOriginMonitorStrategy> {
        @Nonnull
        @Override
        public GitLabSCMOriginMonitorStrategy getDefaults() {
            return new GitLabSCMOriginMonitorStrategy();
        }
    }
}
