package br.com.damsete.jpa.queries;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;

public class QuerySpecificationsBuilder<T> {

    private final List<Criteria> params = newArrayList();

    public final QuerySpecificationsBuilder<T> with(final String key, final String operation, final Object value,
                                                                                                  final String prefix, final String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public QuerySpecificationsBuilder<T> with(final String precedenceIndicator, final String key, final String operation,
                                                                                            final Object value, final String prefix, final String suffix) {
        var op = Operation.getSimpleOperation(operation.charAt(0));
        if (op != null) {
            if (op == Operation.EQUALITY) {
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
            this.params.add(new Criteria(precedenceIndicator, key, op, value));
        }
        return this;
    }

    public Specification<T> build(Function<Criteria, Specification<T>> converter) {
        if (this.params.isEmpty()) {
            return null;
        }

        final List<Specification<T>> specs = this.params.stream().map(converter)
                .collect(Collectors.toCollection(ArrayList::new));

        Specification<T> result = specs.get(0);
        for (int idx = 1; idx < specs.size(); idx++) {
            result = params.get(idx)
                    .isOrPredicate()
                    ? Specification.where(result)
                    .or(specs.get(idx))
                    : Specification.where(result)
                    .and(specs.get(idx));
        }
        return result;
    }

    public Specification<T> build(Deque<?> postFixedExprStack, Function<Criteria, Specification<T>> converter) {
        Deque<Specification<T>> specStack = newLinkedList();

        Collections.reverse((List<?>) postFixedExprStack);

        while (!postFixedExprStack.isEmpty()) {
            var mayBeOperand = postFixedExprStack.pop();
            if (!(mayBeOperand instanceof String)) {
                specStack.push(converter.apply((Criteria) mayBeOperand));
            } else {
                Specification<T> operand1 = specStack.pop();
                Specification<T> operand2 = specStack.pop();
                if (mayBeOperand.equals(Operation.AND_OPERATOR)) {
                    specStack.push(Specification.where(operand1).and(operand2));
                } else if (mayBeOperand.equals(Operation.OR_OPERATOR)) {
                    specStack.push(Specification.where(operand1).or(operand2));
                }
            }
        }
        return specStack.pop();
    }
}
