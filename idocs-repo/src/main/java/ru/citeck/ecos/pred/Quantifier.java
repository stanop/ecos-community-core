package ru.citeck.ecos.pred;

import java.util.Collection;

public enum Quantifier {
    EXACTLY_ZERO {
        @Override
        public boolean evaluate(Collection<?> matches) {
            return matches.size() == 0;
        }
    },
    
    EXACTLY_ONE {
        @Override
        public boolean evaluate(Collection<?> matches) {
            return matches.size() == 1;
        }
    },
    
    EXISTS {
        @Override
        public boolean evaluate(Collection<?> matches) {
            return matches.size() >= 1;
        }
    },
    
    SINGLE {
        @Override
        public boolean evaluate(Collection<?> matches) {
            return matches.size() <= 1;
        }
    },

    OPTIONAL {
        @Override
        public boolean evaluate(Collection<?> matches) {
            return matches.size() >= 0;
        }
    };
    
    public abstract boolean evaluate(Collection<?> matches);
}