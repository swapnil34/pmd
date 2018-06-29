/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.util.codearea;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.fxdesigner.util.codearea.NodeStyleSpan.PositionSnapshot;


/**
 * Collection of nodes that share the same style. In case of overlap,
 * the nested ones gain css classes like depth-1, depth-2, etc. A
 * collection can be overlaid into a single span in one pass using
 * {@link #toSpans()}.
 *
 * @author Clément Fournier
 * @since 6.5.0
 */
public class UniformStyleCollection {

    private static final Map<Set<String>, Map<Integer, Map<Boolean, Set<String>>>> DEPTH_STYLE_CACHE = new HashMap<>();

    private final Set<String> style;
    // sorted in document order
    private final List<NodeStyleSpan> nodes;
    private StyleSpans<Collection<String>> spanCache;

    public UniformStyleCollection(Set<String> style, Collection<NodeStyleSpan> ns) {
        this.style = style;
        this.nodes = new ArrayList<>(ns);
        nodes.sort(NodeStyleSpan.documentOrderComparator());
    }


    public boolean isEmpty() {
        return nodes.isEmpty();
    }


    public Set<String> getStyle() {
        return style;
    }


    public UniformStyleCollection merge(UniformStyleCollection collection) {
        assert collection.getStyle().equals(getStyle());

        if (collection.isEmpty()) {
            return this;
        } else if (this.isEmpty()) {
            return collection;
        } else {
            Set<NodeStyleSpan> merged = new HashSet<>(nodes);
            merged.addAll(collection.nodes);

            return new UniformStyleCollection(style, merged);
        }
    }


    private boolean useInlineHighlight(Node node) {
        return node.getBeginLine() == node.getEndLine();
    }


    private Set<String> styleForDepth(int depth, PositionSnapshot n) {
        return styleForDepth(depth, n != null && useInlineHighlight(n.getNode()));
    }


    private Set<String> styleForDepth(int depth, boolean inlineHighlight) {
        if (depth < 0) {
            // that's the style when we're outside any node
            return Collections.emptySet();
        } else {
            // Caching reduces the number of sets used by this step of the overlaying routine to
            // only a few. The number is probably blowing up during the actual spans overlaying
            // in StyleContext#recomputePainting
            DEPTH_STYLE_CACHE.putIfAbsent(style, new HashMap<>());
            Map<Integer, Map<Boolean, Set<String>>> depthToStyle = DEPTH_STYLE_CACHE.get(style);

            depthToStyle.putIfAbsent(depth, new HashMap<>());
            Map<Boolean, Set<String>> isInlineToStyle = depthToStyle.get(depth);

            if (isInlineToStyle.containsKey(inlineHighlight)) {
                return isInlineToStyle.get(inlineHighlight);
            }

            Set<String> s = new HashSet<>(style);
            s.add("depth-" + depth);
            if (inlineHighlight) {
                // inline highlight can be used to add boxing around a node if it wouldn't be ugly
                s.add("inline-highlight");
            }
            isInlineToStyle.put(inlineHighlight, s);
            return s;
        }
    }


    /**
     * Overlays all the nodes in this collection into a single StyleSpans.
     * This algorithm makes the strong assumption that the nodes can be
     * ordered as a tree, that is, given two nodes n and m, then one of the
     * following holds true:
     * - m and n are disjoint
     * - m is entirely contained within n, or the reverse is true
     *
     * E.g. [    m        ] but not [  m  ]
     *        [ n ] [ n' ]              [   n   ]
     */
    public StyleSpans<Collection<String>> toSpans() {
        // We cache the result so that eg if only the focus node changes,
        // we don't have to overlay all XPath results again
        if (spanCache == null) {
            spanCache = buildSpans();
        }
        return spanCache;
    }

    public StyleSpans<Collection<String>> buildSpans() {

        if (nodes.isEmpty()) {
            return StyleSpans.singleton(Collections.emptyList(), 0);
        } else if (nodes.size() == 1) {
            PositionSnapshot snapshot = nodes.get(0).snapshot();
            return new StyleSpansBuilder<Collection<String>>().add(Collections.emptyList(), snapshot.getBeginIndex())
                                                              .add(styleForDepth(0, snapshot), snapshot.getLength())
                                                              // we don't bother adding the remainder
                                                              .create();
        }

        final StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();

        // stores the parents of the node we're in, to account for nesting depth
        final Deque<PositionSnapshot> overlappingNodes = new ArrayDeque<>();
        PositionSnapshot previous = null;
        int lastSpanEnd = 0;

        for (NodeStyleSpan span : nodes) { // sorted in document order

            PositionSnapshot current = span.snapshot();
            if (current == null) {
                continue;
            }

            // first iteration
            if (previous == null) {
                previous = current;
                builder.add(Collections.emptyList(), previous.getBeginIndex());
                lastSpanEnd = previous.getBeginIndex();
                continue;
            }

            if (previous.getEndIndex() > current.getBeginIndex()) {
                // The current overlaps with the previous

                // This part sometimes throws exceptions when the text changes while the computation is in progress
                // In practice, they can totally be ignored since the highlighting will be recomputed next time
                // the AST is refreshed, which *will* happen... because the text is being edited

                // gap
                builder.add(styleForDepth(overlappingNodes.size() - 1, overlappingNodes.peek()), previous.getBeginIndex() - lastSpanEnd);
                // Part between the start of the previous node and the start of the current one.
                // this is the underscored part [_[ ] ]
                // The current node will be styled on the next iteration.
                builder.add(styleForDepth(overlappingNodes.size(), current), current.getBeginIndex() - previous.getBeginIndex());
                lastSpanEnd = current.getBeginIndex();

                overlappingNodes.addFirst(previous);

                previous = current;
                continue;
            } else {
                // no overlap, the previous span can be added

                // the depth - 1 is for the gap
                builder.add(styleForDepth(overlappingNodes.size() - 1, overlappingNodes.peek()), previous.getBeginIndex() - lastSpanEnd);
                // previous node
                builder.add(styleForDepth(overlappingNodes.size(), previous), previous.getLength());
                lastSpanEnd = previous.getEndIndex();
                previous = current;
            }

            // Check whether some of the enclosing spans end between the end of the previous and the beginning of the current
            Iterator<PositionSnapshot> overlaps = overlappingNodes.iterator();
            while (overlaps.hasNext()) {
                PositionSnapshot enclosing = overlaps.next();
                if (enclosing.getEndIndex() < current.getBeginIndex()) {
                    overlaps.remove();
                    // this is the underscored part [ [ ]_]
                    builder.add(styleForDepth(overlappingNodes.size(), enclosing), enclosing.getEndIndex() - lastSpanEnd);
                    lastSpanEnd = enclosing.getEndIndex();
                }
            }
        }

        // gap
        builder.add(styleForDepth(overlappingNodes.size() - 1, overlappingNodes.peek()), previous.getBeginIndex() - lastSpanEnd);
        // last node
        builder.add(styleForDepth(overlappingNodes.size(), previous), previous.getLength());
        lastSpanEnd = previous.getEndIndex();

        // close the remaining enclosing contexts
        int depth = overlappingNodes.size();
        for (PositionSnapshot enclosing : overlappingNodes) {
            depth--;
            builder.add(styleForDepth(depth, enclosing), enclosing.getEndIndex() - lastSpanEnd);
            lastSpanEnd = enclosing.getEndIndex();
        }

        return builder.create();
    }


    /** Returns an empty style collection. */
    public static UniformStyleCollection empty() {
        return new UniformStyleCollection(Collections.emptySet(), Collections.emptySet());
    }
}
