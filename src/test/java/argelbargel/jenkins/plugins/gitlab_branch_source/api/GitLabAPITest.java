package argelbargel.jenkins.plugins.gitlab_branch_source.api;


import org.gitlab.api.GitlabAPI;
import org.gitlab.api.http.GitlabHTTPRequestor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({GitlabAPI.class})
public class GitLabAPITest {
    @Test
    public void testConnectV3() throws IOException {
        mockDelegate("/v3", "9.4.9");
        GitLabAPI api = GitLabAPI.connect("/v3", "");
        assertTrue(api instanceof GitLabAPIV3);
        assertEquals(3, api.getVersion());
        assertEquals("9.4.9", api.getServerVersion());
    }

    @Test
    public void testConnectV4() throws IOException {
        mockDelegate("/v4", "9.5.1");
        GitLabAPI api = GitLabAPI.connect("/v4", "");
        assertTrue(api instanceof GitLabAPIV4);
        assertEquals(4, api.getVersion());
        assertEquals("9.5.1", api.getServerVersion());
    }


    private void mockDelegate(String url, String version) throws IOException {
        GitlabHTTPRequestor requestor = mock(GitlabHTTPRequestor.class);
        when(requestor.to("/version", GitLabVersion.class)).thenReturn(new GitLabVersion(version, ""));
        GitlabAPI delegate = mock(GitlabAPI.class);
        when(delegate.retrieve()).thenReturn(requestor);
        mockStatic(GitlabAPI.class);
        when(GitlabAPI.connect(url, "")).thenReturn(delegate);
    }
}