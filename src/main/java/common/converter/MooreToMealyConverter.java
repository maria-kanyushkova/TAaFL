package common.converter;

import common.models.MealyEdge;
import common.models.MealyNode;
import common.models.MooreEdge;
import common.models.MooreNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class MooreToMealyConverter {
    public List<MooreEdge> parseMoore(Scanner scanner,
                                      Integer inputsCount,
                                      Integer nodesCount) throws IOException {
        ArrayList<MooreNode> mooreNodes = new ArrayList<>();
        ArrayList<MooreEdge> mooreEdges = new ArrayList<>();

        fillMooreNodes(scanner, nodesCount, mooreNodes);
        fillMooreEdges(scanner, inputsCount, nodesCount, mooreNodes, mooreEdges);

        return mooreEdges;
    }

    private void fillMooreNodes(Scanner scanner, Integer nodesCount, List<MooreNode> mooreNodes) {
        for (Integer i = 0; i < nodesCount; i++) {
            MooreNode mooreNode = new MooreNode();

            String y = scanner.next();

            mooreNode.q = "s" + i;
            mooreNode.y = y;

            mooreNodes.add(mooreNode);
        }
    }


    private void fillMooreEdges(Scanner scanner,
                                Integer inputsCount,
                                Integer nodesCount,
                                List<MooreNode> mooreNodes,
                                List<MooreEdge> mooreEdges) throws IOException {
        for (Integer i = 0; i < inputsCount; i++) {
            for (Integer j = 0; j < nodesCount; j++) {
                MooreEdge mooreEdge = new MooreEdge();

                mooreEdge.x = "x" + i;

                String q = scanner.next().trim();

                mooreEdge.to = findMooreNode(mooreNodes, q);
                mooreEdge.from = mooreNodes.get(j);

                mooreEdges.add(mooreEdge);
            }
        }
    }

    private MooreNode findMooreNode(List<MooreNode> mooreNodes, String q) throws IOException {
        Optional<MooreNode> to = mooreNodes
            .stream()
            .filter(mooreNode -> mooreNode.q.contains(q))
            .findFirst();

        if (to.isEmpty()) {
            throw new IOException("Node " + q + " not found");
        }
        return to.get();
    }

    public List<MealyEdge> mooreToMealy(List<MooreEdge> mooreEdges) {
        List<MealyEdge> mealyEdges = new ArrayList<>();

        for (MooreEdge mooreEdge : mooreEdges) {
            MealyNode mealyFrom = new MealyNode();
            mealyFrom.q = mooreEdge.from.q;

            MealyNode mealyTo = new MealyNode();
            mealyTo.q = mooreEdge.to.q;

            MealyEdge mealyEdge = new MealyEdge();
            mealyEdge.from = mealyFrom;
            mealyEdge.to = mealyTo;
            mealyEdge.x = mooreEdge.x;
            mealyEdge.y = mooreEdge.to.y;

            mealyEdges.add(mealyEdge);
        }

        return mealyEdges;
    }
}
