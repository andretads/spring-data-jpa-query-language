package br.com.damsete.jpa.queries;

import org.springframework.data.jpa.domain.Specification;

public class QuerySpecificationResolver {

    private QuerySpecificationResolver() {
    }

    public static <T> Specification<T> resolver(String query) {
        if (query == null) {
            return null;
        }

        var parser = new CriteriaParser();
        QuerySpecificationsBuilder<T> specBuilder = new QuerySpecificationsBuilder<>();
        return specBuilder.build(parser.parse(query), QuerySpecification::new);
    }
}
