package fi.riista.util.jpa;

public class IntArrayUserType extends ArrayUserType<Integer> {
    @Override
    public Class<Integer> returnedClass() {
        return Integer.class;
    }

    @Override
    protected String arraySqlType() {
        return "INTEGER";
    }
}
