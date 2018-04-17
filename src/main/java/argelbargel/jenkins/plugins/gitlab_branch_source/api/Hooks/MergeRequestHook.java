package argelbargel.jenkins.plugins.gitlab_branch_source.api.Hooks;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class MergeRequestHook extends WebHook {

    private MergeRequestObjectAttributes objectAttributes;

    public MergeRequestObjectAttributes getObjectAttributes() {
        return objectAttributes;
    }

    public void setObjectAttributes(MergeRequestObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MergeRequestHook that = (MergeRequestHook) o;
        return new EqualsBuilder()
                .append(objectAttributes, that.objectAttributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)

                .append(objectAttributes)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("objectAttributes", objectAttributes)
                .toString();
    }
}