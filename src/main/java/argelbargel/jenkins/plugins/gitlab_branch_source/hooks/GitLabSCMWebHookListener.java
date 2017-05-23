package argelbargel.jenkins.plugins.gitlab_branch_source.hooks;

import argelbargel.jenkins.plugins.gitlab_branch_source.Messages;
import jenkins.model.Jenkins;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabConnection;
import static argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHook.HOOK_PATH_SEP;
import static argelbargel.jenkins.plugins.gitlab_branch_source.hooks.GitLabSCMWebHook.NOTIFICATION_ENDPOINT;

public final class GitLabSCMWebHookListener {
    private final String connectionName;
    private final int projectId;
    private transient String idCache;
    private boolean listen;
    private boolean register;

    GitLabSCMWebHookListener(String connectionName, int projectId) {
        this.connectionName = connectionName;
        this.projectId = projectId;
        this.listen = true;
        this.register = true;
    }

    String connectionName() {
        return connectionName;
    }

    int projectId() {
        return projectId;
    }

    boolean listensToSystem() {
        return projectId < 1;
    }

    public boolean getListen() {
        return listen;
    }

    public void setListen(boolean value) {
        listen = value;
    }

    public boolean getRegister() {
        return register;
    }

    public void setRegister(boolean value) {
        register = value;
    }

    public String id() {
        if (idCache == null) {
            idCache = generateId();
        }

        return idCache;
    }

    private String generateId() {
        try {
            StringBuilder idBuilder = new StringBuilder(DigestUtils.md5Hex(gitLabConnection(connectionName).getUrl()));
            if (projectId > 0) {
                idBuilder.append(HOOK_PATH_SEP).append(projectId);
            }
            return idBuilder.toString();
        } catch (NoSuchElementException ignore) {
            // silently ignore and re-generate id later
            return null;
        }
    }

    public URL url() {
        try {
            return new URL(Jenkins.getInstance().getRootUrl() + NOTIFICATION_ENDPOINT + HOOK_PATH_SEP + id());
        } catch (MalformedURLException e) {
            throw new RuntimeException(Messages.GitLabSCMWebHook_hook_url_is_malformed(e.getMessage()));
        }
    }
}
