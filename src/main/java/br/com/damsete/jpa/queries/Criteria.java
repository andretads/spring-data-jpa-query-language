package br.com.damsete.jpa.queries;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Criteria {

    private final String key;
    private final Operation operation;
    private final Object value;
    private final boolean orPredicate;

    public Criteria(String orPredicate, String key, Operation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        this.orPredicate = orPredicate != null && orPredicate.equals(Operation.OR_PREDICATE_FLAG);
    }

    public Criteria(String key, String operation, String prefix, String value, String suffix) {
        var op = Operation.getSimpleOperation(operation.charAt(0));
        if (op == Operation.EQUALITY) { // the operation may be complex operation
            final var startWithAsterisk = prefix != null && prefix.contains(Operation.ZERO_OR_MORE_REGEX);
            final var endWithAsterisk = suffix != null && suffix.contains(Operation.ZERO_OR_MORE_REGEX);

            if (startWithAsterisk && endWithAsterisk) {
                op = Operation.CONTAINS;
            } else if (startWithAsterisk) {
                op = Operation.ENDS_WITH;
            } else if (endWithAsterisk) {
                op = Operation.STARTS_WITH;
            }
        }
        this.key = key;
        this.operation = op;
        this.value = value;
        this.orPredicate = false;
    }

    public String getKey() {
        return key;
    }

    public Operation getOperation() {
        return operation;
    }

    public Object getValue() {
        return value;
    }

    public boolean isOrPredicate() {
        return orPredicate;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
