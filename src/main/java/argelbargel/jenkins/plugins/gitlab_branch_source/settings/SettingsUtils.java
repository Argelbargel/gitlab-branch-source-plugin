package argelbargel.jenkins.plugins.gitlab_branch_source.settings;


import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.dabsquared.gitlabjenkins.connection.GitLabConnection;
import com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabConnection;


final class SettingsUtils {
    static final String DEFAULT_MERGE_COMMIT_MESSAGE = "Accepted Merge-Request #{0} after build {1} succeeded";
    static final String CHECKOUT_CREDENTIALS_ANONYMOUS = "ANONYMOUS";
    private static final Logger LOGGER = Logger.getLogger(SettingsUtils.class.getName());


    @Nonnull
    static String defaultGitLabConnectionName() {
        List<String> connections = gitLabConnectionNames();
        return (!connections.isEmpty()) ? connections.get(0) : "";
    }

    static List<String> gitLabConnectionNames() {
        GitLabConnectionConfig config = connectionConfig();
        List<String> names = new ArrayList<>();

        for (GitLabConnection conn : config.getConnections()) {
            names.add(conn.getName());
        }

        return names;
    }

    static List<DomainRequirement> gitLabConnectionRequirements(String connectioName) {
        URIRequirementBuilder builder = URIRequirementBuilder.create();

        try {
            URL connectionURL = new URL(gitLabConnection(connectioName).getUrl());
            builder.withHostnamePort(connectionURL.getHost(), connectionURL.getPort());
        } catch (Exception ignored) {
            LOGGER.fine("ignoring invalid gitlab-connection: " + connectioName);
        }

        return builder.build();
    }

    private static GitLabConnectionConfig connectionConfig() {
        return (GitLabConnectionConfig) Jenkins.getInstance().getDescriptor(GitLabConnectionConfig.class);
    }


    private SettingsUtils() { /* no instances required */}
}
