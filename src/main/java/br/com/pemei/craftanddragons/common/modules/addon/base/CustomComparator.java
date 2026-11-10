package br.com.pemei.craftanddragons.common.modules.addon.base;


public enum CustomComparator {
    EQUAL {
        @Override
        public <T extends Number & Comparable<T>> boolean compare(T n1, T n2) {
            if (n1 == null || n2 == null) return n1 == n2;
            return n1.compareTo(n2) == 0;
        }
    },
    NOT_EQUAL {
        @Override
        public <T extends Number & Comparable<T>> boolean compare(T n1, T n2) {
            if (n1 == null || n2 == null) return n1 != n2;
            return n1.compareTo(n2) != 0;
        }
    },
    LESS {
        @Override
        public <T extends Number & Comparable<T>> boolean compare(T n1, T n2) {
            if (n1 == null || n2 == null) return false;
            return n1.compareTo(n2) < 0;
        }
    },
    LESS_OR_EQUAL {
        @Override
        public <T extends Number & Comparable<T>> boolean compare(T n1, T n2) {
            if (n1 == null || n2 == null) return n1 == n2;
            return n1.compareTo(n2) <= 0;
        }
    },
    GREATER {
        @Override
        public <T extends Number & Comparable<T>> boolean compare(T n1, T n2) {
            if (n1 == null || n2 == null) return false;
            return n1.compareTo(n2) > 0;
        }
    },
    GREATER_OR_EQUAL {
        @Override
        public <T extends Number & Comparable<T>> boolean compare(T n1, T n2) {
            if (n1 == null || n2 == null) return n1 == n2;
            return n1.compareTo(n2) >= 0;
        }
    };

    public abstract <T extends Number & Comparable<T>> boolean compare(T n1, T n2);
}
