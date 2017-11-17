package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.views.ViewJobFilter;
import jenkins.scm.api.SCMHead;

import java.io.Serializable;
import java.util.List;


abstract class AbstractGitLabSCMViewJobFilter extends ViewJobFilter {
    enum FilterMode {
        ADD,
        REMOVE,
        NONE
    }

    protected static final GitLabSCMHeadFinder DEFAULT_FINDER = new GitLabSCMHeadFinder() {
        @Override
        SCMHead findHead(Item item) {
            return SCMHead.HeadByItem.findHead(item);
        }
    };

    private final GitLabSCMHeadFinder finder;

    AbstractGitLabSCMViewJobFilter(GitLabSCMHeadFinder finder) {
        this.finder = finder;
    }

    @Override
    public final List<TopLevelItem> filter(List<TopLevelItem> added, List<TopLevelItem> all, View view) {
        for (TopLevelItem item : all) {
            switch (filter(item, finder.findGitLabSCMHead(item))) {
                case ADD:
                    if (!added.contains(item)) {
                        added.add(item);
                    }
                    break;
                case REMOVE:
                    added.remove(item);
                    break;
                default:
                    // do nothing
            }
        }

        return added;
    }

    protected abstract FilterMode filter(Item item, GitLabSCMHead head);

    static abstract class GitLabSCMHeadFinder implements Serializable {
        private GitLabSCMHead findGitLabSCMHead(Item item) {
            SCMHead head = findHead(item);
            return (head instanceof GitLabSCMHead) ? (GitLabSCMHead) head : null;
        }

        abstract SCMHead findHead(Item item);
    }
}
