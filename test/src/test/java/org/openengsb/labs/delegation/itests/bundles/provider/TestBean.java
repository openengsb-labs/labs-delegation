package org.openengsb.labs.delegation.itests.bundles.provider;

import java.io.Serializable;

import org.openengsb.labs.delegation.service.Provide;

@Provide(context = { "foo", "bar" }, alias = "mtestbean")
public class TestBean implements Serializable {

    private static final long serialVersionUID = 8283996056794425281L;

    private String id;
    private ChildBean child = new ChildBean();

    public TestBean(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ChildBean getChild() {
        return child;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((child == null) ? 0 : child.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestBean other = (TestBean) obj;
        if (child == null) {
            if (other.child != null) {
                return false;
            }
        } else if (!child.equals(other.child)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
