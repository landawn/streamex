/*
 * Copyright (C) 2017 HaiYang Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package one.util.streamex;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import one.util.streamex.function.IndexedBiConsumer;
import one.util.streamex.function.IndexedBiFunction;
import one.util.streamex.function.IndexedBiPredicate;
import one.util.streamex.function.IndexedConsumer;
import one.util.streamex.function.IndexedFunction;
import one.util.streamex.function.IndexedPredicate;
import one.util.streamex.function.TriPredicate;

/**
 * Factory utility class for functional interfaces.
 * 
 * @author haiyang li
 * 
 * @since 0.8.5
 */
@SuppressWarnings("unchecked")
public final class Fn {

    @SuppressWarnings("rawtypes")
    public static final IntFunction<Map<String, Object>> FACTORY_OF_MAP = (IntFunction) Factory.MAP_FACTORY;
    @SuppressWarnings("rawtypes")
    public static final IntFunction<LinkedHashMap<String, Object>> FACTORY_OF_LINKED_HASH_MAP = (IntFunction) Factory.LINKED_HASH_MAP_FACTORY;
    @SuppressWarnings("rawtypes")
    public static final Supplier<Map<String, Object>> SUPPLIER_OF_MAP = (Supplier) Suppliers.MAP;
    @SuppressWarnings("rawtypes")
    public static final Supplier<LinkedHashMap<String, Object>> SUPPLIER_OF_LINKED_HASH_MAP = (Supplier) Suppliers.LINKED_HASH_MAP;

    private static final Runnable EMPTY_ACTION = new Runnable() {
        @Override
        public void run() {
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Consumer DO_NOTHING = new Consumer() {
        @Override
        public void accept(Object value) {
            // do nothing.
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Consumer PRINTLN = new Consumer() {
        @Override
        public void accept(Object value) {
            System.out.println(value);
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Function IDENTITY = Function.identity();

    private static final Function<Map.Entry<Object, Object>, Object> KEY = new Function<Map.Entry<Object, Object>, Object>() {
        @Override
        public Object apply(Map.Entry<Object, Object> t) {
            return t.getKey();
        }
    };

    private static final Function<Map.Entry<Object, Object>, Object> VALUE = new Function<Map.Entry<Object, Object>, Object>() {
        @Override
        public Object apply(Map.Entry<Object, Object> t) {
            return t.getValue();
        }
    };

    private static final BiFunction<Object, Object, Map.Entry<Object, Object>> ENTRY = new BiFunction<Object, Object, Map.Entry<Object, Object>>() {
        @Override
        public Map.Entry<Object, Object> apply(Object key, Object value) {
            return new AbstractMap.SimpleImmutableEntry<>(key, value);
        }
    };

    private static final Function<String, String> TRIM = new Function<String, String>() {
        @Override
        public String apply(String t) {
            return t == null ? null : t.trim();
        }
    };

    private static final Function<String, String> TRIM_TO_EMPTY = new Function<String, String>() {
        @Override
        public String apply(String t) {
            return t == null ? "" : t.trim();
        }
    };

    private static final Function<String, String> TRIM_TO_NULL = new Function<String, String>() {
        @Override
        public String apply(String t) {
            if (t == null || (t = t.trim()).length() == 0) {
                return null;
            }

            return t;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Predicate ALWAYS_TRUE = new Predicate() {
        @Override
        public boolean test(Object value) {
            return true;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Predicate ALWAYS_FALSE = new Predicate() {
        @Override
        public boolean test(Object value) {
            return false;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Predicate IS_NULL = new Predicate() {
        @Override
        public boolean test(Object value) {
            return value == null;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Predicate NOT_NULL = new Predicate() {
        @Override
        public boolean test(Object value) {
            return value != null;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final BinaryOperator THROWING_MERGER = new BinaryOperator() {
        @Override
        public Object apply(Object t, Object u) {
            throw new IllegalStateException(String.format(
                "Duplicate entry for key (attempt to merge values '%s' and '%s')", t, u));
        }
    };

    @SuppressWarnings("rawtypes")
    private static final BinaryOperator IGNORING_MERGER = new BinaryOperator() {
        @Override
        public Object apply(Object t, Object u) {
            return t;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final BinaryOperator REPLACING_MERGER = new BinaryOperator() {
        @Override
        public Object apply(Object t, Object u) {
            return u;
        }
    };

    private Fn() {
        // Singleton.
    }

    public static <T> Comparator<T> naturalOrder() {
        return Comparators.naturalOrder();
    }

    public static <T> Comparator<T> reversedOrder() {
        return Comparators.reversedOrder();
    }

    public static <T> Comparator<T> reversedOrder(final Comparator<T> cmp) {
        return Comparators.reversedOrder(cmp);
    }

    @SuppressWarnings("rawtypes")
    public static <T, U extends Comparable> Comparator<T> comparingBy(
            final Function<? super T, ? extends U> keyExtractor) {
        return Comparators.comparingBy(keyExtractor);
    }

    @SuppressWarnings("rawtypes")
    public static <T, U extends Comparable> Comparator<T> reversedComparingBy(
            final Function<? super T, ? extends U> keyExtractor) {
        return Comparators.reversedComparingBy(keyExtractor);
    }

    public static Runnable emptyAction() {
        return EMPTY_ACTION;
    }

    public static <T> Consumer<T> doNothing() {
        return DO_NOTHING;
    }

    public static <T> Consumer<T> println() {
        return PRINTLN;
    }

    public static <T, U> BiConsumer<T, U> println(final String separator) {
        return new BiConsumer<T, U>() {
            @Override
            public void accept(T t, U u) {
                System.out.println(t + separator + u);
            }
        };
    }

    public static <T> Function<T, T> identity() {
        return IDENTITY;
    }

    @SuppressWarnings("rawtypes")
    public static <K, V> Function<Entry<K, V>, K> key() {
        return (Function) KEY;
    }

    @SuppressWarnings("rawtypes")
    public static <K, V> Function<Entry<K, V>, V> value() {
        return (Function) VALUE;
    }

    @SuppressWarnings("rawtypes")
    public static <K, V> BiFunction<K, V, Map.Entry<K, V>> entry() {
        return (BiFunction) ENTRY;
    }

    public static Function<String, String> trim() {
        return TRIM;
    }

    public static Function<String, String> trimToEmpty() {
        return TRIM_TO_EMPTY;
    }

    public static Function<String, String> trimToNull() {
        return TRIM_TO_NULL;
    }

    public static <T, U> Function<T, U> cast(final Class<U> clazz) {
        return new Function<T, U>() {
            @Override
            public U apply(T t) {
                return (U) t;
            }
        };
    }

    public static <T> Predicate<T> alwaysTrue() {
        return ALWAYS_TRUE;
    }

    public static <T> Predicate<T> alwaysFalse() {
        return ALWAYS_FALSE;
    }

    public static <T> Predicate<T> isNull() {
        return IS_NULL;
    }

    public static <T> Predicate<T> notNull() {
        return NOT_NULL;
    }

    public static <T> Predicate<T> equal(final Object target) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return value == target || (value != null && value.equals(target));
            }
        };
    }

    public static <T> Predicate<T> notEqual(final Object target) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return !(value == target || (value != null && value.equals(target)));
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Comparable> Predicate<T> greaterThan(final T target) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return Comparators.compare(value, target) > 0;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Comparable> Predicate<T> greaterEqual(final T target) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return Comparators.compare(value, target) >= 0;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Comparable> Predicate<T> lessThan(final T target) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return Comparators.compare(value, target) < 0;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Comparable> Predicate<T> lessEqual(final T target) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return Comparators.compare(value, target) <= 0;
            }
        };
    }

    public static <T> Predicate<T> in(final Collection<?> c) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return c.contains(value);
            }
        };
    }

    public static <T> Predicate<T> notIn(final Collection<?> c) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return !c.contains(value);
            }
        };
    }

    public static <T> Predicate<T> instanceOf(final Class<?> clazz) {
        return new Predicate<T>() {
            @Override
            public boolean test(T value) {
                return clazz.isInstance(value);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public static Predicate<Class> subtypeOf(final Class<?> clazz) {
        return new Predicate<Class>() {
            @Override
            public boolean test(Class value) {
                return clazz.isAssignableFrom(value);
            }
        };
    }

    public static Predicate<String> startsWith(final String prefix) {
        return new Predicate<String>() {
            @Override
            public boolean test(String value) {
                return value.startsWith(prefix);
            }
        };
    }

    public static Predicate<String> endsWith(final String suffix) {
        return new Predicate<String>() {
            @Override
            public boolean test(String value) {
                return value.endsWith(suffix);
            }
        };
    }

    public static Predicate<CharSequence> matches(final Pattern pattern) {
        return new Predicate<CharSequence>() {
            @Override
            public boolean test(CharSequence value) {
                return pattern.matcher(value).find();
            }
        };
    }

    public static <K, V> Predicate<Map.Entry<K, V>> testByKey(final Predicate<? super K> predicate) {
        return new Predicate<Map.Entry<K, V>>() {
            @Override
            public boolean test(Entry<K, V> entry) {
                return predicate.test(entry.getKey());
            }
        };
    }

    public static <K, V> Predicate<Map.Entry<K, V>> testByValue(final Predicate<? super V> predicate) {
        return new Predicate<Map.Entry<K, V>>() {
            @Override
            public boolean test(Entry<K, V> entry) {
                return predicate.test(entry.getValue());
            }
        };
    }

    public static <K, V> Consumer<Map.Entry<K, V>> acceptByKey(final Consumer<? super K> consumer) {
        return new Consumer<Map.Entry<K, V>>() {
            @Override
            public void accept(Entry<K, V> entry) {
                consumer.accept(entry.getKey());
            }
        };
    }

    public static <K, V> Consumer<Map.Entry<K, V>> acceptByValue(final Consumer<? super V> consumer) {
        return new Consumer<Map.Entry<K, V>>() {
            @Override
            public void accept(Entry<K, V> entry) {
                consumer.accept(entry.getValue());
            }
        };
    }

    public static <K, V, R> Function<Map.Entry<K, V>, R> applyByKey(final Function<? super K, R> func) {
        return new Function<Map.Entry<K, V>, R>() {
            @Override
            public R apply(Entry<K, V> entry) {
                return func.apply(entry.getKey());
            }
        };
    }

    public static <K, V, R> Function<Map.Entry<K, V>, R> applyByValue(final Function<? super V, R> func) {
        return new Function<Map.Entry<K, V>, R>() {
            @Override
            public R apply(Entry<K, V> entry) {
                return func.apply(entry.getValue());
            }
        };
    }

    public static <K, V, KK> Function<Map.Entry<K, V>, Map.Entry<KK, V>> mapKey(final Function<? super K, KK> func) {
        return new Function<Map.Entry<K, V>, Map.Entry<KK, V>>() {
            @Override
            public Map.Entry<KK, V> apply(Entry<K, V> entry) {
                return new AbstractMap.SimpleImmutableEntry<>(func.apply(entry.getKey()), entry.getValue());
            }
        };
    }

    public static <K, V, VV> Function<Map.Entry<K, V>, Map.Entry<K, VV>> mapValue(final Function<? super V, VV> func) {
        return new Function<Map.Entry<K, V>, Map.Entry<K, VV>>() {
            @Override
            public Map.Entry<K, VV> apply(Entry<K, V> entry) {
                return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), func.apply(entry.getValue()));
            }
        };
    }

    public static <K, V> Predicate<Map.Entry<K, V>> testKeyVal(final BiPredicate<? super K, ? super V> predicate) {
        return new Predicate<Map.Entry<K, V>>() {
            @Override
            public boolean test(Entry<K, V> entry) {
                return predicate.test(entry.getKey(), entry.getValue());
            }
        };
    }

    public static <K, V> Consumer<Map.Entry<K, V>> acceptKeyVal(final BiConsumer<? super K, ? super V> consumer) {
        return new Consumer<Map.Entry<K, V>>() {
            @Override
            public void accept(Entry<K, V> entry) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        };
    }

    public static <K, V, R> Function<Map.Entry<K, V>, R> applyKeyVal(final BiFunction<? super K, ? super V, R> func) {
        return new Function<Map.Entry<K, V>, R>() {
            @Override
            public R apply(Entry<K, V> entry) {
                return func.apply(entry.getKey(), entry.getValue());
            }
        };
    }

    /**
     * 
     * @param predicate
     * @param limit
     * @return
     */
    public static <T> Predicate<T> limited(final Predicate<T> predicate, final int limit) {
        Objects.requireNonNull(predicate);

        return new Predicate<T>() {
            private final AtomicInteger counter = new AtomicInteger(limit);

            @Override
            public boolean test(T t) {
                return predicate.test(t) && counter.decrementAndGet() >= 0;
            }
        };
    }

    /**
     * 
     * @param predicate
     * @param limit
     * @return
     */
    public static <T> Predicate<T> limited(final Predicate<T> predicate, final long limit) {
        Objects.requireNonNull(predicate);

        return new Predicate<T>() {
            private final AtomicLong counter = new AtomicLong(limit);

            @Override
            public boolean test(T t) {
                return predicate.test(t) && counter.decrementAndGet() >= 0;
            }
        };
    }

    /**
     * 
     * @param predicate
     * @param limit
     * @return
     */
    public static <T, U> BiPredicate<T, U> limited(final BiPredicate<T, U> predicate, final int limit) {
        Objects.requireNonNull(predicate);

        return new BiPredicate<T, U>() {
            private final AtomicInteger counter = new AtomicInteger(limit);

            @Override
            public boolean test(T t, U u) {
                return predicate.test(t, u) && counter.decrementAndGet() >= 0;
            }
        };
    }

    /**
     * 
     * @param predicate
     * @param limit
     * @return
     */
    public static <T, U> BiPredicate<T, U> limited(final BiPredicate<T, U> predicate, final long limit) {
        Objects.requireNonNull(predicate);

        return new BiPredicate<T, U>() {
            private final AtomicLong counter = new AtomicLong(limit);

            @Override
            public boolean test(T t, U u) {
                return predicate.test(t, u) && counter.decrementAndGet() >= 0;
            }
        };
    }

    /**
     * 
     * @param predicate
     * @param limit
     * @return
     */
    public static <A, B, C> TriPredicate<A, B, C> limited(final TriPredicate<A, B, C> predicate, final int limit) {
        Objects.requireNonNull(predicate);

        return new TriPredicate<A, B, C>() {
            private final AtomicInteger counter = new AtomicInteger(limit);

            @Override
            public boolean test(A a, B b, C c) {
                return predicate.test(a, b, c) && counter.decrementAndGet() >= 0;
            }
        };
    }

    /**
     * 
     * @param predicate
     * @param limit
     * @return
     */
    public static <A, B, C> TriPredicate<A, B, C> limited(final TriPredicate<A, B, C> predicate, final long limit) {
        Objects.requireNonNull(predicate);

        return new TriPredicate<A, B, C>() {
            private final AtomicLong counter = new AtomicLong(limit);

            @Override
            public boolean test(A a, B b, C c) {
                return predicate.test(a, b, c) && counter.decrementAndGet() >= 0;
            }
        };
    }

    /**
     * Returns a stateful <code>Predicate</code> which should not be used in
     * parallel stream.
     * 
     * @param predicate
     * @return
     */
    public static <T> Predicate<T> indexed(final IndexedPredicate<T> predicate) {
        Objects.requireNonNull(predicate);

        return new Predicate<T>() {
            private final AtomicInteger idx = new AtomicInteger(0);

            @Override
            public boolean test(T t) {
                return predicate.test(idx.getAndIncrement(), t);
            }
        };
    }

    /**
     * Returns a stateful <code>BiPredicate</code> which should not be used in
     * parallel stream.
     * 
     * @param predicate
     * @return
     */
    public static <U, T> BiPredicate<U, T> indexed(final IndexedBiPredicate<U, T> predicate) {
        Objects.requireNonNull(predicate);

        return new BiPredicate<U, T>() {
            private final AtomicInteger idx = new AtomicInteger(0);

            @Override
            public boolean test(U u, T t) {
                return predicate.test(u, idx.getAndIncrement(), t);
            }
        };
    }

    /**
     * Returns a stateful <code>Function</code> which should not be used in
     * parallel stream.
     * 
     * @param func
     * @return
     */
    public static <T, R> Function<T, R> indexeD(final IndexedFunction<T, R> func) {
        Objects.requireNonNull(func);

        return new Function<T, R>() {
            private final AtomicInteger idx = new AtomicInteger(0);

            @Override
            public R apply(T t) {
                return func.apply(idx.getAndIncrement(), t);
            }
        };
    }

    /**
     * Returns a stateful <code>BiFunction</code> which should not be used in
     * parallel stream.
     * 
     * @param func
     * @return
     */
    public static <U, T, R> BiFunction<U, T, R> indexeD(final IndexedBiFunction<U, T, R> func) {
        Objects.requireNonNull(func);

        return new BiFunction<U, T, R>() {
            private final AtomicInteger idx = new AtomicInteger(0);

            @Override
            public R apply(U u, T t) {
                return func.apply(u, idx.getAndIncrement(), t);
            }
        };
    }

    /**
     * Returns a stateful <code>Consumer</code> which should not be used in
     * parallel stream.
     * 
     * @param action
     * @return
     */
    public static <T> Consumer<T> indeXed(final IndexedConsumer<T> action) {
        Objects.requireNonNull(action);

        return new Consumer<T>() {
            private final AtomicInteger idx = new AtomicInteger(0);

            @Override
            public void accept(T t) {
                action.accept(idx.getAndIncrement(), t);
            }
        };
    }

    /**
     * Returns a stateful <code>BiConsumer</code> which should not be used in
     * parallel stream.
     * 
     * @param action
     * @return
     */
    public static <U, T> BiConsumer<U, T> indeXed(final IndexedBiConsumer<U, T> action) {
        Objects.requireNonNull(action);

        return new BiConsumer<U, T>() {
            private final AtomicInteger idx = new AtomicInteger(0);

            @Override
            public void accept(U u, T t) {
                action.accept(u, idx.getAndIncrement(), t);
            }
        };
    }

    public static <T> BinaryOperator<T> throwingMerger() {
        return THROWING_MERGER;
    }

    public static <T> BinaryOperator<T> ignoringMerger() {
        return IGNORING_MERGER;
    }

    public static <T> BinaryOperator<T> replacingMerger() {
        return REPLACING_MERGER;
    }

    public static <T> Collector<T, ?, List<T>> toList() {
        return Collectors.toList();
    }

    public static <T> Collector<T, ?, Set<T>> toSet() {
        return Collectors.toSet();
    }

    /**
     * 
     * @return
     * @see Collectors#toMap()
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Fn.IDENTITY, Fn.IDENTITY);
    }

    /**
     * 
     * @param mergeFunction
     * @return
     * @see Collectors#toMap(BinaryOperator)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap(final BinaryOperator<V> mergeFunction) {
        return Collectors.toMap(Fn.IDENTITY, Fn.IDENTITY, mergeFunction);
    }

    /**
     * 
     * @param mapFactory
     * @return
     * @see Collectors#toMap(Supplier)
     */
    public static <K, V, M extends Map<K, V>> Collector<Map.Entry<K, V>, ?, M> toMap(final Supplier<M> mapFactory) {
        return Collectors.toMap(Fn.IDENTITY, Fn.IDENTITY, Fn.THROWING_MERGER, mapFactory);
    }

    /**
     * 
     * @param mergeFunction
     * @param mapFactory
     * @return
     * @see Collectors#toMap(BinaryOperator, Supplier)
     */
    public static <K, V, M extends Map<K, V>> Collector<Map.Entry<K, V>, ?, M> toMap(
            final BinaryOperator<V> mergeFunction, final Supplier<M> mapFactory) {
        return Collectors.toMap(Fn.IDENTITY, Fn.IDENTITY, mergeFunction, mapFactory);
    }

    /**
     * 
     * @param keyExtractor
     * @param valueMapper
     * @return
     * @see Collectors#toMap(Function, Function)
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyExtractor,
            Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyExtractor, valueMapper);
    }

    /**
     * 
     * @param keyExtractor
     * @param valueMapper
     * @param mergeFunction
     * @return
     * @see Collectors#toMap(Function, Function, BinaryOperator)
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyExtractor,
            Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
        return Collectors.toMap(keyExtractor, valueMapper, mergeFunction);
    }

    /**
     * 
     * @param keyExtractor
     * @param valueMapper
     * @param mapFactory
     * @return
     * @see Collectors#toMap(Function, Function, Supplier)
     */
    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
            final Function<? super T, ? extends K> keyExtractor, final Function<? super T, ? extends U> valueMapper,
            final Supplier<M> mapFactory) {
        return Collectors.toMap(keyExtractor, valueMapper, Fn.THROWING_MERGER, mapFactory);
    }

    /**
     * 
     * @param keyExtractor
     * @param valueMapper
     * @param mergeFunction
     * @param mapFactory
     * @return
     * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
            final Function<? super T, ? extends K> keyExtractor, final Function<? super T, ? extends U> valueMapper,
            final BinaryOperator<U> mergeFunction, final Supplier<M> mapFactory) {
        return Collectors.toMap(keyExtractor, valueMapper, mergeFunction, mapFactory);
    }

    /**
     * 
     * @param classifier
     * @return
     * @see Collectors#groupingBy(Function)
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(Function<? super T, ? extends K> classifier) {
        return Collectors.groupingBy(classifier);
    }

    /**
     * 
     * @param classifier
     * @param mapFactory
     * @return
     * @see Collectors#groupingBy(Function, Supplier)
     */
    public static <T, K, M extends Map<K, List<T>>> Collector<T, ?, M> groupingBy(
            final Function<? super T, ? extends K> classifier, final Supplier<M> mapFactory) {
        return Collectors.groupingBy(classifier, mapFactory, Collectors.toList());
    }

    /**
     * 
     * @param classifier
     * @param downstream
     * @return
     * @see Collectors#groupingBy(Function, Collector)
     */
    public static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return Collectors.groupingBy(classifier, downstream);
    }

    /**
     * 
     * @param classifier
     * @param downstream
     * @param mapFactory
     * @return
     * @see Collectors#groupingBy(Function, Collector, Supplier)
     */
    public static <T, K, A, D, M extends Map<K, D>> Collector<T, ?, M> groupingBy(
            final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream,
            final Supplier<M> mapFactory) {
        return Collectors.groupingBy(classifier, mapFactory, downstream);
    }

    /**
     * 
     * @param classifier
     * @return
     * @see Collectors#groupingBy(Function)
     */
    public static <T, K> Collector<T, ?, Map<K, Long>> countingBy(Function<? super T, ? extends K> classifier) {
        return countingBy(classifier, Suppliers.ofMap());
    }

    /**
     * 
     * @param classifier
     * @param mapFactory
     * @return
     * @see Collectors#groupingBy(Function, Supplier)
     */
    public static <T, K, M extends Map<K, Long>> Collector<T, ?, M> countingBy(
            final Function<? super T, ? extends K> classifier, final Supplier<M> mapFactory) {
        return Collectors.groupingBy(classifier, mapFactory, Collectors.counting());
    }

    /**
     * 
     * @param classifier
     * @return
     * @see Collectors#groupingBy(Function)
     */
    public static <T, K> Collector<T, ?, Map<K, Integer>> countingIntBy(Function<? super T, ? extends K> classifier) {
        return countingIntBy(classifier, Suppliers.ofMap());
    }

    /**
     * 
     * @param classifier
     * @param mapFactory
     * @return
     * @see Collectors#groupingBy(Function, Supplier)
     */
    public static <T, K, M extends Map<K, Integer>> Collector<T, ?, M> countingIntBy(
            final Function<? super T, ? extends K> classifier, final Supplier<M> mapFactory) {
        return Collectors.groupingBy(classifier, mapFactory, MoreCollectors.countingInt());
    }

    public static <T> Collector<T, ?, Long> counting() {
        return Collectors.counting();
    }

    public static <T> Collector<T, ?, Integer> countingInt() {
        return MoreCollectors.countingInt();
    }

    public static <T> Collector<T, ?, Integer> summingInt(final ToIntFunction<? super T> mapper) {
        return Collectors.summingInt(mapper);
    }

    public static <T> Collector<T, ?, Long> summingLong(final ToLongFunction<? super T> mapper) {
        return Collectors.summingLong(mapper);
    }

    public static <T> Collector<T, ?, Double> summingDouble(final ToDoubleFunction<? super T> mapper) {
        return Collectors.summingDouble(mapper);
    }

    public static <T> Collector<T, ?, Double> averagingInt(final ToIntFunction<? super T> mapper) {
        return Collectors.averagingInt(mapper);
    }

    public static <T> Collector<T, ?, Double> averagingLong(final ToLongFunction<? super T> mapper) {
        return Collectors.averagingLong(mapper);
    }

    public static <T> Collector<T, ?, Double> averagingDouble(final ToDoubleFunction<? super T> mapper) {
        return Collectors.averagingDouble(mapper);
    }

    public static final class Factory {

        private static final IntFunction<boolean[]> BOOLEAN_ARRAY = new IntFunction<boolean[]>() {
            @Override
            public boolean[] apply(int len) {
                return new boolean[len];
            }
        };

        private static final IntFunction<char[]> CHAR_ARRAY = new IntFunction<char[]>() {
            @Override
            public char[] apply(int len) {
                return new char[len];
            }
        };

        private static final IntFunction<byte[]> BYTE_ARRAY = new IntFunction<byte[]>() {
            @Override
            public byte[] apply(int len) {
                return new byte[len];
            }
        };

        private static final IntFunction<short[]> SHORT_ARRAY = new IntFunction<short[]>() {
            @Override
            public short[] apply(int len) {
                return new short[len];
            }
        };

        private static final IntFunction<int[]> INT_ARRAY = new IntFunction<int[]>() {
            @Override
            public int[] apply(int len) {
                return new int[len];
            }
        };

        private static final IntFunction<long[]> LONG_ARRAY = new IntFunction<long[]>() {
            @Override
            public long[] apply(int len) {
                return new long[len];
            }
        };

        private static final IntFunction<float[]> FLOAT_ARRAY = new IntFunction<float[]>() {
            @Override
            public float[] apply(int len) {
                return new float[len];
            }
        };

        private static final IntFunction<double[]> DOUBLE_ARRAY = new IntFunction<double[]>() {
            @Override
            public double[] apply(int len) {
                return new double[len];
            }
        };

        private static final IntFunction<String[]> STRING_ARRAY = new IntFunction<String[]>() {
            @Override
            public String[] apply(int len) {
                return new String[len];
            }
        };

        private static final IntFunction<Object[]> OBJECT_ARRAY = new IntFunction<Object[]>() {
            @Override
            public Object[] apply(int len) {
                return new Object[len];
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super List> LIST_FACTORY = new IntFunction<List>() {
            @Override
            public List apply(int len) {
                return new ArrayList<>(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super LinkedList> LINKED_LIST_FACTORY = new IntFunction<LinkedList>() {
            @Override
            public LinkedList apply(int len) {
                return new LinkedList<>();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super Set> SET_FACTORY = new IntFunction<Set>() {
            @Override
            public Set apply(int len) {
                return new HashSet<>(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super LinkedHashSet> LINKED_HASH_SET_FACTORY = new IntFunction<LinkedHashSet>() {
            @Override
            public LinkedHashSet apply(int len) {
                return new LinkedHashSet<>(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super TreeSet> TREE_SET_FACTORY = new IntFunction<TreeSet>() {
            @Override
            public TreeSet apply(int len) {
                return new TreeSet<>();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super Map> MAP_FACTORY = new IntFunction<Map>() {
            @Override
            public Map apply(int len) {
                return new HashMap<>(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super LinkedHashMap> LINKED_HASH_MAP_FACTORY = new IntFunction<LinkedHashMap>() {
            @Override
            public LinkedHashMap apply(int len) {
                return new LinkedHashMap<>(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super TreeMap> TREE_MAP_FACTORY = new IntFunction<TreeMap>() {
            @Override
            public TreeMap apply(int len) {
                return new TreeMap<>();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super ConcurrentHashMap> CONCURRENT_HASH_MAP_FACTORY = new IntFunction<ConcurrentHashMap>() {
            @Override
            public ConcurrentHashMap apply(int len) {
                return new ConcurrentHashMap(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super Queue> QUEUE_FACTORY = new IntFunction<Queue>() {
            @Override
            public Queue apply(int len) {
                return new LinkedList();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super ArrayDeque> ARRAY_DEQUE_FACTORY = new IntFunction<ArrayDeque>() {
            @Override
            public ArrayDeque apply(int len) {
                return new ArrayDeque(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super LinkedBlockingQueue> LINKED_BLOCKING_QUEUE_FACTORY = new IntFunction<LinkedBlockingQueue>() {
            @Override
            public LinkedBlockingQueue apply(int len) {
                return new LinkedBlockingQueue(len);
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super ConcurrentLinkedQueue> CONCURRENT_LINKED_QUEUE_FACTORY = new IntFunction<ConcurrentLinkedQueue>() {
            @Override
            public ConcurrentLinkedQueue apply(int len) {
                return new ConcurrentLinkedQueue();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final IntFunction<? super PriorityQueue> PRIORITY_QUEUE_FACTORY = new IntFunction<PriorityQueue>() {
            @Override
            public PriorityQueue apply(int len) {
                return new PriorityQueue(len);
            }
        };

        private Factory() {
            // singleton.
        }

        public static IntFunction<boolean[]> ofBooleanArray() {
            return BOOLEAN_ARRAY;
        }

        public static IntFunction<char[]> ofCharArray() {
            return CHAR_ARRAY;
        }

        public static IntFunction<byte[]> ofByteArray() {
            return BYTE_ARRAY;
        }

        public static IntFunction<short[]> ofShortArray() {
            return SHORT_ARRAY;
        }

        public static IntFunction<int[]> ofIntArray() {
            return INT_ARRAY;
        }

        public static IntFunction<long[]> ofLongArray() {
            return LONG_ARRAY;
        }

        public static IntFunction<float[]> ofFloatArray() {
            return FLOAT_ARRAY;
        }

        public static IntFunction<double[]> ofDoubleArray() {
            return DOUBLE_ARRAY;
        }

        public static IntFunction<String[]> ofStringArray() {
            return STRING_ARRAY;
        }

        public static IntFunction<Object[]> ofObjectArray() {
            return OBJECT_ARRAY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<List<T>> ofList() {
            return (IntFunction) LIST_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<LinkedList<T>> ofLinkedList() {
            return (IntFunction) LINKED_LIST_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<Set<T>> ofSet() {
            return (IntFunction) SET_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<LinkedHashSet<T>> ofLinkedHashSet() {
            return (IntFunction) LINKED_HASH_SET_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<TreeSet<T>> ofTreeSet() {
            return (IntFunction) TREE_SET_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> IntFunction<Map<K, V>> ofMap() {
            return (IntFunction) MAP_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> IntFunction<LinkedHashMap<K, V>> ofLinkedHashMap() {
            return (IntFunction) LINKED_HASH_MAP_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> IntFunction<TreeMap<K, V>> ofTreeMap() {
            return (IntFunction) TREE_MAP_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> IntFunction<ConcurrentHashMap<K, V>> ofConcurrentHashMap() {
            return (IntFunction) CONCURRENT_HASH_MAP_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<Queue<T>> ofQueue() {
            return (IntFunction) QUEUE_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<ArrayDeque<T>> ofArrayDeque() {
            return (IntFunction) ARRAY_DEQUE_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<LinkedBlockingQueue<T>> ofLinkedBlockingQueue() {
            return (IntFunction) LINKED_BLOCKING_QUEUE_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<ConcurrentLinkedQueue<T>> ofConcurrentLinkedQueue() {
            return (IntFunction) CONCURRENT_LINKED_QUEUE_FACTORY;
        }

        @SuppressWarnings("rawtypes")
        public static <T> IntFunction<PriorityQueue<T>> ofPriorityQueue() {
            return (IntFunction) PRIORITY_QUEUE_FACTORY;
        }
    }

    public static final class Suppliers {
        @SuppressWarnings("rawtypes")
        private static final Supplier<? super List> LIST = new Supplier<List>() {
            @Override
            public List get() {
                return new ArrayList();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super LinkedList> LINKED_LIST = new Supplier<LinkedList>() {
            @Override
            public LinkedList get() {
                return new LinkedList();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super Set> SET = new Supplier<Set>() {
            @Override
            public Set get() {
                return new HashSet();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super LinkedHashSet> LINKED_HASH_SET = new Supplier<LinkedHashSet>() {
            @Override
            public LinkedHashSet get() {
                return new LinkedHashSet();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super TreeSet> TREE_SET = new Supplier<TreeSet>() {
            @Override
            public TreeSet get() {
                return new TreeSet();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super Map> MAP = new Supplier<Map>() {
            @Override
            public Map get() {
                return new HashMap();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super LinkedHashMap> LINKED_HASH_MAP = new Supplier<LinkedHashMap>() {
            @Override
            public LinkedHashMap get() {
                return new LinkedHashMap();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super TreeMap> TREE_MAP = new Supplier<TreeMap>() {
            @Override
            public TreeMap get() {
                return new TreeMap();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super ConcurrentHashMap> CONCURRENT_HASH_MAP = new Supplier<ConcurrentHashMap>() {
            @Override
            public ConcurrentHashMap get() {
                return new ConcurrentHashMap();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super Queue> QUEUE = new Supplier<Queue>() {
            @Override
            public Queue get() {
                return new LinkedList();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super ArrayDeque> ARRAY_DEQUE = new Supplier<ArrayDeque>() {
            @Override
            public ArrayDeque get() {
                return new ArrayDeque();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super LinkedBlockingQueue> LINKED_BLOCKING_QUEUE = new Supplier<LinkedBlockingQueue>() {
            @Override
            public LinkedBlockingQueue get() {
                return new LinkedBlockingQueue();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super ConcurrentLinkedQueue> CONCURRENT_LINKED_QUEUE = new Supplier<ConcurrentLinkedQueue>() {
            @Override
            public ConcurrentLinkedQueue get() {
                return new ConcurrentLinkedQueue();
            }
        };

        @SuppressWarnings("rawtypes")
        private static final Supplier<? super PriorityQueue> PRIORITY_QUEUE = new Supplier<PriorityQueue>() {
            @Override
            public PriorityQueue get() {
                return new PriorityQueue();
            }
        };

        private Suppliers() {
            // singleton.
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<List<T>> ofList() {
            return (Supplier) LIST;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<LinkedList<T>> ofLinkedList() {
            return (Supplier) LINKED_LIST;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<Set<T>> ofSet() {
            return (Supplier) SET;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<LinkedHashSet<T>> ofLinkedHashSet() {
            return (Supplier) LINKED_HASH_SET;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<TreeSet<T>> ofTreeSet() {
            return (Supplier) TREE_SET;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> Supplier<Map<K, V>> ofMap() {
            return (Supplier) MAP;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> Supplier<LinkedHashMap<K, V>> ofLinkedHashMap() {
            return (Supplier) LINKED_HASH_MAP;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> Supplier<TreeMap<K, V>> ofTreeMap() {
            return (Supplier) TREE_MAP;
        }

        @SuppressWarnings("rawtypes")
        public static <K, V> Supplier<ConcurrentHashMap<K, V>> ofConcurrentHashMap() {
            return (Supplier) CONCURRENT_HASH_MAP;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<Queue<T>> ofQueue() {
            return (Supplier) QUEUE;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<ArrayDeque<T>> ofArrayDeque() {
            return (Supplier) ARRAY_DEQUE;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<LinkedBlockingQueue<T>> ofLinkedBlockingQueue() {
            return (Supplier) LINKED_BLOCKING_QUEUE;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<ConcurrentLinkedQueue<T>> ofConcurrentLinkedQueue() {
            return (Supplier) CONCURRENT_LINKED_QUEUE;
        }

        @SuppressWarnings("rawtypes")
        public static <T> Supplier<PriorityQueue<T>> ofPriorityQueue() {
            return (Supplier) PRIORITY_QUEUE;
        }
    }

}
