package argelbargel.jenkins.plugins.gitlab_branch_source;

import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPI;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabAPIException;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabMergeRequest;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.filters.GitLabMergeRequestFilter;
import argelbargel.jenkins.plugins.gitlab_branch_source.events.GitLabSCMMergeRequestEvent;
import argelbargel.jenkins.plugins.gitlab_branch_source.events.GitLabSCMPushEvent;
import argelbargel.jenkins.plugins.gitlab_branch_source.events.GitLabSCMTagPushEvent;
import argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMMergeRequestHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMTagHead;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.MergeRequestObjectAttributes;
import hudson.model.TaskListener;
import jenkins.branch.MultiBranchProject;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.*;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabTag;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static argelbargel.jenkins.plugins.gitlab_branch_source.GitLabHelper.gitLabAPI;
import static argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMHead.createBranch;
import static argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMHead.createMergeRequest;
import static argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMHead.createTag;
import static argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMRefSpec.BRANCHES;
import static argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMRefSpec.TAGS;
import static hudson.model.TaskListener.NULL;
import static java.util.Collections.emptyMap;

import java.util.logging.Logger;
import java.util.logging.Level;


class SourceHeads {

    private static final Logger LOGGER = Logger.getLogger(SourceHeads.class.getName());
    private static final SCMHeadObserver NOOP_OBSERVER = new SCMHeadObserver() {
        @Override
        public void observe(@Nonnull jenkins.scm.api.SCMHead head, @Nonnull SCMRevision revision) { /* NOOP */ }
    };
    private static final SCMSourceCriteria ALL_CRITERIA = new SCMSourceCriteria() {
        @Override
        public boolean isHead(@Nonnull Probe probe, @Nonnull TaskListener taskListener) throws IOException {
            return true;
        }
    };
    private static final String CAN_BE_MERGED = "can_be_merged";


    private final GitLabSCMSource source;
    private transient Map<Integer, String> branchesWithMergeRequestsCache;


    SourceHeads(GitLabSCMSource source) {
        this.source = source;
    }

    void retrieve(@CheckForNull SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @CheckForNull SCMHeadEvent<?> event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        if (event instanceof GitLabSCMMergeRequestEvent) {
            retrieveMergeRequest(criteria, observer, (GitLabSCMMergeRequestEvent) event, listener);
        } else if (event instanceof GitLabSCMTagPushEvent) {
            retrieveTag(criteria, observer, (GitLabSCMTagPushEvent) event, listener);
        } else if (event instanceof GitLabSCMPushEvent) {
            retrieveBranch(criteria, observer, (GitLabSCMPushEvent) event, listener);
        } else {
            retrieveAll(criteria, observer, listener);
        }
    }

    @CheckForNull
    SCMRevision retrieve(@Nonnull SCMHead head, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        log(listener, Messages.GitLabSCMSource_retrievingRevision(head.getName()));
        try {
            return new SCMRevisionImpl(head, retrieveRevision(head));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private String retrieveRevision(SCMHead head) throws GitLabAPIException {
        if (head instanceof GitLabSCMMergeRequestHead) {
            return retrieveMergeRequestRevision(((GitLabSCMMergeRequestHead) head).getId());
        } else if (head instanceof GitLabSCMTagHead) {
            return retrieveTagRevision(head.getName());
        }

        return retrieveBranchRevision(head.getName());
    }

    private String retrieveMergeRequestRevision(String id) throws GitLabAPIException {
        return api().getMergeRequest(source.getProjectId(), id).getSha();
    }

    private String retrieveTagRevision(String name) throws GitLabAPIException {
        return api().getTag(source.getProjectId(), name).getCommit().getId();
    }

    private String retrieveBranchRevision(String name) throws GitLabAPIException {
        return api().getBranch(source.getProjectId(), name).getCommit().getId();
    }

    private void retrieveMergeRequest(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @Nonnull GitLabSCMMergeRequestEvent event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        MergeRequestObjectAttributes attributes = event.getPayload().getObjectAttributes();
        String targetBranch = attributes.getTargetBranch();

        if (!source.isExcluded(targetBranch)) {
            int mrId = attributes.getIid();
            log(listener, Messages.GitLabSCMSource_retrievingMergeRequest(mrId));
            try {
                GitLabMergeRequest mr = api().getMergeRequest(source.getProjectId(), mrId);
                observe(criteria, observer, mr, listener);
            } catch (NoSuchElementException e) {
                log(listener, Messages.GitLabSCMSource_removedMergeRequest(mrId));
                branchesWithMergeRequests(listener).remove(mrId);
            }
        }
    }

    private void retrieveBranch(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @Nonnull GitLabSCMPushEvent event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        retrieveBranch(criteria, observer, BRANCHES.remoteName(event.getPayload().getRef()), listener);
    }

    private void retrieveBranch(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, String branchName, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        if (!source.isExcluded(branchName)) {
            log(listener, Messages.GitLabSCMSource_retrievingBranch(branchName));
            try {
                GitlabBranch branch = api().getBranch(source.getProjectId(), branchName);
                observe(criteria, observer, branch, listener);
            } catch (NoSuchElementException e) {
                log(listener, Messages.GitLabSCMSource_removedHead(branchName));
            }
        }
    }

    private void retrieveTag(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @Nonnull GitLabSCMTagPushEvent event, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        retrieveTag(criteria, observer, TAGS.remoteName(event.getPayload().getRef()), listener);
    }

    private void retrieveTag(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, String tagName, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        if (!source.isExcluded(tagName)) {
            log(listener, Messages.GitLabSCMSource_retrievingTag(tagName));
            try {
                GitlabTag tag = api().getTag(source.getProjectId(), tagName);
                tag.getCommit().getCommittedDate().getTime();
                observe(criteria, observer, tag, listener);
            } catch (NoSuchElementException e) {
                log(listener, Messages.GitLabSCMSource_removedHead(tagName));
            }
        }
    }

    private void retrieveAll(@CheckForNull SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        // TODO: could/should we optimize based on SCMHeadObserver#getIncludes()?
        retrieveMergeRequests(criteria, observer, listener);
        retrieveBranches(criteria, observer, listener);
        retrieveTags(criteria, observer, listener);
    }

    private void retrieveMergeRequests(@CheckForNull SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        branchesWithMergeRequestsCache = new HashMap<>();

        if (source.getProject().isMergeRequestsEnabled() && source.getSourceSettings().shouldMonitorMergeRequests()) {
            log(listener, Messages.GitLabSCMSource_retrievingMergeRequests());

            GitLabMergeRequestFilter filter = source.getSourceSettings().createMergeRequestFilter(listener);
            for (GitLabMergeRequest mr : filter.filter(api().getMergeRequests(source.getProjectId()))) {
                checkInterrupt();

                if (!source.isExcluded(mr.getTargetBranch())) {
                    observe(criteria, observer, mr, listener);
                }
            }
        }
    }

    private void retrieveBranches(@CheckForNull SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        if (source.getSourceSettings().getBranchMonitorStrategy().getMonitored()) {
            log(listener, Messages.GitLabSCMSource_retrievingBranches());

            for (GitlabBranch branch : api().getBranches(source.getProjectId())) {
                checkInterrupt();

                if (!source.isExcluded(branch.getName())) {
                    observe(criteria, observer, branch, listener);
                }
            }
        }
    }

    private void retrieveTags(@CheckForNull SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        if (source.getSourceSettings().getTagMonitorStrategy().getMonitored()) {
            log(listener, Messages.GitLabSCMSource_retrievingTags());
            for (GitlabTag tag : api().getTags(source.getProjectId())) {
                checkInterrupt();

                if (!source.isExcluded(tag.getName())) {
                    observe(criteria, observer, tag, listener);
                }
            }
        }
    }

    private void observe(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, GitlabBranch branch, TaskListener listener) throws IOException, InterruptedException {
        log(listener, Messages.GitLabSCMSource_monitoringBranch(branch.getName()));

        boolean hasMergeRequest = branchesWithMergeRequests(NULL).containsValue(branch.getName());
        if (hasMergeRequest && !source.getSourceSettings().getBranchMonitorStrategy().getBuildBranchesWithMergeRequests()) {
            log(listener, Messages.GitLabSCMSource_willNotBuildBranchWithMergeRequest(branch.getName()));
        }

        observe(criteria, observer, createBranch(source.getProjectId(), branch.getName(), branch.getCommit().getId(), hasMergeRequest), listener);
    }

    private void observe(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, GitlabTag tag, TaskListener listener) throws IOException, InterruptedException {
        log(listener, Messages.GitLabSCMSource_monitoringTag(tag.getName()));
        observe(criteria, observer, createTag(source.getProjectId(), tag.getName(), tag.getCommit().getId(), tag.getCommit().getCommittedDate().getTime()), listener);
    }

    private void observe(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, GitLabMergeRequest mergeRequest, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        log(listener, Messages.GitLabSCMSource_monitoringMergeRequest(mergeRequest.getIid()));
        String targetBranch = mergeRequest.getTargetBranch();
        GitLabSCMMergeRequestHead head = createMergeRequest(
                mergeRequest.getIid(),
                mergeRequest.getTitle(),
                mergeRequest.getIid(),
                createBranch(mergeRequest.getSourceProjectId(), mergeRequest.getSourceBranch(), mergeRequest.getSha()),
                createBranch(mergeRequest.getTargetProjectId(), targetBranch, retrieveBranchRevision(targetBranch)), Objects.equals(mergeRequest.getMergeStatus(), CAN_BE_MERGED));
        if (source.getSourceSettings().buildUnmerged(head)) {
            observe(criteria, observer, head, listener);
        }
        if (source.getSourceSettings().buildMerged(head)) {
            if (!head.isMergeable() && buildOnlyMergeableRequests(head)) {
                log(listener, Messages.GitLabSCMSource_willNotBuildUnmergeableRequest(mergeRequest.getIid(), mergeRequest.getTargetBranch(), mergeRequest.getMergeStatus()));
            }
            observe(criteria, observer, head.merged(), listener);
        }
        if (!source.getSourceSettings().getBranchMonitorStrategy().getBuildBranchesWithMergeRequests() && head.fromOrigin()) {
            branchesWithMergeRequests(listener).put(mergeRequest.getIid(), mergeRequest.getSourceBranch());
        }
    }

    private void observe(SCMSourceCriteria criteria, @Nonnull SCMHeadObserver observer, GitLabSCMHead head, TaskListener listener) throws IOException, InterruptedException {
        if (criteria == null || matches(criteria, head, listener)) {
            observer.observe(head, head.getRevision());
        }
    }

    private boolean matches(SCMSourceCriteria criteria, GitLabSCMHead head, TaskListener listener) {
        SCMSourceCriteria.Probe probe = source.createProbe(head, head.getRevision());
        try {
            if (criteria.isHead(probe, listener)) {
                log(listener, head.getName() + " (" + head.getRevision().getHash() + ") meets criteria");
                return true;
            } else {
                log(listener, head.getName() + " (" + head.getRevision().getHash() + ") does not meet criteria");
            }
        } catch (IOException e) {
            log(listener, "error checking criteria: " + e.getMessage());
        }
        return false;
    }

    private GitLabAPI api() throws GitLabAPIException {
        return gitLabAPI(source.getSourceSettings());
    }

    private void log(@Nonnull TaskListener listener, String message) {
        listener.getLogger().format(message + "\n");
    }

    private Map<Integer, String> branchesWithMergeRequests(TaskListener listener) throws IOException, InterruptedException {
        if (source.getSourceSettings().getBranchMonitorStrategy().getBuildBranchesWithMergeRequests()) {
            return emptyMap();
        }
        if (branchesWithMergeRequestsCache == null) {
            retrieveMergeRequests(ALL_CRITERIA, NOOP_OBSERVER, listener);
        }
        return branchesWithMergeRequestsCache;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean buildOnlyMergeableRequests(SCMHead head) {
        if (head instanceof GitLabSCMMergeRequestHead) {
            return source.getSourceSettings().determineMergeRequestStrategyValue(
                    ((GitLabSCMMergeRequestHead) head),
                    source.getSourceSettings().getOriginMonitorStrategy().getBuildOnlyMergeableMerged(),
                    source.getSourceSettings().getForksMonitorStrategy().getBuildOnlyMergeableMerged());
        }
        return true;
    }

    private void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
