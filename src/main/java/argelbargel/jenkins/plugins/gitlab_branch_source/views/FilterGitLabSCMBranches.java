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


import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMBranchHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.GitLabSCMHead;
import argelbargel.jenkins.plugins.gitlab_branch_source.Messages;
import hudson.Extension;
import hudson.model.Actionable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.views.ViewJobFilter;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.NONE;
import static argelbargel.jenkins.plugins.gitlab_branch_source.views.AbstractGitLabSCMViewJobFilter.FilterMode.REMOVE;


@SuppressWarnings("unused")
public class FilterGitLabSCMBranches extends AbstractGitLabSCMViewJobFilter {
    private static final Pattern DEFAULT_PATTERN = Pattern.compile(".*");

    private boolean showOnlyDefaultBranches = false;
    private boolean showBranchesWithMergeRequests = true;
    private Pattern branchNamePattern = DEFAULT_PATTERN;

    @DataBoundConstructor
    public FilterGitLabSCMBranches() {
        super(DEFAULT_FINDER);
    }

    FilterGitLabSCMBranches(GitLabSCMHeadFinder finder) {
        super(finder);
    }

    @DataBoundSetter
    public void setBranchNamePattern(String pattern) {
        branchNamePattern = StringUtils.isNotBlank(pattern) ? Pattern.compile(pattern) : DEFAULT_PATTERN;
    }

    public String getBranchNamePattern() {
        return branchNamePattern.pattern();
    }

    @DataBoundSetter
    public void setShowBranchesWithMergeRequests(boolean value) {
        showBranchesWithMergeRequests = value;
    }

    public boolean getShowBranchesWithMergeRequests() {
        return showBranchesWithMergeRequests;
    }

    @DataBoundSetter
    public void setShowOnlyDefaultBranches(boolean value) {
        showOnlyDefaultBranches = value;
    }

    public boolean getShowOnlyDefaultBranches() {
        return showOnlyDefaultBranches;
    }


    @Override
    protected FilterMode filter(Item item, GitLabSCMHead head) {
        // ignore everything that's not a GitLabSCMBranch
        if (head == null || !GitLabSCMBranchHead.class.isInstance(head)) {
            return NONE;
        }

        return filter(item, (GitLabSCMBranchHead) head);

    }

    private FilterMode filter(Item item, GitLabSCMBranchHead head) {
        if (showOnlyDefaultBranches && item instanceof Actionable  && !isDefaultBranch((Actionable)item)) {
            return REMOVE;
        }

        if (!showBranchesWithMergeRequests && head.hasMergeRequest()) {
            return REMOVE;
        }

        if (!branchNamePattern.matcher(head.getName()).matches()) {
            return REMOVE;
        }

        return NONE;
    }


    private boolean isDefaultBranch(Actionable item) {
        return item.getAction(PrimaryInstanceMetadataAction.class) != null;
    }

    private boolean filterBranches(GitLabSCMBranchHead head) {
        return showBranchesWithMergeRequests || !head.hasMergeRequest();
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<ViewJobFilter> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ViewJobFilter_FilterBranches_DisplayName();
        }
    }
}
