package common.converter;

import common.models.MealyEdge;
import common.models.MealyNode;
import common.models.MooreEdge;
import common.models.MooreNode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MealyToMooreConverter {
    public List<MealyEdge> parseMealy(Scanner scanner,
                                      Integer inputsCount,
                                      Integer nodesCount) throws IOException {
        ArrayList<MealyNode> mealyNodes = new ArrayList<>();
        ArrayList<MealyEdge> mealyEdges = new ArrayList<>();

        fillMealyNodes(nodesCount, mealyNodes);
        fillMealyEdges(scanner, inputsCount, nodesCount, mealyNodes, mealyEdges);

        return mealyEdges;
    }

    private void fillMealyNodes(Integer nodesCount, List<MealyNode> mealyNodes) {
        for (Integer i = 0; i < nodesCount; i++) {
            MealyNode mealyNode = new MealyNode();

            mealyNode.q = "s" + i;

            mealyNodes.add(mealyNode);
        }
    }

    private void fillMealyEdges(Scanner scanner,
                                Integer inputsCount,
                                Integer nodesCount,
                                List<MealyNode> mealyNodes,
                                List<MealyEdge> mealyEdges) throws IOException {
        var delimiter = scanner.delimiter();
        for (Integer i = 0; i < inputsCount; i++) {
            for (Integer j = 0; j < nodesCount; j++) {
                String q = scanner.useDelimiter("y").next().trim();
                String y = scanner.useDelimiter(delimiter).next().trim();

                MealyEdge mealyEdge = new MealyEdge();

                mealyEdge.x = "x" + i;
                mealyEdge.y = y;

                mealyEdge.to = findMealyNode(mealyNodes, q);
                mealyEdge.from = mealyNodes.get(j);

                mealyEdges.add(mealyEdge);
            }
        }
    }


    private MealyNode findMealyNode(List<MealyNode> mealyNodes, String q) throws IOException {
        Optional<MealyNode> to = mealyNodes
            .stream()
            .filter(mealyNode -> mealyNode.q.equals(q))
            .findFirst();
        if (to.isEmpty()) {
            throw new IOException("Node " + q + " not found");
        }
        return to.get();
    }

    public List<MooreEdge> mealyToMoore(List<MealyEdge> mealyEdges) {

        List<MooreEdge> mooreEdges = new ArrayList<>();

        HashSet<MealyEdge> uniqueMealyEdges = new HashSet<>(mealyEdges);

        HashMap<MooreState, String> stateToZ = new HashMap<>();
        HashMap<String, MooreState> zToState = new HashMap<>();

        List<MooreNode> mooreNodes = new ArrayList<>();

        var sortedUniqueMealyEdges = sortedUniqueEdges(uniqueMealyEdges);

        fillMooreNodes(stateToZ, zToState, mooreNodes, sortedUniqueMealyEdges);

        fillMooreEdges(mealyEdges, mooreEdges, stateToZ, zToState, mooreNodes);

        return mooreEdges;
    }

    private void fillMooreEdges(List<MealyEdge> mealyEdges,
                                List<MooreEdge> mooreEdges,
                                HashMap<MooreState, String> stateToZ,
                                HashMap<String, MooreState> zToState,
                                List<MooreNode> mooreNodes) {
        int index = 0;
        for (MooreNode mooreFrom : mooreNodes) {
            MooreState state = zToState.get(mooreFrom.q);

            String qFrom = state.q;
            List<MealyEdge> mealyEdgeTo = mealyEdges
                .stream()
                .filter(mealyEdge -> mealyEdge.from.q.equals(qFrom))
                .collect(Collectors.toList());

            for (MealyEdge mealyEdge : mealyEdgeTo) {
                MooreEdge mooreEdge = new MooreEdge();

                MooreState mooreState = new MooreState(mealyEdge.y, mealyEdge.to.q);

                String mooreToZ = stateToZ.get(mooreState);

                Optional<MooreNode> mooreTo = mooreNodes.stream()
                    .filter(mooreNode -> mooreNode.q.equals(mooreToZ))
                    .findFirst();

                if (mooreTo.isPresent()) {
                    mooreEdge.from = mooreFrom;
                    mooreEdge.to = mooreTo.get();
                    mooreEdge.x = "x" + index++;

                    mooreEdges.add(mooreEdge);
                }
            }
            index = 0;
        }
    }

    private void fillMooreNodes(HashMap<MooreState, String> stateToZ,
                                HashMap<String, MooreState> zToState,
                                List<MooreNode> mooreNodes,
                                List<MealyEdge> sortedUniqueMealyEdges) {
        int index = 0;
        for (MealyEdge uniqueMealyEdge : sortedUniqueMealyEdges) {
            MealyNode mealyFrom = uniqueMealyEdge.to;
            Optional<MealyEdge> mealyEdgeFrom = sortedUniqueMealyEdges
                .stream()
                .filter(mealyEdge -> mealyEdge.to.equals(mealyFrom))
                .findFirst();
            if (mealyEdgeFrom.isPresent()) {
                MooreState state = new MooreState(uniqueMealyEdge.y, mealyFrom.q);
                if (!stateToZ.containsKey(state)) {
                    stateToZ.put(state, "z" + index);
                    zToState.put("z" + index, state);
                    index++;
                }

                MooreNode mooreNode = new MooreNode();
                mooreNode.q = stateToZ.get(state);
                mooreNode.y = uniqueMealyEdge.y;

                mooreNodes.add(mooreNode);
            }
        }
    }

    private List<MealyEdge> sortedUniqueEdges(HashSet<MealyEdge> uniqueMealyEdges) {
        return uniqueMealyEdges.stream().sorted((left, right) -> {
            int a = Integer.parseInt(left.to.q.substring(1));
            int b = Integer.parseInt(right.to.q.substring(1));
            if (a > b) {
                return 1;
            } else if (a < b) {
                return -1;
            } else {
                int c = Integer.parseInt(left.y.substring(1));
                int d = Integer.parseInt(right.y.substring(1));
                if (c > d) {
                    return 1;
                } else if (c < d) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }).collect(Collectors.toList());
    }
}
