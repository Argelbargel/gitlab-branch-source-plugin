package argelbargel.jenkins.plugins.gitlab_branch_source;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPI;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import argelbargel.jenkins.plugins.gitlab_branch_source.settings.GitLabSCMSourceSettings;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.dabsquared.gitlabjenkins.connection.GitLabApiToken;
import com.dabsquared.gitlabjenkins.connection.GitLabConnection;
import com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig;
import hudson.model.Item;
import hudson.security.ACL;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

public final class GitLabHelper {
    private GitLabHelper() { /* no instances allowed */ }


    public static GitLabAPI gitLabAPI(GitLabSCMSourceSettings settings) throws GitLabAPIException {
        return gitLabAPI(settings.getConnectionName());
    }

    public static GitLabAPI gitLabAPI(String connectionName) throws GitLabAPIException {
        return gitLabAPI(gitLabConnection(connectionName));
    }

    private static GitLabAPI gitLabAPI(GitLabConnection connection) throws GitLabAPIException {
        return GitLabAPI.connect(connection.getUrl(), gitLabApiToken(connection.getApiTokenId()), connection.isIgnoreCertificateErrors(), connection.getReadTimeout());
    }

    public static GitLabConnection gitLabConnection(String connectionName) {
        for (GitLabConnection conn : connectionConfig().getConnections()) {
            if (conn.getName().equals(connectionName)) {
                return conn;
            }
        }

        throw new NoSuchElementException("unknown gitlab-connection: " + connectionName);
    }

    private static GitLabConnectionConfig connectionConfig() {
        return (GitLabConnectionConfig) Jenkins.getInstance().getDescriptor(GitLabConnectionConfig.class);
    }


    private static String gitLabApiToken(String id) {
        StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                lookupCredentials(StandardCredentials.class, (Item) null, ACL.SYSTEM, new ArrayList<DomainRequirement>()),
                CredentialsMatchers.withId(id));
        if (credentials != null) {
            if (credentials instanceof GitLabApiToken) {
                return ((GitLabApiToken) credentials).getApiToken().getPlainText();
            }
        }

        throw new IllegalStateException("No gitlab-api-token found for id: " + id);
    }
}
