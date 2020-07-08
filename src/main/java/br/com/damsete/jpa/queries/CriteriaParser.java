package br.com.damsete.jpa.queries;

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newLinkedList;

public class CriteriaParser {

    private static final Map<String, Operator> ops = Map.of("AND", Operator.AND, "OR", Operator.OR, "or",
            Operator.OR, "and", Operator.AND);

    private static final Pattern SpecCriteraRegex = Pattern.compile("^(\\w+?)(" + Joiner.on("|")
            .join(Operation.SIMPLE_OPERATION_SET) + ")(\\p{Punct}?)(.*?)(\\p{Punct}?)$");

    private static boolean isHigerPrecedenceOperator(String currOp, String prevOp) {
        return ops.containsKey(prevOp) && ops.get(prevOp).precedence >= ops.get(currOp).precedence;
    }

    public Deque<Object> parse(String query) {
        Deque<Object> output = newLinkedList();
        Deque<String> stack = newLinkedList();

        Arrays.stream(query.split("\\s+")).forEach(token -> {
            if (ops.containsKey(token)) {
                handlerOperation(output, stack, token);
            } else if (token.equals(Operation.LEFT_PARANTHESIS)) {
                stack.push(Operation.LEFT_PARANTHESIS);
            } else if (token.equals(Operation.RIGHT_PARANTHESIS)) {
                while (stack.peek() != null && !stack.peek().equals(Operation.LEFT_PARANTHESIS)) {
                    output.push(stack.pop());
                }
                stack.pop();
            } else {
                handlerRegex(output, token);
            }
        });

        while (!stack.isEmpty()) {
            output.push(stack.pop());
        }

        return output;
    }

    private enum Operator {
        OR(1), AND(2);

        final int precedence;

        Operator(int p) {
            precedence = p;
        }
    }

    private void handlerOperation(Deque<Object> output, Deque<String> stack, String token) {
        while (!stack.isEmpty() && isHigerPrecedenceOperator(token, stack.peek())) {
            output.push(stack.pop().equalsIgnoreCase(Operation.OR_OPERATOR)
                    ? Operation.OR_OPERATOR : Operation.AND_OPERATOR);
        }
        stack.push(token.equalsIgnoreCase(Operation.OR_OPERATOR)
                ? Operation.OR_OPERATOR : Operation.AND_OPERATOR);
    }

    private void handlerRegex(Deque<Object> output, String token) {
        var matcher = SpecCriteraRegex.matcher(token);
        while (matcher.find()) {
            output.push(new Criteria(matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(4), matcher.group(5)));
        }
    }
}
