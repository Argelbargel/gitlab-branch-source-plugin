/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package argelbargel.jenkins.plugins.gitlab_branch_source.views;


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMMergeRequestHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.Messages;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.views.ViewJobFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.ADD;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.NONE;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.REMOVE;


@SuppressWarnings("unused")
public class FilterGitLabSCMMergeRequests extends AbstractGitLabSCMViewJobFilter {
    private boolean showOnlyMergeRequestsFromOrigin = false;

    @DataBoundConstructor
    public FilterGitLabSCMMergeRequests() {
        super(DEFAULT_FINDER);
    }

    FilterGitLabSCMMergeRequests(GitLabSCMHeadFinder finder) {
        super(finder);
    }

    @DataBoundSetter
    public void setShowOnlyMergeRequestsFromOrigin(boolean value) {
        showOnlyMergeRequestsFromOrigin = value;
    }

    public boolean getShowOnlyMergeRequestsFromOrigin() {
        return showOnlyMergeRequestsFromOrigin;
    }

    @Override
    protected FilterMode filter(Item item, GitLabSCMHead head) {
        // ignore everything that's not a GitLabSCMMergeRequest
        if (head == null || !GitLabSCMMergeRequestHead.class.isInstance(head)) {
            return NONE;
        }

        return (showOnlyMergeRequestsFromOrigin && !((GitLabSCMMergeRequestHead) head).fromOrigin()) ? REMOVE : NONE;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<ViewJobFilter> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ViewJobFilter_FilterMergeRequests_DisplayName();
        }
    }
}
