package argelbargel.jenkins.plugins.gitlab_branch_source.api;


import org.apache.commons.lang.StringUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.http.Query;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabProjectHook;
import org.gitlab.api.models.GitlabRepositoryTree;
import org.gitlab.api.models.GitlabSystemHook;
import org.gitlab.api.models.GitlabTag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectSelector.VISIBLE;
import static argelbargel.jenkins.plugins.gitlab_branch_source.api.GitLabProjectVisibility.ALL;


public abstract class GitLabAPI {
    protected static final Logger LOGGER = Logger.getLogger(GitLabAPI.class.getName());
    private static final String PATH_SEP = "/";
    private static final Map<String, String> VERSION_CACHE = new VersionCache(10);

    public static GitLabAPI connect(String url, String token) throws GitLabAPIException {
        GitlabAPI delegate = GitlabAPI.connect(url, token);
        String version = retrieveServerVersion(delegate, url);
        return GitLabAPIImplVersion.byServerVersion(version).create(delegate, version);
    }

    private static String retrieveServerVersion(GitlabAPI delegate, String url) throws GitLabAPIException {
        try {
            if (!VERSION_CACHE.containsKey(url)) {
                VERSION_CACHE.put(url, delegate.retrieve().to("/version", GitLabVersion.class).toString());
            }
            return VERSION_CACHE.get(url);
        } catch (IOException e) {
            throw new GitLabAPIException(e);
        }
    }


    private final GitlabAPI delegate;
    private final int apiVersion;
    private final String serverVersion;


    GitLabAPI(GitlabAPI delegate, int apiVersion, String serverVersion) {
        this.apiVersion = apiVersion;
        this.serverVersion = serverVersion;
        this.delegate = delegate;
    }

    protected final GitlabAPI getDelegate() {
        return delegate;
    }

    public final int getVersion() {
        return apiVersion;
    }

    public final String getServerVersion() throws GitLabAPIException {
        return serverVersion;
    }

    public final GitLabProject getProject(int id) throws GitLabAPIException {
        return getProject((Serializable) id);
    }

    public final GitLabProject getProject(String name) throws GitLabAPIException {
        return getProject((Serializable) encode(name));
    }

    protected GitLabProject getProject(Serializable nameOrId) throws GitLabAPIException {
        try {
            String tailUrl = GitlabProject.URL + "/" + nameOrId;
            return getDelegate().retrieve().to(tailUrl, GitLabProject.class);
        } catch (FileNotFoundException e) {
            throw new NoSuchElementException("unknown project " + nameOrId);
        } catch (IOException e) {
            throw new GitLabAPIException(e);
        }
    }

    public final List<GitlabBranch> getBranches(int id) throws GitLabAPIException {
        return getBranches((Serializable) id);
    }

    @SuppressWarnings("WeakerAccess") // API
    protected List<GitlabBranch> getBranches(Serializable nameOrId) throws GitLabAPIException {
        try {
            return getDelegate().getBranches(nameOrId);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public GitlabBranch getBranch(int projectId, String branch) throws GitLabAPIException {
        try {
            String tailUrl = GitlabProject.URL + PATH_SEP + projectId + GitlabBranch.URL + PATH_SEP + URLEncoder.encode(branch, "UTF-8");
            return getDelegate().retrieve().to(tailUrl, GitlabBranch.class);
        } catch (FileNotFoundException e) {
            throw new NoSuchElementException("unknown branch " + branch);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public final List<GitlabTag> getTags(int id) throws GitLabAPIException {
        return getTags((Serializable) id);
    }

    @SuppressWarnings("WeakerAccess") // API
    protected List<GitlabTag> getTags(Serializable nameOrId) throws GitLabAPIException {
        try {
            return getDelegate().getTags(nameOrId);
        } catch (IOException e) {
            throw new GitLabAPIException(e);
        }
    }

    public GitlabTag getTag(int projectId, String tag) throws GitLabAPIException {
        try {
            String tailUrl = GitlabProject.URL + PATH_SEP + projectId + GitlabTag.URL + PATH_SEP + URLEncoder.encode(tag, "UTF-8");
            return getDelegate().retrieve().to(tailUrl, GitlabTag.class);
        } catch (FileNotFoundException e) {
            throw new NoSuchElementException("unknown tag " + tag);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public List<GitLabMergeRequest> getMergeRequests(int projectId) throws GitLabAPIException {
        try {
            String tailUrl = "/projects/" + projectId + "/merge_requests?state=opened";
            return getDelegate().retrieve()
                    .getAll(tailUrl, GitLabMergeRequest[].class);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public abstract GitLabMergeRequest getMergeRequest(int projectId, String mergeRequestId) throws GitLabAPIException;

    public abstract GitLabMergeRequest getMergeRequest(int projectId, int mergeRequestId) throws GitLabAPIException;

    public List<GitLabProject> findProjects(String group, GitLabProjectSelector selector, GitLabProjectVisibility visibility, String searchPattern) throws GitLabAPIException {
        LOGGER.fine("finding projects for group" + group + ", " + selector + ", " + visibility + ", " + searchPattern + "...");
        return findProjects(projectUrl(group, selector, visibility, searchPattern));
    }

    public final List<GitLabProject> findProjects(GitLabProjectSelector selector, GitLabProjectVisibility visibility, String searchPattern) throws GitLabAPIException {
        LOGGER.fine("finding projects for " + selector + ", " + visibility + ", " + searchPattern + "...");
        return findProjects(projectUrl(selector, visibility, searchPattern));
    }

    @SuppressWarnings("WeakerAccess") // API
    protected List<GitLabProject> findProjects(String url) throws GitLabAPIException {
        try {
            return getDelegate()
                    .retrieve()
                    .getAll(url, GitLabProject[].class);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public GitLabGroup getGroup(int id) throws GitLabAPIException {
        try {
            return getDelegate().retrieve().to(GitlabGroup.URL + PATH_SEP + id, GitLabGroup.class);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public GitlabCommit getCommit(int id, String ref) throws GitLabAPIException {
        try {
            return getDelegate().getCommit(id, ref);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public List<GitlabRepositoryTree> getTree(int id, String ref, String path) throws GitLabAPIException {
        try {
            Query query = new Query()
                    .appendIf("path", path)
                    .appendIf("ref_name", ref);


            String tailUrl = GitlabProject.URL + "/" + id + "/repository" + GitlabRepositoryTree.URL + query.toString();
            GitlabRepositoryTree[] tree = getDelegate().retrieve().to(tailUrl, GitlabRepositoryTree[].class);
            return Arrays.asList(tree);
        } catch (Exception e) {
            throw new GitLabAPIException(e);
        }
    }

    public final GitlabSystemHook registerSystemHook(URL url) throws GitLabAPIException {
        try {
            return registerSystemHook(url.toString());
        } catch (IOException e) {
            throw new GitLabAPIException(e);
        }
    }

    @SuppressWarnings("WeakerAccess") // API
    protected GitlabSystemHook registerSystemHook(String url) throws IOException {
        LOGGER.fine("registering system-hook " + url + "...");
        for (GitlabSystemHook hook : getDelegate().getSystemHooks()) {
            if (hook.getUrl().equals(url)) {
                return hook;
            }
        }

        return getDelegate().dispatch()
                .with("url", url)
                .with("push_events", false)
                .to("/hooks", GitlabSystemHook.class);
    }

    public final boolean unregisterSystemHook(URL url) throws GitLabAPIException {
        try {
            return unregisterSystemHook(url.toString());
        } catch (IOException e) {
            throw new GitLabAPIException(e);
        }
    }

    @SuppressWarnings("WeakerAccess") // API
    protected boolean unregisterSystemHook(String url) throws IOException {
        LOGGER.finer("looking up system-hooks...");
        for (GitlabSystemHook hook : getDelegate().getSystemHooks()) {
            if (hook.getUrl().equals(url)) {
                LOGGER.fine("un-registering system-hook " + url + "...");
                getDelegate().deleteSystemHook(hook.getId());
                return true;
            }
        }
        return false;
    }

    public final GitlabProjectHook registerProjectHook(URL url, int projectId) throws GitLabAPIException {
        try {
            return registerProjectHook(url.toString(), projectId);
        } catch (IOException e) {
            throw new GitLabAPIException(e);
        }
    }

    @SuppressWarnings("WeakerAccess") // API
    protected GitlabProjectHook registerProjectHook(String url, int projectId) throws IOException {
        LOGGER.fine("registering project-hook for project " + projectId + ": " + url + "...");
        for (GitlabProjectHook hook : getDelegate().getProjectHooks(projectId)) {
            if (hook.getUrl().equals(url)) {
                return hook;
            }
        }

        return getDelegate().addProjectHook(projectId, url, true, false, true, true, false);
    }

    public final boolean unregisterProjectHook(URL url, int projectId) throws GitLabAPIException {
        try {
            return unregisterProjectHook(url.toString(), projectId);
        } catch (IOException e) {
            throw new GitLabAPIException(e);
        }
    }

    @SuppressWarnings("WeakerAccess") // API
    protected boolean unregisterProjectHook(String url, int projectId) throws IOException {
        LOGGER.finer("looking up project-hooks for project " + projectId + "...");
        for (GitlabProjectHook hook : getDelegate().getProjectHooks(projectId)) {
            if (hook.getUrl().equals(url)) {
                LOGGER.fine("un-registering project-hook for project " + projectId + ": " + url + "...");
                String tailUrl = GitlabProject.URL + PATH_SEP + hook.getProjectId() + GitlabProjectHook.URL + PATH_SEP + hook.getId();
                getDelegate().retrieve().method("DELETE").to(tailUrl, GitlabProjectHook[].class);
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("WeakerAccess") // API
    protected String projectUrl(String group, GitLabProjectSelector selector, GitLabProjectVisibility visibility, String searchPattern) {
        StringBuilder urlBuilder = new StringBuilder(GitlabGroup.URL).append(PATH_SEP).append(group).append(GitLabProject.URL);

        if (!VISIBLE.equals(selector)) {
            urlBuilder.append("?").append(selector.id()).append("=true");
        }

        if (!ALL.equals(visibility)) {
            urlBuilder.append(VISIBLE.equals(selector) ? "?" : "&").append("visibility=").append(visibility.id());
        }

        if (!StringUtils.isEmpty(searchPattern)) {
            urlBuilder.append(VISIBLE.equals(selector) && ALL.equals(visibility) ? "?" : "&").append("search=").append(searchPattern);
        }

        return urlBuilder.toString();
    }

    @SuppressWarnings("WeakerAccess") // API
    protected String projectUrl(GitLabProjectSelector selector, GitLabProjectVisibility visibility, String searchPattern) {
        StringBuilder urlBuilder = new StringBuilder(GitlabProject.URL)
                .append(PATH_SEP).append(selector.id());

        if (!ALL.equals(visibility)) {
            urlBuilder.append("?visibility=").append(visibility.id());
        }

        if (!StringUtils.isEmpty(searchPattern)) {
            urlBuilder.append(ALL.equals(visibility) ? "?" : "&").append("search=").append(searchPattern);
        }

        return urlBuilder.toString();
    }

    @SuppressWarnings("WeakerAccess") // API
    protected final String encode(String in) throws GitLabAPIException {
        try {
            return URLEncoder.encode(in, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new GitLabAPIException(e);
        }
    }

    private static final class VersionCache extends LinkedHashMap<String, String> {
        private final int maxSize;

        private VersionCache(int size) {
            super(size, 0.9f, true);
            maxSize = size;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > maxSize;
        }
    }
}
