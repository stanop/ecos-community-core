package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Full text search query builder
 * To create new query use FTSQuery.create()
 * To search by constructed query use method query(SearchService searchService)
 *
 * @author Pavel Simonov
 */
public class FTSQuery implements OperatorExpected, OperandExpected {

    private static final Log logger = LogFactory.getLog(FTSQuery.class);

    private static final String ISUNSET = "ISUNSET";
    private static final String ISNULL = "ISNULL";
    private static final String ISNOTNULL = "ISNOTNULL";
    private static final String NOT = "NOT";
    private static final String PARENT = "PARENT";
    private static final String TYPE = "TYPE";

    private QueryConsistency consistency = QueryConsistency.DEFAULT;
    private SearchParameters searchParameters = new SearchParameters();

    private Group group = new Group();

    private FTSQuery() {
    }

    /**
     * Create new query
     */
    public static OperandExpected create() {
        return new FTSQuery();
    }

    public OperatorExpected values(Map<QName, Serializable> values) {
        return values(values, BinOperator.AND, false);
    }

    public OperatorExpected values(Map<QName, Serializable> values, BinOperator joinOperator) {
        return values(values, joinOperator, false);
    }

    public OperatorExpected values(Map<QName, Serializable> values, BinOperator joinOperator, boolean exact) {
        int count = values.size();
        if (count == 0) {
            throw new IllegalArgumentException("Values is empty");
        }
        open();
        for (Map.Entry<QName, Serializable> entry : values.entrySet()) {
            value(entry.getKey(), entry.getValue(), exact);
            if (--count > 0) {
                group.setBiOperator(new BinOperatorTerm(joinOperator));
            }
        }
        close();
        return this;
    }

    public OperatorExpected any(QName field, Iterable<Serializable> values) {
        Iterator<Serializable> it = values.iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Values is empty");
        }
        open();
        while (it.hasNext()) {
            value(field, it.next());
            if (it.hasNext()) or();
        }
        close();
        return this;
    }

    public OperatorExpected empty(QName field) {
        return open().isNull(field).or().isUnset(field).close();
    }

    public OperatorExpected exact(QName field, Serializable value) {
        return value(field, value, true);
    }

    public OperatorExpected value(QName field, Serializable value) {
        return value(field, value, false);
    }

    public OperatorExpected value(QName field, Serializable value, boolean exact) {
        if (value == null) {
            empty(field);
        } else {
            ValueOperator valueOperator = new ValueOperator();
            valueOperator.field = field;
            valueOperator.value = value;
            valueOperator.exact = exact;
            group.addTerm(valueOperator);
        }
        return this;
    }

    public OperatorExpected isSet(QName field) {
        return isNotNull(field);
    }

    public OperatorExpected isUnset(QName field) {
        group.addTerm(new SysValueOperator(ISUNSET, field));
        return this;
    }

    public OperatorExpected isNull(QName field) {
        group.addTerm(new SysValueOperator(ISNULL, field));
        return this;
    }

    public OperatorExpected isNotNull(QName field) {
        group.addTerm(new SysValueOperator(ISNOTNULL, field));
        return this;
    }

    public OperatorExpected parent(NodeRef parent) {
        group.addTerm(new SysValueOperator(PARENT, parent));
        return this;
    }

    public OperatorExpected type(QName typeName) {
        group.addTerm(new SysValueOperator(TYPE, typeName));
        return this;
    }

    public OperandExpected or() {
        group.setBiOperator(new BinOperatorTerm(BinOperator.OR));
        return this;
    }

    public OperandExpected and() {
        group.setBiOperator(new BinOperatorTerm(BinOperator.AND));
        return this;
    }

    public OperandExpected not() {
        group.setUnOperator(new UnOperatorTerm(NOT));
        return this;
    }

    public OperandExpected open() {
        group.startGroup();
        return this;
    }

    public OperatorExpected close() {
        group.stopGroup();
        return this;
    }

    public FTSQuery transactional() {
        consistency = QueryConsistency.TRANSACTIONAL;
        return this;
    }


    public FTSQuery eventual() {
        consistency = QueryConsistency.EVENTUAL;
        return this;
    }

    public Optional<NodeRef> queryOne(SearchService searchService) {
        return query(searchService).stream().findFirst();
    }

    public String getQuery() {
        return group.getQuery();
    }

    public List<NodeRef> query(SearchService searchService) {

        String query = group.getQuery();

        if (logger.isDebugEnabled()) {
            logger.debug("FTSQuery: " + query);
        }

        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQuery(query);

        ResultSet resultSet = null;

        try {
            resultSet = searchService.query(searchParameters);
            return resultSet.getNodeRefs();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FTSQuery query = (FTSQuery) o;

        return consistency == query.consistency && group.equals(query.group);
    }

    @Override
    public int hashCode() {
        int result = consistency.hashCode();
        result = 31 * result + group.hashCode();
        return result;
    }

    private interface Term {
        void toString(StringBuilder builder);
    }

    private interface Operand extends Term {
    }

    private class UnOperatorTerm implements Term {

        String operator;
        Term term;

        UnOperatorTerm(String operator) {
            this.operator = operator;
        }

        public void toString(StringBuilder builder) {
            builder.append(operator).append(' ');
            term.toString(builder);
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UnOperatorTerm that = (UnOperatorTerm) o;

            return operator.equals(that.operator) && term.equals(that.term);
        }

        @Override
        public int hashCode() {
            int result = operator.hashCode();
            result = 31 * result + term.hashCode();
            return result;
        }
    }

    private class BinOperatorTerm implements Term {

        Term term0;
        Term term1;
        BinOperator operator;

        BinOperatorTerm(BinOperator operator) {
            this.operator = operator;
        }

        @Override
        public void toString(StringBuilder builder) {
            term0.toString(builder);
            builder.append(' ').append(operator).append(' ');
            term1.toString(builder);
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BinOperatorTerm operator1 = (BinOperatorTerm) o;

            return term0.equals(operator1.term0) &&
                   term1.equals(operator1.term1) &&
                   operator.equals(operator1.operator);
        }

        @Override
        public int hashCode() {
            int result = term0.hashCode();
            result = 31 * result + term1.hashCode();
            result = 31 * result + operator.hashCode();
            return result;
        }
    }

    private class SysValueOperator implements Operand {

        String field;
        Serializable value;

        SysValueOperator(String field, Serializable value) {
            this.field = field;
            this.value = value;
        }

        @Override
        public void toString(StringBuilder builder) {
            builder.append(field).append(":\"").append(value).append('\"');
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SysValueOperator that = (SysValueOperator) o;

            return (field != null ? field.equals(that.field) : that.field == null) &&
                   (value != null ? value.equals(that.value) : that.value == null);
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    private class ValueOperator implements Operand {

        QName field;
        Serializable value;
        boolean exact = false;

        @Override
        public void toString(StringBuilder builder) {
            char prefix = exact || consistency.equals(QueryConsistency.TRANSACTIONAL) ? '=' : '@';
            builder.append(prefix).append(field).append(":\"").append(value).append('\"');
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ValueOperator operator = (ValueOperator) o;

            return exact == operator.exact &&
                   (field != null ? field.equals(operator.field) : operator.field == null) &&
                   (value != null ? value.equals(operator.value) : operator.value == null);
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (exact ? 1 : 0);
            return result;
        }
    }

    private class Group implements Operand {

        UnOperatorTerm unOperator = null;
        BinOperatorTerm biOperator = null;
        Group group = null;
        Term term = null;

        private String query = null;
        private int hash = 0;

        void startGroup() {
            if (group != null) {
                group.startGroup();
            } else {
                Group group = new Group();
                addTerm(group);
                this.group = group;
            }
            query = null;
        }

        void stopGroup() {
            if (group == null) {
                throw new IllegalStateException("Bracket not open");
            }
            Group finalGroup = this;
            while (finalGroup.group.group != null) {
                finalGroup = finalGroup.group;
            }
            finalGroup.group = null;
            query = null;
        }

        void setUnOperator(UnOperatorTerm operator) {
            if (group != null) {
                group.setUnOperator(operator);
            } else {
                unOperator = operator;
            }
            query = null;
        }

        void setBiOperator(BinOperatorTerm operator) {
            if (group != null) {
                group.setBiOperator(operator);
            } else {
                if (term == null) {
                    throw new IllegalStateException("Binary operator can't be used without left operand");
                }
                biOperator = operator;
                biOperator.term0 = term;
                term = null;
            }
            query = null;
        }

        void addTerm(Term term) {
            if (group != null) {
                group.addTerm(term);
            } else {
                Term result = term;
                if (unOperator != null) {
                    unOperator.term = result;
                    result = unOperator;
                    unOperator = null;
                }
                if (biOperator != null) {
                    biOperator.term1 = result;
                    result = biOperator;
                    biOperator = null;
                } else if (this.term != null) {
                    throw new IllegalStateException("Search query building error. You should specify binary operator to combine two terms");
                }
                this.term = result;
            }
            query = null;
        }

        public String getQuery() {

            if (query != null) {
                return query;
            }

            StringBuilder sb = new StringBuilder();
            term.toString(sb);
            query = sb.toString();

            return query;
        }

        @Override
        public void toString(StringBuilder builder) {
            if (term instanceof Operand) {
                term.toString(builder);
            } else {
                builder.append('(');
                term.toString(builder);
                builder.append(')');
            }
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Group group = (Group) o;

            return term.equals(group.term);
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = term.hashCode();
            }
            return hash;
        }
    }
}
