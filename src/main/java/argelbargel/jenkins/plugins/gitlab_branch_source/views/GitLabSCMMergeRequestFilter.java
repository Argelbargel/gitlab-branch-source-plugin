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


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMMergeRequestHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.Messages;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;
import hudson.views.ViewJobFilter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


@SuppressWarnings("unused")
public final class GitLabSCMMergeRequestFilter extends AbstractGitLabSCMViewJobFilter<GitLabSCMMergeRequestHead> {
    private final boolean matchOnlyMergeRequestsFromOrigin;

    @DataBoundConstructor
    public GitLabSCMMergeRequestFilter(String includeExcludeTypeString, boolean matchOnlyMergeRequestsFromOrigin) {
        this(includeExcludeTypeString, matchOnlyMergeRequestsFromOrigin, DEFAULT_FINDER);
    }

    GitLabSCMMergeRequestFilter(String includeExcludeTypeString, boolean matchOnlyMergeRequestsFromOrigin, GitLabSCMHeadFinder finder) {
        super(includeExcludeTypeString, finder, GitLabSCMMergeRequestHead.class);
        this.matchOnlyMergeRequestsFromOrigin = matchOnlyMergeRequestsFromOrigin;
    }

    public boolean getMatchOnlyMergeRequestsFromOrigin() {
        return matchOnlyMergeRequestsFromOrigin;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    protected boolean matches(TopLevelItem item, GitLabSCMMergeRequestHead head) {
        return !matchOnlyMergeRequestsFromOrigin || head.fromOrigin();
    }


    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<ViewJobFilter> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.MergeRequestFilter_DisplayName();
        }
    }
}
