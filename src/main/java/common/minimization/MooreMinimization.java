package common.minimization;

import common.models.MooreEdge;
import common.models.MooreNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MooreMinimization {
    private final Comparator<String> cmp = (l, r) -> {
        int a = Integer.parseInt(l.substring(1));
        int b = Integer.parseInt(r.substring(1));
        return Integer.compare(a, b);
    };

    private char letter = 'a';

    public List<MooreEdge> minimizeGraph(List<MooreEdge> edges, int cnt) {
        List<MooreNode> nodes = getSortedNodes(edges);

        var sources = new TreeMap<MooreNode, List<MooreEdge>>();

        fillSources(edges, nodes, sources);

        var y = new LinkedHashMap<String, String>();
        var prev = new TreeMap<String, Map<String, List<MooreNode>>>(cmp);

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

        var result = new LinkedList<MooreEdge>();

        leaveOnlyFirstValues(curr);

        restoreEdgesFromSources(sources, index, curr, result);

        LinkedList<MooreEdge> ordered = calculateCorrectOrder(cnt, result);

        return toMooreEdge(ordered);
    }

    private List<MooreEdge> toMooreEdge(List<MooreEdge> ordered) {
        return ordered.stream().map(mooreEdge -> {
            var edge = new MooreEdge();
            edge.to = mooreEdge.to;
            edge.from = mooreEdge.from;
            edge.x = mooreEdge.x;
            return edge;
        }).collect(Collectors.toList());
    }

    private LinkedList<MooreEdge> calculateCorrectOrder(int cnt, List<MooreEdge> result) {
        var list = new LinkedList<MooreEdge>();
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

    private void restoreEdgesFromSources(Map<MooreNode, List<MooreEdge>> sources,
                                         AtomicInteger index,
                                         Map<String, Map<String, List<MooreNode>>> curr,
                                         List<MooreEdge> result) {
        for (var entry : curr.entrySet()) {
            var currKey = entry.getKey();
            var map = entry.getValue();
            for (var e : sources.entrySet()) {
                var node = e.getKey();
                var list = e.getValue();
                list.forEach(mooreEdge -> {
                    index.set(0);
                    for (var mapEntry : map.entrySet()) {
                        var mapKey = mapEntry.getKey();
                        var nodes = mapEntry.getValue();
                        for (var mooreNode : nodes) {
                            if (mapKey.equals(node.q)) {
                                var edge = new MooreEdge();
                                var from = new MooreNode();
                                from.q = currKey;
                                from.y = mooreNode.y;
                                edge.from = from;
                                edge.to = mooreNode;
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

    private void leaveOnlyFirstValues(Map<String, Map<String, List<MooreNode>>> curr) {
        for (var stringMapEntry : curr.entrySet()) {
            var firstKey = stringMapEntry.getValue().keySet().stream().findFirst().orElseThrow();
            var firstValue = stringMapEntry.getValue().get(firstKey);
            var map = new TreeMap<String, List<MooreNode>>();
            map.put(firstKey, firstValue);
            stringMapEntry.setValue(map);
        }
    }

    private void applyGroupingToMap(Map<MooreNode, List<MooreEdge>> sources,
                                    Map<String, Map<String, List<MooreNode>>> map) {
        for (var entry : map.entrySet()) {
            var nodesList = entry.getValue();
            for (var e : nodesList.entrySet()) {
                var nodeKey = e.getKey();
                var mooreNodeList = e.getValue();
                for (var mapEntry : sources.entrySet()) {
                    var mooreNode = mapEntry.getKey();
                    var mooreEdgeList = mapEntry.getValue();
                    if (mooreNode.q.equals(nodeKey)) {
                        for (var mooreEdge : mooreEdgeList) {
                            int i = Integer.parseInt(mooreEdge.to.q.substring(1));
                            for (var stringMapEntry : map.entrySet()) {
                                var key = stringMapEntry.getKey();
                                var value = stringMapEntry.getValue();
                                if (value.containsKey("s" + i)) {
                                    var node = new MooreNode(mooreNode);
                                    node.q = key;
                                    mooreNodeList.add(node);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void performInitialGrouping(Map<MooreNode, List<MooreEdge>> sources,
                                        Map<String, String> y,
                                        Map<String, Map<String, List<MooreNode>>> prev,
                                        AtomicInteger index) {
        for (var entry : sources.entrySet()) {
            var tableKey = entry.getKey();
            String value;
            var key = tableKey.y;
            if (!y.containsKey(key)) {
                value = String.valueOf(letter) + index.getAndIncrement();
            } else {
                value = y.get(key);
            }
            y.put(key, value);
            if (!prev.containsKey(value)) {
                var map = new TreeMap<String, List<MooreNode>>(cmp);
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

    private void fillSources(List<MooreEdge> edges,
                             List<MooreNode> nodes,
                             Map<MooreNode, List<MooreEdge>> sources) {
        for (var node : nodes) {
            var list = edges
                .stream()
                .filter(edge -> edge.from.equals(node))
                .collect(Collectors.toList());
            sources.put(node, list);
        }
    }

    private List<MooreNode> getSortedNodes(List<MooreEdge> edges) {
        var sortMooreEdges = getSortEdges(edges);
        List<MooreNode> nodes = new LinkedList<>();

        for (MooreEdge v : sortMooreEdges) {
            if (!nodes.contains(v.from)) {
                nodes.add(v.from);
            }
            if (!nodes.contains(v.to)) {
                nodes.add(v.to);
            }
        }

        return nodes;
    }

    private Map<String, Map<String, List<MooreNode>>> deep(Map<MooreNode, List<MooreEdge>> sources,
                                                           Map<String, Map<String, List<MooreNode>>> currGroupedByKey,
                                                           AtomicInteger index) {
        var next = new TreeMap<String, Map<String, List<MooreNode>>>(cmp);

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
                    var map = new TreeMap<String, List<MooreNode>>(cmp);
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
                            var map = new TreeMap<String, List<MooreNode>>(cmp);
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
                            var map = new TreeMap<String, List<MooreNode>>(cmp);
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

    private List<MooreEdge> getSortEdges(List<MooreEdge> edges) {
        return edges.stream().sorted((l, r) -> {
            int a = Integer.parseInt(l.to.q.substring(1));
            int b = Integer.parseInt(r.to.q.substring(1));
            return Integer.compare(a, b);
        }).collect(Collectors.toList());
    }
}
