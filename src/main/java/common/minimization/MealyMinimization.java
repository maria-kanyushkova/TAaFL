package common.minimization;

import common.models.FullCheckMealyEdge;
import common.models.MealyEdge;
import common.models.MealyNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MealyMinimization {
    private final Comparator<String> cmp = (l, r) -> {
        int a = Integer.parseInt(l.substring(1));
        int b = Integer.parseInt(r.substring(1));
        return Integer.compare(a, b);
    };

    private char letter = 'a';

    public List<MealyEdge> minimizeGraph(List<MealyEdge> edges, int cnt) {
        List<MealyEdgeEq> converted = toMealyEdgeEqs(edges);
        List<MealyNode> nodes = getSortedNodes(converted);

        var sources = new TreeMap<MealyNode, List<MealyEdgeEq>>();

        fillSources(converted, nodes, sources);

        var y = new LinkedHashMap<List<String>, String>();
        var prev = new TreeMap<String, Map<String, List<MealyNode>>>(cmp);

        var index = new AtomicInteger();

        performInitialGrouping(sources, y, prev, index);

        applyGroupingToMap(sources, prev);

        var curr = deep(sources, prev, index);

        int currSize = prev.size();
        int prevSize = 0;
        while (prevSize != currSize) {
            prevSize = currSize;
            curr = deep(sources, curr, index);
            currSize = curr.size();
        }

        var result = new LinkedList<MealyEdgeEq>();

        leaveOnlyFirstValues(curr);

        restoreEdgesFromSources(sources, index, curr, result);

        LinkedList<MealyEdge> ordered = calculateCorrectOrder(cnt, result);

        return toMealyEdge(ordered);
    }

    private List<MealyEdge> toMealyEdge(List<MealyEdge> ordered) {
        return ordered.stream().map(mealyEdge -> {
            var edge = new MealyEdge();
            edge.to = mealyEdge.to;
            edge.from = mealyEdge.from;
            edge.y = mealyEdge.y;
            edge.x = mealyEdge.x;
            return edge;
        }).collect(Collectors.toList());
    }

    private LinkedList<MealyEdge> calculateCorrectOrder(int cnt, List<MealyEdgeEq> result) {
        var list = new LinkedList<MealyEdge>();
        var i = 0;
        while (i != cnt) {
            for (int j = 0; j < result.size(); j++) {
                if (j % cnt == i) {
                    list.add(result.get(j));
                }
            }
            i++;
        }
        return list;
    }

    private void restoreEdgesFromSources(Map<MealyNode, List<MealyEdgeEq>> sources,
                                         AtomicInteger index,
                                         Map<String, Map<String, List<MealyNode>>> curr,
                                         List<MealyEdgeEq> result) {
        for (var entry : curr.entrySet()) {
            var currKey = entry.getKey();
            var map = entry.getValue();
            for (var e : sources.entrySet()) {
                var node = e.getKey();
                var list = e.getValue();
                list.forEach(mealyEdge -> {
                    index.set(0);
                    for (var mapEntry : map.entrySet()) {
                        var mapKey = mapEntry.getKey();
                        var nodes = mapEntry.getValue();
                        for (var mealyNode : nodes) {
                            if (mapKey.equals(node.q)) {
                                var edge = new MealyEdgeEq();
                                var from = new MealyNode();
                                from.q = currKey;
                                edge.y = list.get(index.get()).y;
                                edge.from = from;
                                edge.to = mealyNode;
                                edge.x = "x" + index.getAndIncrement();
                                if (!result.contains(edge)) {
                                    result.add(edge);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void leaveOnlyFirstValues(Map<String, Map<String, List<MealyNode>>> curr) {
        for (var stringMapEntry : curr.entrySet()) {
            var firstKey = stringMapEntry.getValue().keySet().stream().findFirst().orElseThrow();
            var firstValue = stringMapEntry.getValue().get(firstKey);
            var map = new TreeMap<String, List<MealyNode>>();
            map.put(firstKey, firstValue);
            stringMapEntry.setValue(map);
        }
    }

    private void applyGroupingToMap(Map<MealyNode, List<MealyEdgeEq>> sources,
                                    Map<String, Map<String, List<MealyNode>>> map) {
        for (var entry : map.entrySet()) {
            var nodesList = entry.getValue();
            for (var e : nodesList.entrySet()) {
                var nodeKey = e.getKey();
                var mealyNodeList = e.getValue();
                for (var mapEntry : sources.entrySet()) {
                    var mealyNode = mapEntry.getKey();
                    var mealyEdgesList = mapEntry.getValue();
                    if (mealyNode.q.equals(nodeKey)) {
                        for (var mealyEdge : mealyEdgesList) {
                            int i = Integer.parseInt(mealyEdge.to.q.substring(1));
                            for (var stringMapEntry : map.entrySet()) {
                                var key = stringMapEntry.getKey();
                                var value = stringMapEntry.getValue();
                                if (value.containsKey("s" + i)) {
                                    var node = new MealyNode(mealyNode);
                                    node.q = key;
                                    mealyNodeList.add(node);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void performInitialGrouping(Map<MealyNode, List<MealyEdgeEq>> sources,
                                        Map<List<String>, String> y,
                                        Map<String, Map<String, List<MealyNode>>> prev,
                                        AtomicInteger index) {
        for (var entry : sources.entrySet()) {
            var tableKey = entry.getKey();
            var tableValue = entry.getValue();
            String value;
            var key = tableValue
                .stream()
                .map(mealyEdge -> mealyEdge.y)
                .collect(Collectors.toList());
            if (!y.containsKey(key)) {
                value = String.valueOf(letter) + index.getAndIncrement();
            } else {
                value = y.get(key);
            }
            y.put(key, value);
            if (!prev.containsKey(value)) {
                var map = new TreeMap<String, List<MealyNode>>(cmp);
                map.put(tableKey.q, new LinkedList<>());
                prev.put(value, map);
            } else {
                var map = prev.get(value);
                if (!map.containsKey(tableKey.q)) {
                    map.put(tableKey.q, new LinkedList<>());
                }
            }
        }
    }

    private void fillSources(List<MealyEdgeEq> edges,
                             List<MealyNode> nodes,
                             Map<MealyNode, List<MealyEdgeEq>> sources) {
        for (var node : nodes) {
            var list = edges
                .stream()
                .filter(edge -> edge.from.equals(node))
                .collect(Collectors.toList());
            sources.put(node, list);
        }
    }

    private List<MealyNode> getSortedNodes(List<MealyEdgeEq> edges) {
        var sortMooreEdges = getSortEdges(edges);
        List<MealyNode> nodes = new LinkedList<>();

        for (MealyEdgeEq v : sortMooreEdges) {
            if (!nodes.contains(v.from)) {
                nodes.add(v.from);
            }
            if (!nodes.contains(v.to)) {
                nodes.add(v.to);
            }
        }

        return nodes;
    }

    private List<MealyEdgeEq> toMealyEdgeEqs(List<MealyEdge> edges) {
        return edges.stream().map(mealyEdge -> {
            var edge = new MealyEdgeEq();
            edge.to = mealyEdge.to;
            edge.from = mealyEdge.from;
            edge.y = mealyEdge.y;
            edge.x = mealyEdge.x;
            return edge;
        }).collect(Collectors.toList());
    }

    private Map<String, Map<String, List<MealyNode>>> deep(Map<MealyNode, List<MealyEdgeEq>> sources,
                                                           Map<String, Map<String, List<MealyNode>>> currGroupedByKey,
                                                           AtomicInteger index) {
        var next = new TreeMap<String, Map<String, List<MealyNode>>>(cmp);

        letter++;

        index.set(0);

        currGroupedByKey.forEach((groupKey, nodesMap) -> {
            var ref = new Object() {
                String key = String.valueOf(letter) + index;
            };

            if (next.isEmpty()) {
                next.put(ref.key, new TreeMap<>());
            }

            var i = new AtomicInteger(0);
            var ind = new AtomicReference<>("");

            if (nodesMap.size() == 1) {
                for (var entry : nodesMap.entrySet()) {
                    var key = entry.getKey();
                    var nodes = entry.getValue();
                    var map = new TreeMap<String, List<MealyNode>>(cmp);
                    map.put(key, nodes);
                    next.put(ref.key, map);
                }
            }

            nodesMap
                .entrySet()
                .stream()
                .reduce((prev, entry) -> {
                    var prevMapKey = prev.getKey();
                    var prevMapList = prev.getValue();
                    var currMapKey = entry.getKey();
                    var currMapList = entry.getValue();
                    if (!prevMapList.equals(currMapList)) {
                        for (var e : next.entrySet()) {
                            var key = e.getKey();
                            var v = e.getValue();
                            if (v.isEmpty()) {
                                var value = next.get(key);
                                value.put(prevMapKey, prevMapList);
                                next.put(key, value);
                            }
                            for (var mapEntry : v.entrySet()) {
                                var nodes = mapEntry.getValue();
                                if (currMapList.equals(nodes)) {
                                    ind.set(key);
                                }
                            }
                        }
                        var in = new AtomicReference<>("");
                        for (var e : next.entrySet()) {
                            var nextKey = e.getKey();
                            if (nextKey.equals(ref.key)) {
                                in.set(nextKey);
                            }
                        }
                        if (in.get().equals("")) {
                            var map = new TreeMap<String, List<MealyNode>>(cmp);
                            map.put(prevMapKey, prevMapList);
                            next.put(ref.key, map);
                        }
                        if (ind.get().equals("")) {
                            ref.key = String.valueOf(letter) + index.incrementAndGet();
                        } else {
                            var map = next.get(String.valueOf(ind));
                            map.put(currMapKey, currMapList);
                            next.put(String.valueOf(ind), map);
                        }
                    }

                    if (ind.get().equals("")) {
                        if (next.containsKey(ref.key)) {
                            var map = next.get(ref.key);
                            map.put(prevMapKey, prevMapList);
                            map.put(currMapKey, currMapList);
                        } else {
                            var map = new TreeMap<String, List<MealyNode>>(cmp);
                            if (i.getAndIncrement() == 0) {
                                map.put(currMapKey, currMapList);
                                if (currMapList.equals(prevMapList)) {
                                    map.put(prevMapKey, prevMapList);
                                }
                            }
                            if (map.size() == 0) {
                                map.put(currMapKey, currMapList);
                            }
                            next.put(ref.key, map);
                        }
                    } else {
                        ind.set("");
                    }
                    return entry;
                });

            index.incrementAndGet();
        });

        for (var entry : next.entrySet()) {
            var map = entry.getValue();
            for (var e : map.entrySet()) {
                var nodes = e.getValue();
                nodes.clear();
            }
        }

        applyGroupingToMap(sources, next);

        return next;
    }

    private List<MealyEdgeEq> getSortEdges(List<MealyEdgeEq> edges) {
        return edges.stream().sorted((l, r) -> {
            int a = Integer.parseInt(l.to.q.substring(1));
            int b = Integer.parseInt(r.to.q.substring(1));
            if (a > b) {
                return 1;
            } else if (a < b) {
                return -1;
            } else {
                int c = Integer.parseInt(l.y.substring(1));
                int d = Integer.parseInt(r.y.substring(1));
                return Integer.compare(c, d);
            }
        }).collect(Collectors.toList());
    }

    private static class MealyEdgeEq extends FullCheckMealyEdge {
    }
}
