package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import org.junit.Test;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_DEFAULT_BRANCH_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_TAG_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_GITLAB_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.createFinder;
import static hudson.views.AbstractIncludeExcludeJobFilter.IncludeExcludeType.includeMatched;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


public class GitLabSCMBranchFilterTest {
    private static GitLabSCMBranchFilter createFilter(boolean matchOnlyDefaultBranch, boolean matchOnlyBranchesWithMergeRequests) throws Exception {
        return new GitLabSCMBranchFilter(includeMatched.name(), matchOnlyDefaultBranch, matchOnlyBranchesWithMergeRequests, createFinder());
    }

    @Test
    public void doesNotMatchNonBranchItems() throws Exception {
        GitLabSCMBranchFilter filter = createFilter(false, false);
        assertFalse(filter.matches(NON_SCM_ITEM));
        assertFalse(filter.matches(NON_GITLAB_SCM_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_TAG_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM));
    }

    @Test
    public void matchesBranchItems() throws Exception {
        GitLabSCMBranchFilter filter = createFilter(false, false);
        assertTrue(filter.matches(GITLAB_SCM_DEFAULT_BRANCH_ITEM));
        assertTrue(filter.matches(GITLAB_SCM_BRANCH_ITEM));
        assertTrue(filter.matches(GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM));
    }

    @Test
    public void matchesOnlyDefaultBranchItems() throws Exception {
        GitLabSCMBranchFilter filter = createFilter(true, false);
        assertTrue(filter.matches(GITLAB_SCM_DEFAULT_BRANCH_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_BRANCH_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM));
    }

    @Test
    public void matchesOnlyBranchItemsWithMergeRequest() throws Exception {
        GitLabSCMBranchFilter filter = createFilter(false, true);
        assertFalse(filter.matches(GITLAB_SCM_DEFAULT_BRANCH_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_BRANCH_ITEM));
        assertTrue(filter.matches(GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM));
    }
}