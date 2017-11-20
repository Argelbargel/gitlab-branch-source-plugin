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
import hudson.model.TopLevelItem;
import hudson.views.ViewJobFilter;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;


@SuppressWarnings("unused")
public class FilterGitLabSCMBranches extends AbstractGitLabSCMViewJobFilter {
    private static final Pattern DEFAULT_PATTERN = Pattern.compile(".*");

    private final boolean matchOnlyDefaultBranches;
    private final boolean matchOnlyBranchesWithMergeRequests;


    @DataBoundConstructor
    public FilterGitLabSCMBranches(String includeExcludeTypeString, boolean matchOnlyDefaultBranches, boolean matchOnlyBranchesWithMergeRequests) {
        this(includeExcludeTypeString, matchOnlyDefaultBranches, matchOnlyBranchesWithMergeRequests, DEFAULT_FINDER);
    }

    FilterGitLabSCMBranches(String includeExcludeTypeString, boolean matchOnlyDefaultBranches, boolean matchOnlyBranchesWithMergeRequests, GitLabSCMHeadFinder finder) {
        super(includeExcludeTypeString, finder);
        this.matchOnlyDefaultBranches = matchOnlyDefaultBranches;
        this.matchOnlyBranchesWithMergeRequests = matchOnlyBranchesWithMergeRequests;
    }

    public boolean getMatchOnlyBranchesWithMergeRequests() {
        return matchOnlyBranchesWithMergeRequests;
    }

    public boolean getMatchOnlyDefaultBranches() {
        return matchOnlyDefaultBranches;
    }


    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    protected boolean matches(TopLevelItem item, GitLabSCMHead head) {
        if (head == null || !GitLabSCMBranchHead.class.isInstance(head)) {
            return false;
        }

        if (matchOnlyDefaultBranches) {
            return isDefaultBranch(item);
        }

        if (matchOnlyBranchesWithMergeRequests) {
            return hasMergeRequest(head);
        }

        return true;
    }

    private boolean hasMergeRequest(GitLabSCMHead head) {
        return head instanceof GitLabSCMBranchHead  && ((GitLabSCMBranchHead) head).hasMergeRequest();
    }

    private boolean isDefaultBranch(Item item) {
        return item instanceof  Actionable && ((Actionable) item).getAction(PrimaryInstanceMetadataAction.class) != null;
    }

    private boolean filterBranches(GitLabSCMBranchHead head) {
        return matchOnlyBranchesWithMergeRequests || !head.hasMergeRequest();
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
