package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import org.junit.Test;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_TAG_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_GITLAB_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.createFinder;
import static hudson.views.AbstractIncludeExcludeJobFilter.IncludeExcludeType.includeMatched;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


public class FilterGitLabSCMMergeRequestsTest {
    private static FilterGitLabSCMMergeRequests createFilter(boolean matchOnlyMergeRequestsFromOrigin) throws Exception {
        return new FilterGitLabSCMMergeRequests(includeMatched.name(), matchOnlyMergeRequestsFromOrigin, createFinder());
    }

    @Test
    public void doesNotMatchNonBranchItems() throws Exception {
        FilterGitLabSCMMergeRequests filter = createFilter( false);
        assertFalse(filter.matches(NON_SCM_ITEM));
        assertFalse(filter.matches(NON_GITLAB_SCM_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_TAG_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_BRANCH_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM));
    }

    @Test
    public void matchesMergeRequestItems() throws Exception {
        FilterGitLabSCMMergeRequests filter = createFilter(false);
        assertTrue(filter.matches(GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM));
        assertTrue(filter.matches(GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM));
    }

    @Test
    public void matchesOnlyItemsWithMergeRequestsFromOrigin() throws Exception {
        FilterGitLabSCMMergeRequests filter = createFilter(true);
        assertTrue(filter.matches(GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM));
    }
}