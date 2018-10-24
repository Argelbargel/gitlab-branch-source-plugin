package argelbargel.jenkins.plugins.gitlab_branch_source;


import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPI;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProject;

import java.util.List;


class DefaultProjectQuery extends ProjectQuery {
    DefaultProjectQuery(String selector, String visibility, String searchPattern, Boolean excludeArchivedProjects) {
        super(selector, visibility, searchPattern, excludeArchivedProjects);
    }

    @Override
    protected List<GitLabProject> execute(GitLabAPI api) throws GitLabAPIException {
        return api.findProjects(getSelector(), getVisibility(), getSearchPattern(), getExcludeArchivedProjects());
    }
}
