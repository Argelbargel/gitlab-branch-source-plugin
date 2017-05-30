package argelbargel.jenkins.plugins.gitlab_branch_source.hooks;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMNavigator;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMSource;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.model.UnprotectedRootAction;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Extension
public final class GitLabSCMWebHook implements UnprotectedRootAction {
    static final String HOOK_PATH_SEP = "/";
    @SuppressWarnings("WeakerAccess")
    static final String URL_NAME = "gitlab-scm";
    static final String NOTIFICATION_ENDPOINT = URL_NAME + HOOK_PATH_SEP + "notify";

    private static final Pattern HOOK_ID_PATTERN = Pattern.compile("^([^/]+)(" + Pattern.quote(HOOK_PATH_SEP) + "\\d+)?$");


    public static GitLabSCMWebHookListener createListener(GitLabSCMNavigator navigator) {
        return new GitLabSCMWebHookListener(navigator.getSourceSettings().getConnectionName(), 0);
    }

    public static GitLabSCMWebHookListener createListener(GitLabSCMSource source) {
        return new GitLabSCMWebHookListener(source.getSourceSettings().getConnectionName(), source.getProjectId());
    }

    public static GitLabSCMWebHook get() {
        return Jenkins.getInstance().getExtensionList(RootAction.class).get(GitLabSCMWebHook.class);
    }

    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void initialize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ACL.impersonate(ACL.SYSTEM, new ListenerInitializerTask(GitLabSCMWebHook.get()));
            }
        }).start();
    }


    private final HookManager manager;
    private final HookHandler handler;

    public GitLabSCMWebHook() {
        manager = new HookManager();
        handler = new HookHandler();
    }

    public void addListener(GitLabSCMNavigator navigator, Item owner) {
        manager.addListener(navigator.getHookListener(), owner, navigator.getRegisterWebHooks());
    }

    public void addListener(GitLabSCMSource source, Item owner) {
        manager.addListener(source.getHookListener(), owner, source.getRegisterWebHooks());
    }

    public void removeListener(GitLabSCMNavigator navigator, Item owner) {
        manager.removeListener(navigator.getHookListener(), owner, navigator.getRegisterWebHooks());
    }

    public void removeListener(GitLabSCMSource source, Item owner) {
        manager.removeListener(source.getHookListener(), owner, source.getRegisterWebHooks());
    }


    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }


    @SuppressWarnings("unused")
    @RequirePOST
    public HttpResponse doNotify(StaplerRequest req) {
        try {
            handler.handle(extractListenerId(req), req);
            return HttpResponses.ok();
        } catch (HttpResponses.HttpResponseException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw HttpResponses.error(400, "bad request: " + e.getMessage());
        } catch (Exception e) {
            throw HttpResponses.error(500, e.getMessage());
        }
    }

    private String extractListenerId(StaplerRequest req) {
        String path = req.getRestOfPath();
        String id = getListenerId(path.substring(1));
        if (id == null) {
            throw HttpResponses.notFound();
        }

        return id;
    }

    private String getListenerId(String id) {
        if (manager.hasListener(id)) {
            return id;
        }

        // unknown project-hooks (<connection-id>/<project-id>) are redirected to navigator (<connection-id>)
        Matcher m = HOOK_ID_PATTERN.matcher(id);
        if (m.matches() && manager.hasListener(m.group(1))) {
            return m.group(1);
        }

        return null;

    }


    private static class ListenerInitializerTask implements Runnable {
        private final GitLabSCMWebHook hook;

        private ListenerInitializerTask(GitLabSCMWebHook hook) {
            this.hook = hook;
        }

        @Override
        public void run() {
            for (SCMNavigatorOwner owner : Jenkins.getInstance().getAllItems(SCMNavigatorOwner.class)) {
                for (SCMNavigator navigator : owner.getSCMNavigators()) {
                    if (navigator instanceof GitLabSCMNavigator) {
                        handle((GitLabSCMNavigator) navigator, owner);
                    }
                }
            }

            for (SCMSourceOwner owner : Jenkins.getInstance().getAllItems(SCMSourceOwner.class)) {
                for (SCMSource source : owner.getSCMSources()) {
                    if (source instanceof GitLabSCMSource) {
                        handle((GitLabSCMSource) source, owner);
                    }
                }
            }
        }

        private void handle(GitLabSCMSource source, SCMSourceOwner owner) {
            if (source.getListenToWebHooks()) {
                hook.addListener(source, owner);
            }
        }

        private void handle(GitLabSCMNavigator navigator, SCMNavigatorOwner owner) {
            if (navigator.getListenToWebHooks() && !StringUtils.isEmpty(navigator.getSourceSettings().getConnectionName())) {
                hook.addListener(navigator, owner);
            }
        }
    }
}
