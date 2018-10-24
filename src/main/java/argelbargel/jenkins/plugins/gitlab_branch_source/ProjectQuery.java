package argelbargel.jenkins.plugins.gitlab_branch_source;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPI;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProject;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectSelector;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectVisibility;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabAPI;


abstract class ProjectQuery {
    public static ProjectQuery create(GitLabSCMNavigator navigator) {
        if (!StringUtils.isEmpty(navigator.getProjectGroup())) {
            return new GroupProjectQuery(navigator.getProjectGroup(), navigator.getProjectSelectorId(), navigator.getProjectVisibilityId(), navigator.getProjectSearchPattern(), navigator.getSourceSettings().getExcludeArchivedProjects());
        } else {
            return new DefaultProjectQuery(navigator.getProjectSelectorId(), navigator.getProjectVisibilityId(), navigator.getProjectSearchPattern(), navigator.getSourceSettings().getExcludeArchivedProjects());
        }
    }

    private GitLabProjectSelector selector;
    private GitLabProjectVisibility visibility;
    private String searchPattern;
    private Boolean excludeArchivedProjects;

    ProjectQuery(String selector, String visibility, String searchPattern, Boolean excludeArchivedProjects) {
        this(GitLabProjectSelector.byId(selector), GitLabProjectVisibility.byId(visibility), searchPattern, excludeArchivedProjects);
    }

    private ProjectQuery(GitLabProjectSelector selector, GitLabProjectVisibility visibility, String searchPattern, Boolean excludeArchivedProjects) {
        this.selector = selector;
        this.visibility = visibility;
        this.searchPattern = searchPattern;
        this.excludeArchivedProjects = excludeArchivedProjects;
    }

    @Nonnull
    final GitLabProjectSelector getSelector() {
        return selector;
    }

    @Nonnull
    final GitLabProjectVisibility getVisibility() {
        return visibility;
    }

    @Nonnull
    final String getSearchPattern() {
        return searchPattern;
    }
    
    final Boolean getExcludeArchivedProjects() {
        return excludeArchivedProjects;
    }

    final List<GitLabProject> execute(String connectionName) throws GitLabAPIException {
        return execute(gitLabAPI(connectionName));
    }

    protected abstract List<GitLabProject> execute(GitLabAPI api) throws GitLabAPIException;
}
