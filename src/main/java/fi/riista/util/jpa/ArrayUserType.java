package fi.riista.util.jpa;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public abstract class ArrayUserType<T> implements UserType {
    protected static final int[] SQL_TYPES = {Types.ARRAY};

    @Override
    public final int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public abstract Class<T> returnedClass();

    protected abstract String arraySqlType();

    @Override
    public Object nullSafeGet(final ResultSet resultSet,
                              final String[] names,
                              final SharedSessionContractImplementor sharedSessionContractImplementor,
                              final Object owner) throws HibernateException, SQLException {
        if (resultSet.wasNull()) {
            return null;
        }

        final Array array = resultSet.getArray(names[0]);

        return array.getArray();
    }

    @Override
    public void nullSafeSet(final PreparedStatement statement,
                            final Object value,
                            final int index,
                            final SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        if (value == null) {
            statement.setNull(index, SQL_TYPES[0]);
        } else {
            final Connection connection = statement.getConnection();

            final T[] castObject = (T[]) value;
            final Array array = connection.createArrayOf(arraySqlType(), castObject);

            statement.setArray(index, array);
        }
    }

    @Override
    public final Object deepCopy(final Object value) {
        return value;
    }

    @Override
    public final boolean isMutable() {
        return false;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    @Override
    public final Serializable disassemble(final Object value) {
        return (Serializable) value;
    }

    @Override
    public final boolean equals(final Object x, final Object y) {
        return Objects.equals(x, y);
    }

    @Override
    public final int hashCode(final Object x) {
        return x.hashCode();
    }

    @Override
    public final Object replace(
            final Object original,
            final Object target,
            final Object owner) {
        return original;
    }
}
