package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.views.ViewJobFilter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.ADD;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.REMOVE;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_TAG_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.assertResultAndAdded;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.createFinder;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;


public class AbstractGitLabSCMViewJobFilterTest {
    @Test
    public void filterAddsItem() throws Exception {
        ViewJobFilter filter = createFilter(ADD);

        TopLevelItem item = mock(TopLevelItem.class);
        List<TopLevelItem> added = new ArrayList<>();
        List<TopLevelItem> result = filter.filter(added, singletonList(item), null);

        assertResultAndAdded(singletonList(item), result, added);
    }

    @Test
    public void filterDoesNotProduceDuplicates() throws Exception {
        ViewJobFilter filter = createFilter(ADD);

        List<TopLevelItem> added = singletonList(GITLAB_SCM_TAG_ITEM);
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_TAG_ITEM), null);

        assertResultAndAdded(singletonList(GITLAB_SCM_TAG_ITEM), result, added);
    }

    @Test
    public void removesItem() throws Exception {
        ViewJobFilter filter = createFilter(REMOVE);

        List<TopLevelItem> added = new ArrayList<>(singletonList(GITLAB_SCM_TAG_ITEM));
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_TAG_ITEM), null);

        assertResultAndAdded(Collections.<TopLevelItem>emptyList(), result, added);
    }

    private ViewJobFilter createFilter(final AbstractGitLabSCMViewJobFilter.FilterMode mode) throws Exception {
        return new AbstractGitLabSCMViewJobFilter(createFinder()) {
            @Override
            protected FilterMode filter(Item item, GitLabSCMHead head) {
                return mode;
            }
        };
    }
}