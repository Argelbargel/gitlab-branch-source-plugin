package argelbargel.jenkins.plugins.gitlab_branch_source;


import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

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

    public final boolean getBuild() {
        return build;
    }

    public final BuildStatusPublishMode getBuildStatusPublishMode() {
        return buildStatusPublishMode;
    }


    protected static abstract class MonitorStrategyDescriptor<T extends MonitorStrategy> extends Descriptor<MonitorStrategy> {
        @Nonnull
        public abstract T getDefaults();

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
