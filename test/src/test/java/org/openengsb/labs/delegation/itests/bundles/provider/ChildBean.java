package org.openengsb.labs.delegation.itests.bundles.provider;

import java.io.Serializable;

import org.openengsb.labs.delegation.service.Provide;

@Provide(context = "bar")
public class ChildBean implements Serializable {

    private static final long serialVersionUID = 1590509354350141448L;
    private static int count = 0;

    protected int id = ++count;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
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
        ChildBean other = (ChildBean) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

}
