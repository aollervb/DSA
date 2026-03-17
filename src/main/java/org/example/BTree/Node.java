package org.example.BTree;

import java.util.List;

public class Node {
    private List<Integer> keys;
    private List<Node> children;
    private boolean isLeaf;

    public Node(List<Integer> keys, List<Node> children) {
        this.keys = keys;
        this.children = children;
    }

    public Node(){}

    /*
               Getters and Setters
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    public List<Integer> getKeys() {
        return keys;
    }

    public void setKeys(List<Integer> keys) {
        this.keys = keys;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }
}
