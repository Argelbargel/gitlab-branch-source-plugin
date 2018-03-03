package argelbargel.jenkins.plugins.gitlab_branch_source.settings;


import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;


public class MonitorStrategy extends AbstractDescribableImpl<MonitorStrategy> {
    private boolean monitor;
    private boolean build;
    private BuildStatusPublishMode buildStatusPublishMode;

    MonitorStrategy(boolean monitored, boolean build, BuildStatusPublishMode buildStatusPublishMode) {
        this.monitor = monitored;
        this.build = build;
        this.buildStatusPublishMode = buildStatusPublishMode;
    }

    public final boolean getMonitored() {
        return monitor;
    }

    @DataBoundSetter
    public final void setMonitored(boolean value) {
        monitor = value;
    }

    public final boolean getBuild() {
        return build;
    }

    @DataBoundSetter
    public final void setBuild(boolean value) {
        build = value;
    }

    public final BuildStatusPublishMode getBuildStatusPublishMode() {
        return buildStatusPublishMode;
    }

    @DataBoundSetter
    public final void setBuildStatusPublishMode(BuildStatusPublishMode value) {
        buildStatusPublishMode = value;
    }

    protected static abstract class MonitorStrategyDescriptor<T extends MonitorStrategy> extends Descriptor<MonitorStrategy> {
        @Nonnull
        public abstract T getDefaults();

        @SuppressWarnings("unused")
        @Restricted(NoExternalUse.class)
        public final ListBoxModel doFillBuildStatusPublishModeItems() {
            StandardListBoxModel result = new StandardListBoxModel();
            for (BuildStatusPublishMode mode : BuildStatusPublishMode.values()) {
                result.add(mode.name());
            }

            return result;
        }
    }
}
