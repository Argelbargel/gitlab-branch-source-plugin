package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.views.AbstractIncludeExcludeJobFilter;
import jenkins.scm.api.SCMHead;

import java.io.Serializable;


abstract class AbstractGitLabSCMViewJobFilter<HEAD extends GitLabSCMHead> extends AbstractIncludeExcludeJobFilter {
    static final GitLabSCMHeadFinder DEFAULT_FINDER = new GitLabSCMHeadFinder() {
        @Override
        SCMHead findHead(Item item) {
            return SCMHead.HeadByItem.findHead(item);
        }
    };

    private final GitLabSCMHeadFinder finder;
    private final Class<HEAD> headClass;

    AbstractGitLabSCMViewJobFilter(String includeExcludeTypeString, GitLabSCMHeadFinder finder, Class<HEAD> headClass) {
        super(includeExcludeTypeString);
        this.finder = finder;
        this.headClass = headClass;
    }

    @Override
    protected final boolean matches(TopLevelItem item) {
        return matchesHead(item, finder.findGitLabSCMHead(item));
    }

    @SuppressWarnings("unchecked")
    private boolean matchesHead(TopLevelItem item, GitLabSCMHead head) {
        return head != null && headClass.isInstance(head) && matches(item, (HEAD) head);
    }

    protected abstract boolean matches(TopLevelItem item, HEAD head);


    static abstract class GitLabSCMHeadFinder implements Serializable {
        private GitLabSCMHead findGitLabSCMHead(Item item) {
            SCMHead head = findHead(item);
            return (head instanceof GitLabSCMHead) ? (GitLabSCMHead) head : null;
        }

        abstract SCMHead findHead(Item item);
    }
}
