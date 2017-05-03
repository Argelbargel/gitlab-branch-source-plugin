package argelbargel.jenkins.plugins.gitlab_branch_source.hooks;

import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPI;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import hudson.model.Item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabAPI;

class HookManager {
    private static final Logger LOGGER = Logger.getLogger(HookManager.class.getName());

    private final Map<String, ListenerState> managedListeners;

    HookManager() {
        managedListeners = new HashMap<>();
    }

    synchronized void addListener(GitLabSCMWebHookListener listener, Item owner, boolean register) {
        addListener(listener, owner);
        if (register) {
            registerHook(listener);
        }
    }

    synchronized void removeListener(GitLabSCMWebHookListener listener, Item owner, boolean unregister) {
        removeListener(listener, owner);
        if (unregister) {
            unregisterHook(listener);
        }
    }

    private void addListener(GitLabSCMWebHookListener listener, Item owner) {
        if (!hasListener(listener.id())) {
            managedListeners.put(listener.id(), new ListenerState());
        }

        managedListeners.get(listener.id()).acquire(owner);
    }

    private void registerHook(GitLabSCMWebHookListener listener) {
        if (!hasListener(listener.id())) {
            throw new IllegalArgumentException("unknown listener with id: " + listener.id());
        }

        ListenerState managed = managedListeners.get(listener.id());
        if (!managed.isRegistered()) {
            try {
                GitLabAPI api = gitLabAPI(listener.connectionName());
                if (listener.listensToSystem()) {
                    LOGGER.info("registering system-hook for " + listener.id() + "...");
                    api.registerSystemHook(listener.url());
                } else {
                    LOGGER.info("registering project-hook for " + listener.id() + "...");
                    api.registerProjectHook(listener.url(), listener.projectId());
                }
                managed.register();
            } catch (GitLabAPIException e) {
                LOGGER.warning("could not register hook " + listener.url() + ": " + e.getMessage());
            }
        }
    }

    private void removeListener(GitLabSCMWebHookListener listener, Item owner) {
        ListenerState managed = managedListeners.get(listener.id());
        if (managed != null) {
            managed.release(owner);
            if (!managed.hasUsages()) {
                unregisterAndDestroy(listener, managed);
            }
        }
    }

    private void unregisterAndDestroy(GitLabSCMWebHookListener listener, ListenerState managed) {
        unregisterHook(listener);
        if (!managed.isRegistered()) {
            managedListeners.remove(listener.id());
        }
    }

    private void unregisterHook(GitLabSCMWebHookListener listener) {
        ListenerState managed = managedListeners.get(listener.id());
        if (managed != null && !managed.hasUsages() && managed.isRegistered()) {
            try {
                GitLabAPI api = gitLabAPI(listener.connectionName());
                if (listener.listensToSystem()) {
                    LOGGER.info("un-registering system-hook for " + listener.id() + "...");
                    api.unregisterSystemHook(listener.url());
                } else {
                    LOGGER.info("un-registering project-hook for " + listener.id() + "...");
                    api.unregisterProjectHook(listener.url(), listener.projectId());
                }
                managed.unregister();
            } catch (GitLabAPIException e) {
                LOGGER.warning("could not un-register hook " + listener.url() + ": " + e.getMessage());
            }
        }
    }

    boolean hasListener(String id) {
        return managedListeners.containsKey(id);
    }


    private static class ListenerState {
        private Set<String> users;
        private boolean registered;

        ListenerState() {
            users = new HashSet<>();
            registered = false;
        }

        void acquire(Item owner) {
            if (!users.contains(owner.getFullName())) {
                LOGGER.info("acquiring listener for " + owner.getFullName() + "...");
                users.add(owner.getFullName());
            }
        }

        void release(Item owner) {
            if (users.contains(owner.getFullName())) {
                LOGGER.info("releasing listener for " + owner.getFullName() + "...");
                users.remove(owner.getFullName());
            }
        }

        boolean hasUsages() {
            return !users.isEmpty();
        }

        void register() {
            registered = true;
        }

        void unregister() {
            registered = false;
        }

        boolean isRegistered() {
            return registered;
        }
    }
}
