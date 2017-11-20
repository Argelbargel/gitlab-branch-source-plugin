package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMBranchHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMMergeRequestHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMTagHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.GitLabSCMHeadFinder;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import jenkins.model.AbstractTopLevelItem;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


final class TestUtility {
    static final TopLevelItem NON_SCM_ITEM = createItem();
    static final TopLevelItem NON_GITLAB_SCM_ITEM = createItem();
    static final TopLevelItem GITLAB_SCM_TAG_ITEM = createItem();
    static final TopLevelItem GITLAB_SCM_BRANCH_ITEM = createItem();
    static final TopLevelItem GITLAB_SCM_DEFAULT_BRANCH_ITEM = createDefaultBranchItem();
    static final TopLevelItem GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM = createItem();
    static final TopLevelItem GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM = createItem();
    static final TopLevelItem GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM = createItem();


    private TestUtility() { /* utility class */ }

    static GitLabSCMHeadFinder createFinder() throws Exception {
        HeadFinderStub finder = new HeadFinderStub();
        finder.put(NON_GITLAB_SCM_ITEM, mock(SCMHead.class));
        finder.put(GITLAB_SCM_TAG_ITEM, createGitLabSCMTagHead());
        finder.put(GITLAB_SCM_DEFAULT_BRANCH_ITEM, createGitLabSCMBranchHead());
        finder.put(GITLAB_SCM_BRANCH_ITEM, createGitLabSCMBranchHead("branch"));
        finder.put(GITLAB_SCM_BRANCH_WITH_MERGE_REQUEST_ITEM, createGitLabSCMBranchHead("",true));
        finder.put(GITLAB_SCM_MERGEREQUEST_FROM_ORIGIN_ITEM, createGitLabSCMMergeRequest(true));
        finder.put(GITLAB_SCM_MERGEREQUEST_FROM_FORK_ITEM, createGitLabSCMMergeRequest(false));
        return finder;
    }

    private static AbstractTopLevelItem createItem() {
        return mock(AbstractTopLevelItem.class);
    }

    private static TopLevelItem createDefaultBranchItem() {
        AbstractTopLevelItem item = createItem();
        when(item.getAction(PrimaryInstanceMetadataAction.class)).thenReturn(mock(PrimaryInstanceMetadataAction.class));
        return item;
    }

    private static GitLabSCMBranchHead createGitLabSCMBranchHead() throws Exception {
        return createGitLabSCMBranchHead("");
    }

    private static GitLabSCMBranchHead createGitLabSCMBranchHead(String name) throws Exception {
        return createGitLabSCMBranchHead(name, false);
    }

    private static GitLabSCMBranchHead createGitLabSCMBranchHead(String name, boolean hasMergeRequest) throws Exception {
        return createGitLabSCMBranchHead(0, name, hasMergeRequest);
    }

    private static GitLabSCMBranchHead createGitLabSCMBranchHead(int projectId, String name, boolean hasMergeRequest) throws Exception {
        Constructor<GitLabSCMBranchHead> c = GitLabSCMBranchHead.class.getDeclaredConstructor(int.class, String.class, String.class, boolean.class);
        c.setAccessible(true);
        return c.newInstance(projectId, name, "", hasMergeRequest);
    }

    private static GitLabSCMMergeRequestHead createGitLabSCMMergeRequest(boolean fromOrigin) throws Exception {
        Constructor<GitLabSCMMergeRequestHead> c = GitLabSCMMergeRequestHead.class.getDeclaredConstructor(int.class, String.class, GitLabSCMHead.class, GitLabSCMBranchHead.class, boolean.class);
        c.setAccessible(true);
        return c.newInstance(0, "", createGitLabSCMBranchHead((fromOrigin ? 0 : 1), "", false), createGitLabSCMBranchHead(0, "", true), true);
    }

    private static GitLabSCMTagHead createGitLabSCMTagHead() throws Exception {
        Constructor<GitLabSCMTagHead> c = GitLabSCMTagHead.class.getDeclaredConstructor(int.class, String.class, String.class, long.class);
        c.setAccessible(true);
        return c.newInstance(0, "", "", 0);
    }

    private static final class HeadFinderStub extends GitLabSCMHeadFinder {
        private final Map<Item, SCMHead> heads;

        HeadFinderStub() {
            heads = new HashMap<>();
        }

        void put(Item item, SCMHead head) {
            heads.put(item, head);
        }

        @Override
        SCMHead findHead(Item item) {
            return heads.get(item);
        }
    }
}
