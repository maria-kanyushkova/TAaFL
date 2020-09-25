package common.graph;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.LinkSource;
import common.models.MealyEdge;
import common.models.MooreEdge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.graph;

public class GraphPrinter {
    public void printGraph(String graphTitle, List<LinkSource> linkSources, String pathToFile) {
        Graph g = graph(graphTitle)
            .directed()
            .with(linkSources);

        try {
            Graphviz
                .fromGraph(g)
                .width(1024)
                .render(Format.PNG)
                .toFile(new File(pathToFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printMealyTable(Integer inputsCount,
                                List<MealyEdge> mealyEdges,
                                String pathToOutput) throws IOException {
        File output = new File(pathToOutput);
        try (FileWriter writer = new FileWriter(output)) {
            int index = 0;
            for (MealyEdge mealyEdge : mealyEdges) {
                writer.append(mealyEdge.to.q).append(mealyEdge.y);
                if ((index + 1) % (mealyEdges.size() / inputsCount) == 0) {
                    writer.append("\n");
                } else {
                    writer.append(" ");
                }
                ++index;
            }
        }
    }

    public void printMooreTable(Integer inputsCount,
                                List<MooreEdge> mooreEdges,
                                String pathToOutput) throws IOException {
        File output = new File(pathToOutput);
        var sortedMooreEdges = mooreEdges.stream().sorted((left, right) -> {
            int a = Integer.parseInt(left.x.substring(1));
            int b = Integer.parseInt(right.x.substring(1));
            return Integer.compare(a, b);
        }).collect(Collectors.toList());
        try (FileWriter writer = new FileWriter(output)) {
            int index = 0;
            for (MooreEdge mooreEdge : sortedMooreEdges) {
                writer.append(mooreEdge.to.q);
                if ((index + 1) % (mooreEdges.size() / inputsCount) == 0) {
                    writer.append("\n");
                } else {
                    writer.append(" ");
                }
                ++index;
            }
        }
    }
}
