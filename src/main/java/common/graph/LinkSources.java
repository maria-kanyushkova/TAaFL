package common.graph;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.Node;
import common.models.MealyEdge;
import common.models.MooreEdge;

import java.util.ArrayList;
import java.util.List;

import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class LinkSources {
    public List<LinkSource> createMooreLinkSources(List<MooreEdge> mooreEdges) {
        List<LinkSource> sources = new ArrayList<>();
        for (MooreEdge mooreEdge : mooreEdges) {
            Label label = Label.of(mooreEdge.x);
            Node from = node(mooreEdge.from.q).with("xlabel", mooreEdge.from.y);
            Node to = node(mooreEdge.to.q);
            sources.add(from.link(to(to).with(label)));
        }
        return sources;
    }

    public List<LinkSource> createMealyLinkSources(List<MealyEdge> mealyEdges) {
        List<LinkSource> sources = new ArrayList<>();
        for (MealyEdge mealyEdge : mealyEdges) {
            Label label = Label.of(mealyEdge.x + "/" + mealyEdge.y);
            sources.add(node(mealyEdge.from.q).link(to(node(mealyEdge.to.q)).with(label)));
        }
        return sources;
    }
}
