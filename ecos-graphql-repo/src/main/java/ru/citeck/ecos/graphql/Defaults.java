package ru.citeck.ecos.graphql;

import java.util.function.Supplier;

public class Defaults {

    public static class IntZero implements Supplier<Object> {
        @Override
        public Object get() {
            return 0;
        }
    }

}
