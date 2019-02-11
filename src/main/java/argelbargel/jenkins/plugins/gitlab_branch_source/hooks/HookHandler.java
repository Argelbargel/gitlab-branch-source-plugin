package argelbargel.jenkins.plugins.gitlab_branch_source.hooks;

import argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabHookEventType;
import argelbargel.jenkins.plugins.gitlab_branch_source.api.SystemHook;
import argelbargel.jenkins.plugins.gitlab_branch_source.events.*;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.MergeRequestHook;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.PushHook;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.WebHook;
import com.dabsquared.gitlabjenkins.util.JsonUtil;
import hudson.util.IOUtils;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMSourceEvent;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.logging.Level;

import static argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabHookEventType.byHeader;
import static jenkins.scm.api.SCMEvent.originOf;


class HookHandler {
    private static final Logger LOGGER = Logger.getLogger(HookHandler.class.getName());
    private static final String GITLAB_EVENT_TYPE_HEADER = "X-Gitlab-Event";
    private static final String CHARSET_UTF_8 = "utf-8";

    void handle(String id, HttpServletRequest request) throws IOException {
        handle(id, getEventType(request), request);
    }

    private void handle(String id, GitLabHookEventType eventType, HttpServletRequest request) throws IOException {
        LOGGER.fine("handling hook for " + id + " for eventType " + eventType);
        try {
            String requestBody = getRequestBody(request);
            switch (eventType) {
                case PUSH:
                    SCMHeadEvent.fireNow(new GitLabSCMPushEvent(id, readHook(PushHook.class, requestBody), originOf(request)));
                    break;
                case TAG_PUSH:
                    SCMHeadEvent.fireNow(new GitLabSCMTagPushEvent(id, readHook(PushHook.class, requestBody), originOf(request)));
                    break;
                case MERGE_REQUEST:
                    argelbargel.jenkins.plugins.gitlab_branch_source.api.Hooks.MergeRequestHook hookAction= readHookTemp(argelbargel.jenkins.plugins.gitlab_branch_source.api.Hooks.MergeRequestHook.class, requestBody);
                    SCMHeadEvent.fireNow(GitLabSCMMergeRequestEvent.create(id, readHook(MergeRequestHook.class, requestBody),hookAction.getObjectAttributes().getAction(), originOf(request)));
                    break;
                case SYSTEM_HOOK:
                    handleSystemHook(id, request,requestBody);
                    break;
                default:
                    LOGGER.warning("ignoring hook: " + eventType);
                    throw new IllegalArgumentException("cannot handle hook-event of type " + eventType);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "GitLabHookEventType", e);
        }
    }

    private void handleSystemHook(String id, HttpServletRequest request, String requestBody) throws IOException {
        try {
            SystemHook hook = readHook(SystemHook.class, requestBody);
            SCMSourceEvent.fireNow(GitLabSCMSourceEvent.create(id, hook, originOf(request)));
        } catch (IllegalArgumentException e) {
            LOGGER.warning("ignoring system hook: " + e.getMessage());
        }
    }

    private <T extends WebHook> T readHook(Class<T> type, String requestBody) {
        try {
            return JsonUtil.read(requestBody, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not read payload");
        }
    }
    private <T extends argelbargel.jenkins.plugins.gitlab_branch_source.api.Hooks.WebHook> T readHookTemp(Class<T> type, String requestBody) {
        try {
            return JsonUtil.read(requestBody, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not read payload");
        }
    }

    private GitLabHookEventType getEventType(HttpServletRequest req) {
        try {
            return byHeader(req.getHeader(GITLAB_EVENT_TYPE_HEADER));
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    private String getRequestBody(HttpServletRequest request) throws IOException {
        String charset = request.getCharacterEncoding() == null ? CHARSET_UTF_8 : request.getCharacterEncoding();
        String requestBody = IOUtils.toString(request.getInputStream(), charset);
        if (StringUtils.isBlank(requestBody)) {
            throw new IllegalArgumentException("request-body is empty");
        }

        return requestBody;
    }
}
