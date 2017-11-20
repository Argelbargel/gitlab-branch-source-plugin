package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.views.AbstractIncludeExcludeJobFilter;
import jenkins.scm.api.SCMHead;

import java.io.Serializable;


abstract class AbstractGitLabSCMViewJobFilter extends AbstractIncludeExcludeJobFilter {
    static final GitLabSCMHeadFinder DEFAULT_FINDER = new GitLabSCMHeadFinder() {
        @Override
        SCMHead findHead(Item item) {
            return SCMHead.HeadByItem.findHead(item);
        }
    };

    private final GitLabSCMHeadFinder finder;

    AbstractGitLabSCMViewJobFilter(String includeExcludeTypeString, GitLabSCMHeadFinder finder) {
        super(includeExcludeTypeString);
        this.finder = finder;
    }

    @Override
    protected final boolean matches(TopLevelItem item) {
        return matches(item, finder.findGitLabSCMHead(item));
    }

    protected abstract boolean matches(TopLevelItem item, GitLabSCMHead head);


    static abstract class GitLabSCMHeadFinder implements Serializable {
        private GitLabSCMHead findGitLabSCMHead(Item item) {
            SCMHead head = findHead(item);
            return (head instanceof GitLabSCMHead) ? (GitLabSCMHead) head : null;
        }

        abstract SCMHead findHead(Item item);
    }
}
