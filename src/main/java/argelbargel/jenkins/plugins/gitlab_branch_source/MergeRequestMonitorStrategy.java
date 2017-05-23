package argelbargel.jenkins.plugins.gitlab_branch_source;


import org.kohsuke.stapler.DataBoundSetter;


public class MergeRequestMonitorStrategy extends MonitorStrategy {
    private boolean buildUnmerged;
    private boolean ignoreWorkInProgress;
    private boolean onlyMergeable;
    private boolean acceptMergeRequests;

    protected MergeRequestMonitorStrategy(boolean monitor, boolean buildUnmerged, BuildStatusPublishMode buildStatusPublishMode) {
        super(monitor, buildUnmerged, buildStatusPublishMode);
        this.ignoreWorkInProgress = true;
        this.onlyMergeable = false;
        this.buildUnmerged = false;
        this.acceptMergeRequests = false;
    }

    @DataBoundSetter
    public final void setBuildUnmerged(boolean value) {
        buildUnmerged = value;
    }

    public final boolean getBuildUnmerged() {
        return buildUnmerged;
    }

    @DataBoundSetter
    public final void setIgnoreWorkInProgress(boolean value) {
        ignoreWorkInProgress = value;
    }

    public final boolean getIgnoreWorkInProgress() {
        return ignoreWorkInProgress;
    }

    @DataBoundSetter
    public final void setBuildOnlyMergeableMerged(boolean value) {
        onlyMergeable = value;
    }

    public final boolean getBuildOnlyMergeableMerged() {
        return onlyMergeable;
    }

    public final boolean getAcceptMergeRequests() {
        return acceptMergeRequests;
    }

    @DataBoundSetter
    public final void setAcceptMergeRequests(boolean value) {
        acceptMergeRequests = value;
    }
}
