package argelbargel.jenkins.plugins.gitlab_branch_source.api;


import com.vdurmont.semver4j.Semver;
import org.gitlab.api.GitlabAPI;

import java.util.NoSuchElementException;

import static com.vdurmont.semver4j.Semver.SemverType.LOOSE;
import static java.util.Arrays.stream;
import static org.apache.commons.lang.StringUtils.isNotEmpty;


enum GitLabAPIImplVersion {
    V3(3, "4", "9.5", GitLabAPIV3.class),
    V4(4, "9.5", "", GitLabAPIV4.class);

    public static GitLabAPIImplVersion byServerVersion(String version) {
        return stream(values())
                .filter(v -> v.isSupported(version))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("no version of the found which supports server-version " + version));
    }

    private int version;
    private final Semver minVersion;
    private final Semver maxVersion;
    private final Class<? extends GitLabAPI> implClass;

    GitLabAPIImplVersion(int version, String min, String max, Class<? extends GitLabAPI> impl) {
        this.version = version;
        minVersion = new Semver(min, LOOSE);
        maxVersion = (isNotEmpty(max)) ? new Semver(max, LOOSE) : null;
        implClass = impl;
    }

    boolean isSupported(String other) {
        return !minVersion.isGreaterThan(other) && (maxVersion == null || maxVersion.isGreaterThan(other));
    }

    GitLabAPI create(GitlabAPI delegate, String serverVersion) throws GitLabAPIException {
        try {
            return implClass
                    .getDeclaredConstructor(GitlabAPI.class, int.class, String.class)
                    .newInstance(delegate, version, serverVersion);
        } catch (Exception e) {
            throw new GitLabAPIException("could not create implementation-instance", e);
        }
    }
}
