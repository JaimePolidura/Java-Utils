package es.jaime.javaddd.domain.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class CollectionUtils {
    @SafeVarargs
    public static <T> List<T> concat(Collection<T>... collections) {
        List<T> toReturn = new LinkedList<>();

        for (Collection<T> collection : collections)
            toReturn.addAll(collection);

        return toReturn;
    }

    public static <I, K, V> Map<K, V> groupBy(List<I> input, Function<I, K> groupByKeyExtractor,
                                              Function<I, V> creator, BiFunction<V, V, V> combiner) {
        Map<K, V> grouped = new HashMap<>();

        for (I actualElement : input) {
            K keyActualElement = groupByKeyExtractor.apply(actualElement);
            V groupedElement = grouped.get(keyActualElement);
            boolean notGrouped = groupedElement == null;

            grouped.put(keyActualElement, notGrouped ?
                    creator.apply(actualElement) :
                    combiner.apply(creator.apply(actualElement), groupedElement)
            );
        }

        return grouped;
    }

    public static <K, V> Map<K, V> mapDiff(Map<K, V> mapA, Map<K, V> mapB, BiFunction<V, V, V> combiner,
                                           Predicate<V> deleteCombinedCondition) {
        Map<K, V> newMap = new HashMap<>();

        for(Map.Entry<K, V> entry : mapA.entrySet()) {
            K actualKey = entry.getKey();
            V actualAValue = entry.getValue();
            boolean containedInB = mapB.containsKey(actualKey);

            if (containedInB) {
                V actualBValue = mapB.get(actualKey);
                V combinedValue = combiner.apply(actualAValue, actualBValue);

                if(!deleteCombinedCondition.test(combinedValue))
                    newMap.put(actualKey, combinedValue);

            } else {
                newMap.put(actualKey, actualAValue);
            }
        }

        return newMap;
    }

    public static <K> Map<K, Double> incrementMap(Map<K, Double> map, K key, double value) {
        double newValue = map.containsKey(key) ? map.get(key) + value : value;
        map.put(key, newValue);

        return map;
    }

    public static <K> Map<K, Double> decrementMap(Map<K, Double> map, K key, double value) {
        double newValue = map.containsKey(key) ? map.get(key) - value : value;

        if(newValue <= 0)
            map.remove(key, newValue);
        else
            map.put(key, newValue);


        return map;
    }

    public static <K, V> Map<K, V> incrementMap(Map<K, V> map, K key, V valueToApply, BiFunction<V, V, V> combiner) {
        V valueInMap = map.get(key);
        V newValue = valueInMap != null ? combiner.apply(valueInMap, valueToApply) : valueToApply;
        map.put(key, newValue);

        return map;
    }

    public static <K, V> Map<K, List<V>> incrementMapList(Map<K, List<V>> map, K key, V valueToApply) {
        List<V> found = map.get(key);
        if (found == null){
            map.put(key, new LinkedList<>(Collections.singletonList(valueToApply)));
        }else{
            found.add(valueToApply);
            map.put(key, found);
        }

        return map;
    }

    public static <I, O> List<O> map(List<I> initial, Function<I, O> mapper) {
        return initial.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();

        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @SafeVarargs
    public static <T> List<T> toList(T... items) {
        return new ArrayList<T>(Arrays.asList(items));
    }
}