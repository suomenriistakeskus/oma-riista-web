package fi.riista.util;

public class EnumUtils {


    /**
     * Maps value to other similar valued enumeration. Meant to be used with mapping enum values from/to
     * generated classes
     * @param clazz Class to convert the value to
     * @param value Value to be casted
     * @return Converted value. Null if null was passed in.
     */
    public static <A extends Enum<A>, B extends Enum<B>> B convertNullableByEnumName(final Class<B> clazz, final A value) {
        return value == null ? null : Enum.valueOf(clazz, value.name());
    }

    // Prevent instantiation
    private EnumUtils() {}
}
