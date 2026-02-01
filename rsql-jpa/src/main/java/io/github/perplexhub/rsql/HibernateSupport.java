package io.github.perplexhub.rsql;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.util.ClassUtils;

final class HibernateSupport {

  private static final boolean isHibernatePresent = ClassUtils.isPresent(
      "org.hibernate.query.criteria.HibernateCriteriaBuilder", HibernateSupport.class.getClassLoader());

  private HibernateSupport() {
  }

  static boolean isHibernatePresent() {
    return isHibernatePresent;
  }

  static boolean isHibernateCriteriaBuilder(CriteriaBuilder cb) {
    return isHibernatePresent && cb instanceof HibernateCriteriaBuilder;
  }

  /**
   * Must be guarded with {@linkplain #isHibernatePresent} before invoking.
   */
  static Predicate ilike(CriteriaBuilder cb, Expression<String> expression, String arg, Character escapeChar) {
    var hcb = (HibernateCriteriaBuilder) cb;
    var pattern = '%' + arg + '%';

    return escapeChar != null
        ? hcb.ilike(expression, pattern, escapeChar)
        : hcb.ilike(expression, pattern);
  }

  /**
   * Casts the given expression to the specified type.
   * <p>
   * Since Hibernate 6.6, {@link Expression#as(Class)} no longer performs a real SQL CAST,
   * it only does an unsafe Java typecast on the Expression object itself.
   * To perform a genuine SQL type conversion, we must use {@link JpaExpression#cast(Class)}.
   * </p>
   *
   * @param expression the expression to cast
   * @param type       the target type
   * @param <T>        the target type
   * @return the casted expression
   * @see <a href="https://docs.hibernate.org/orm/6.6/migration-guide/migration-guide.html#criteria-query">Hibernate 6.6 Migration Guide</a>
   */
  @SuppressWarnings("unchecked")
  static <T> Expression<T> cast(Expression<?> expression, Class<T> type) {
    if (isHibernatePresent && expression instanceof JpaExpression<?> jpaExpression) {
      return jpaExpression.cast(type);
    }
    return (Expression<T>) expression.as(type);
  }
}
