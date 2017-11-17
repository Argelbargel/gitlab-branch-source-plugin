package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import hudson.model.TopLevelItem;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH1_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.assertResultAndAdded;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.createFinder;
import static java.util.Collections.singletonList;


public class FilterGitLabSCMMergeRequestsTest {
    private FilterGitLabSCMMergeRequests filter;

    @Before
    public void createFilter() throws Exception {
        filter = new FilterGitLabSCMMergeRequests(createFinder());
    }

    @Test
    public void testFilterIgnoresNonMergeRequestItems() {
        List<TopLevelItem> added = new ArrayList<>(singletonList(NON_SCM_ITEM));
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_BRANCH1_ITEM), null);

        assertResultAndAdded(singletonList(NON_SCM_ITEM), result, added);
    }

    @Test
    public void filterRemoveMergeRequestItemsNotFromOrigin() {
        filter.setShowOnlyMergeRequestsFromOrigin(true);

        List<TopLevelItem> added = new ArrayList<>(singletonList(GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM));
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM), null);

        assertResultAndAdded(Collections.<TopLevelItem>emptyList(), result, added);
    }
}