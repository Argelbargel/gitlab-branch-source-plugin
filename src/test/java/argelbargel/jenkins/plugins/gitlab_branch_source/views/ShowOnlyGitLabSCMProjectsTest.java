package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import hudson.model.TopLevelItem;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_BRANCH1_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.GITLAB_SCM_TAG_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_GITLAB_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.NON_SCM_ITEM;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.assertResultAndAdded;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.TestUtility.createFinder;
import static java.util.Arrays.asList;


public class ShowOnlyGitLabSCMProjectsTest {
    private static final List<TopLevelItem> ALL_ITEMS = asList(NON_SCM_ITEM, NON_GITLAB_SCM_ITEM, GITLAB_SCM_TAG_ITEM, GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM, GITLAB_SCM_BRANCH1_ITEM);

    private ShowOnlyGitLabSCMProjects filter;

    @Before
    public void createFilter() throws Exception {
        filter = new ShowOnlyGitLabSCMProjects(createFinder());
    }

    @Test
    public void filterAddsGitLabSCMProjects() {
        ArrayList<TopLevelItem> added = new ArrayList<>();
        List<TopLevelItem> result = filter.filter(added, ALL_ITEMS, null);

        assertResultAndAdded(asList(GITLAB_SCM_TAG_ITEM, GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM, GITLAB_SCM_BRANCH1_ITEM), result, added);
    }

    @Test
    public void filterRemovesNonGitLabSCMProjects() {
        ArrayList<TopLevelItem> added = new ArrayList<>(asList(NON_SCM_ITEM, NON_GITLAB_SCM_ITEM));
        List<TopLevelItem> result = filter.filter(added, ALL_ITEMS, null);

        assertResultAndAdded(asList(GITLAB_SCM_TAG_ITEM, GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM, GITLAB_SCM_BRANCH1_ITEM), result, added);
    }

    @Test
    public void filterRemovesBranchItems() {
        filter.setShowBranches(false);

        List<TopLevelItem> added = new ArrayList<>(ALL_ITEMS);
        List<TopLevelItem> result = filter.filter(added, ALL_ITEMS, null);

        assertResultAndAdded(asList(GITLAB_SCM_TAG_ITEM, GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM), result, added);
    }

    @Test
    public void filterRemovesMergeRequestItems() {
        filter.setShowMergeRequests(false);

        List<TopLevelItem> added = new ArrayList<>(ALL_ITEMS);
        List<TopLevelItem> result = filter.filter(added, ALL_ITEMS, null);

        assertResultAndAdded(asList(GITLAB_SCM_TAG_ITEM, GITLAB_SCM_BRANCH1_ITEM), result, added);
    }

    @Test
    public void filterRemovesTagItems() {
        filter.setShowTags(false);

        List<TopLevelItem> added = new ArrayList<>(ALL_ITEMS);
        List<TopLevelItem> result = filter.filter(added, ALL_ITEMS, null);

        assertResultAndAdded(asList(GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM, GITLAB_SCM_BRANCH1_ITEM), result, added);
    }

}