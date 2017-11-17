package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import hudson.model.TopLevelItem;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH1_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH2_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_DEFAULT_BRANCH_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_TAG_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.assertResultAndAdded;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.createFinder;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;


public class FilterGitLabSCMBranchesTest {
    private FilterGitLabSCMBranches filter;

    @Before
    public void createFilter() throws Exception {
        filter = new FilterGitLabSCMBranches(createFinder());
    }

    @Test
    public void testFilterIgnoresNonBranchItems() {
        List<TopLevelItem> added = new ArrayList<>(singletonList(NON_SCM_ITEM));
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_TAG_ITEM), null);

        assertResultAndAdded(singletonList(NON_SCM_ITEM), result, added);
    }

    @Test
    public void testFilterRemovesNonDefaultBranchItems() {
        filter.setShowOnlyDefaultBranches(true);

        List<TopLevelItem> added = new ArrayList<>(singletonList(GITLAB_SCM_BRANCH1_ITEM));
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_BRANCH1_ITEM), null);

        assertResultAndAdded(Collections.<TopLevelItem>emptyList(), result, added);
    }

    @Test
    public void testFilterRemovesMergeRequestBranchItems() {
        filter.setShowBranchesWithMergeRequests(false);

        List<TopLevelItem> added = new ArrayList<>(singletonList(GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM));
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM), null);

        assertResultAndAdded(Collections.<TopLevelItem>emptyList(), result, added);
    }

    @Test
    public void testFilterRemovesNonMatchingBranchesItems() {
        filter.setBranchNamePattern(".*branch1");

        List<TopLevelItem> added = new ArrayList<>(singletonList(GITLAB_SCM_BRANCH2_ITEM));
        List<TopLevelItem> result = filter.filter(added, singletonList(GITLAB_SCM_BRANCH2_ITEM), null);

        assertResultAndAdded(Collections.<TopLevelItem>emptyList(), result, added);
    }
}