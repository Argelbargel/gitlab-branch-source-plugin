package argelbargel.jenkins.plugins.gitlab_branch_source;


import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import static argelbargel.jenkins.plugins.gitlab_branch_source.BuildStatusPublishMode.stages;


public final class GitLabSCMBranchMonitorStrategy extends MonitorStrategy {
    private String includes;
    private String excludes;
    private boolean buildBranchesWithMergeRequests;

    @DataBoundConstructor
    public GitLabSCMBranchMonitorStrategy(boolean monitored, BuildStatusPublishMode buildStatusPublishMode) {
        super(monitored, monitored, buildStatusPublishMode);
        this.includes = DescriptorImpl.DEFAULT_INCLUDES;
        this.excludes = DescriptorImpl.DEFAULT_EXCLUDES;
        this.buildBranchesWithMergeRequests = false;
    }

    GitLabSCMBranchMonitorStrategy() {
        this(true, stages);
    }

    public String getIncludes() {
        return includes;
    }

    @DataBoundSetter
    public void setIncludes(String includes) {
        this.includes = (includes != null) ? includes : DescriptorImpl.DEFAULT_INCLUDES;
    }

    public String getExcludes() {
        return excludes;
    }

    @DataBoundSetter
    public void setExcludes(String excludes) {
        this.excludes = (excludes != null) ? excludes : DescriptorImpl.DEFAULT_EXCLUDES;
    }

    public boolean getBuildBranchesWithMergeRequests() {
        return buildBranchesWithMergeRequests;
    }

    @DataBoundSetter
    public void setBuildBranchesWithMergeRequests(boolean value) {
        buildBranchesWithMergeRequests = value;
    }


    @SuppressWarnings({"unused", "WeakerAccess"})
    @Extension
    public static final class DescriptorImpl extends MonitorStrategyDescriptor<GitLabSCMBranchMonitorStrategy> {
        public static final String DEFAULT_INCLUDES = "*";
        public static final String DEFAULT_EXCLUDES = "";

        @Override
        public GitLabSCMBranchMonitorStrategy getDefaults() {
            return new GitLabSCMBranchMonitorStrategy();
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckIncludes(@QueryParameter String includes) {
            if (includes.isEmpty()) {
                return FormValidation.warning(Messages.GitLabSCMBranchMonitorStrategy_did_you_mean_to_use_to_match_all_branches());
            }
            return FormValidation.ok();
        }
    }
}
