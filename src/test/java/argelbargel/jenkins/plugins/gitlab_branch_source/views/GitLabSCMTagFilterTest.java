package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import org.junit.Test;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_TAG_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_GITLAB_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.createFinder;
import static hudson.views.AbstractIncludeExcludeJobFilter.IncludeExcludeType.includeMatched;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


public class GitLabSCMTagFilterTest {
    private static GitLabSCMTagFilter createFilter() throws Exception {
        return new GitLabSCMTagFilter(includeMatched.name(), createFinder());
    }

    @Test
    public void doesNotMatchNonTagItems() throws Exception {
        GitLabSCMTagFilter filter = createFilter();
        assertFalse(filter.matches(NON_SCM_ITEM));
        assertFalse(filter.matches(NON_GITLAB_SCM_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_BRANCH_ITEM));
        assertFalse(filter.matches(GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM));
    }

    @Test
    public void matchesTagItems() throws Exception {
        GitLabSCMTagFilter filter = createFilter();
        assertTrue(filter.matches(GITLAB_SCM_TAG_ITEM));
    }
}