package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.Locale;
import java.util.Set;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

/**
 * Liquibase owns users, items and item_permissions.
 * Hibernate schema update may still create Envers tables.
 * Public, because Hibernate instantiates this provider by class name.
 */
public class LiquibaseTablesFilterProvider implements SchemaFilterProvider {

    private static final Set<String> OWNED_BY_LIQUIBASE = Set.of(
            "users",
            "items",
            "item_permissions"
    );

    private static final SchemaFilter FILTER = new SchemaFilter() {

        @Override
        public boolean includeNamespace(Namespace namespace) {
            return true;
        }

        @Override
        public boolean includeTable(Table table) {
            String name = table.getName();
            if (name == null) {
                return true;
            }
            return !OWNED_BY_LIQUIBASE.contains(name.toLowerCase(Locale.ROOT));
        }

        @Override
        public boolean includeSequence(Sequence sequence) {
            return true;
        }
    };

    @Override
    public SchemaFilter getCreateFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getDropFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getTruncatorFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getMigrateFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getValidateFilter() {
        return SchemaFilter.ALL;
    }
}
