/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.vdkcr.server.fast;

/**
 *
 * @author Administrator
 */
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <P>
 * Enum class for types of Stages to take on a document eror. Its String representation is the same
 * as the name of the static final member.
 * </P>
 *
 * <P>
 * See Joshua Bloch, "Effective Java", Item 21: "Replace enum constructs with classes", pp. 104-114
 * </P>
 *
 */
@SuppressWarnings("serial")
public final class Stage implements Serializable {

    private static int nextOrdinal = 0;

    private final int ordinal = nextOrdinal++;

    private final String name;

    private Stage(String name) {
        this.name = name;
    }

    public final String toString() {
        return this.name;
    }

    public final boolean equals(Object that) {
        return super.equals(that);
    }

    public final int hashCode() {
        return super.hashCode();
    }

    public final static Stage COMPLETED = new Stage("COMPLETED");

    public final static Stage SECURED = new Stage("SECURED");

    public final static Stage LOST = new Stage("LOST");

    private final static Stage[] PRIVATE_VALUES = new Stage[] { COMPLETED, SECURED, LOST };

    /**
     * An unmodifiable List of types.
     */
    public final static List<Stage> VALUES = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));

    Object readResolve() {
        return PRIVATE_VALUES[this.ordinal];
    }
}
