package fi.riista.config.jpa;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.DoubleType;
import org.hibernate.type.Type;

import java.util.List;

public class PostgreSQLDistanceFunction implements SQLFunction {

    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("The function must be passed 2 arguments");
        }

        String field = (String) arguments.get(0);
        String value = (String) arguments.get(1);

        return field + " <-> " + value;
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) {
        return new DoubleType();
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }
}
