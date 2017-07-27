package argelbargel.jenkins.plugins.gitlab_branch_source.api;

import java.io.IOException;

public final class GitLabAPIException extends IOException {
    GitLabAPIException(Exception e) {
        this("error accessing gitlab-api: " + e.getMessage(), e.getCause());
    }

    GitLabAPIException(String message, Throwable cause) {
        super(message, cause.getCause());
    }
}
