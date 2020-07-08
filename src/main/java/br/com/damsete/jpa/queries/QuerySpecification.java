package br.com.damsete.jpa.queries;

import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class QuerySpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    private final transient Criteria criteria;

    public QuerySpecification(Criteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(@Nonnull Root<T> root, @Nonnull CriteriaQuery<?> criteriaQuery,
                                 @Nonnull CriteriaBuilder criteriaBuilder) {
        switch (this.criteria.getOperation()) {
            case EQUALITY:
                return criteriaBuilder.equal(root.get(this.criteria.getKey()), this.criteria.getValue());
            case NEGATION:
                return criteriaBuilder.notEqual(root.get(this.criteria.getKey()), this.criteria.getValue());
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(root.get(this.criteria.getKey()), this.criteria.getValue().toString());
            case LESS_THAN:
                return criteriaBuilder.lessThan(root.get(this.criteria.getKey()), this.criteria.getValue().toString());
            case LIKE:
                return criteriaBuilder.like(criteriaBuilder.upper(root.get(this.criteria.getKey())),
                        this.criteria.getValue().toString().toUpperCase());
            case STARTS_WITH:
                return criteriaBuilder.like(criteriaBuilder.upper(root.get(this.criteria.getKey())),
                        this.criteria.getValue().toString().toUpperCase() + "%");
            case ENDS_WITH:
                return criteriaBuilder.like(criteriaBuilder.upper(root.get(this.criteria.getKey())),
                        "%" + this.criteria.getValue().toString().toUpperCase());
            case CONTAINS:
                return criteriaBuilder.like(criteriaBuilder.upper(root.get(this.criteria.getKey())),
                        "%" + this.criteria.getValue().toString().toUpperCase() + "%");
            default:
                return null;
        }
    }
}
