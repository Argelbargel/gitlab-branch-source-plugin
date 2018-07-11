package argelbargel.jenkins.plugins.gitlab_branch_source.events;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMSource;
import argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMMergeRequestHead;
import com.dabsquared.gitlabjenkins.cause.CauseData;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.MergeRequestHook;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.MergeRequestObjectAttributes;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.NoteHook;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.events.CauseDataHelper.buildCauseData;
import static argelbargel.jenkins.plugins.gitlab_branch_source.heads.GitLabSCMHead.*;
import static jenkins.scm.api.SCMEvent.Type.*;


public final class GitLabSCMNoteEvent extends GitLabSCMHeadEvent<NoteHook> {
    public static GitLabSCMNoteEvent create(String id, NoteHook hook, String origin) {
           //   Logger LOGGER = Logger.getLogger(GitLabSCMNoteEvent.class.getName());


        if(hook.getObjectAttributes().getNote().equalsIgnoreCase("jenkins retry"))
        {
            if(hook.getMergeRequest()!=null)
            {
                return new GitLabSCMNoteEvent(CREATED, id, hook, origin);
            }
        }

        return null;

    }

    private GitLabSCMNoteEvent(Type type, String id, NoteHook payload, String origin) {
        super(type, id, payload, origin);
    }

    @Override
    CauseData getCauseData() {
        Logger.getLogger(GitLabSCMNoteEvent.class.getName()).severe("Note GetCauseData");
        return buildCauseData(getPayload());
    }

    @Override
    protected boolean isMatch(@Nonnull GitLabSCMSource source) {

        Logger.getLogger(GitLabSCMNoteEvent.class.getName()).severe("Note ISMatch");
        if (!super.isMatch(source) || !isOrigin(source, getAttributes().getTargetProjectId())) {

            Logger.getLogger(GitLabSCMNoteEvent.class.getName()).severe("Note ISMatch returning false");
            return false;
        }

        boolean isOrigin = isOrigin(source, getAttributes().getSourceProjectId());
        boolean rData= ((isOrigin && source.getSourceSettings().getOriginMonitorStrategy().getMonitored()) || (!isOrigin && source.getSourceSettings().getForksMonitorStrategy().getMonitored()));


        return rData;
    }

    private boolean isOrigin(@Nonnull GitLabSCMSource source, Integer projectId) {

        return projectId.equals(source.getProjectId());
    }

    private MergeRequestObjectAttributes getAttributes() {
        return getPayload().getMergeRequest();
    }

    @Override
    Collection<? extends GitLabSCMHead> heads(@Nonnull GitLabSCMSource source) throws IOException, InterruptedException {
        Collection<GitLabSCMHead> heads = new ArrayList<>();
        MergeRequestObjectAttributes attributes = getAttributes();
        Integer sourceProjectId = attributes.getSourceProjectId();
        String sourceBranch = attributes.getSourceBranch();
        String hash = attributes.getLastCommit().getId();
        GitLabSCMMergeRequestHead head = createMergeRequest(
                attributes.getIid(), attributes.getTitle(), attributes.getIid(),
                createBranch(sourceProjectId, sourceBranch, hash),
                createBranch(attributes.getTargetProjectId(), attributes.getTargetBranch(), REVISION_HEAD));

        Logger.getLogger(GitLabSCMNoteEvent.class.getName()).severe("key area  sourceid: " + source.getId() + " branch id:" +source.getpro);
        if (source.getSourceSettings().buildUnmerged(head)) {
            heads.add(head);
        }

        if (source.getSourceSettings().buildMerged(head)) {
            heads.add(head.merged());
        }

        if (head.fromOrigin()) {
            heads.add(createBranch(sourceProjectId, sourceBranch, hash));
        }

        return heads;
    }

    @Nonnull
    @Override
    public String getSourceName() {
        return getAttributes().getTarget().getPathWithNamespace();
    }
}
