package org.dsa.datastructures.BTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BTreeIterative implements BTree {
    private final int degree;
    private Node root;

    public BTreeIterative(int degree) {
        this.degree = degree;
    }

    public Node getRoot() {
        return this.root;
    }

    @Override
    public boolean search(int key) {
        Node currentNode = root;
        while (currentNode != null) {
            if (currentNode.isLeaf()) {
                return contains(key, currentNode.getKeys());
            }

            if (contains(key, currentNode.getKeys())) {
                return true;
            }

            currentNode = currentNode.getChildren().get(lowerBound(key, currentNode.getKeys()));
        }
        return false;
    }

    @Override
    public boolean insert(int key) {
        Node parent = null;
        Node currentNode = root;
        boolean keyAlreadyInserted = false;
        if (search(key)) {
            return true;
        }

        if (currentNode == null) {
            root = new Node(new ArrayList<>(Collections.singleton(key)), new ArrayList<>());
            return true;
        }

        while (!keyAlreadyInserted) {
            List<Integer> keys = currentNode.getKeys();
            if (keys.size() == 2 * degree - 1) {
                int middleKey = keys.get(degree - 1);
                if (parent == null) {
                    splitRoot(currentNode);
                    currentNode = root;
                } else {
                    splitNode(parent, currentNode);
                    currentNode = key < middleKey
                            ? parent.getChildren().get(parent.getKeys().indexOf(middleKey))
                            : parent.getChildren().get(parent.getKeys().indexOf(middleKey) + 1);
                }
            }
            int idx = lowerBound(key, currentNode.getKeys());

            if (currentNode.isLeaf()) {
                currentNode.getKeys().add(idx, key);
                keyAlreadyInserted = true;
                continue;
            }
            parent = currentNode;
            currentNode = currentNode.getChildren().get(idx);
        }
        return keyAlreadyInserted;
    }

    private void splitNode(Node parent, Node node) {
        // Add the middle key to the parent Node
        int middleKey = node.getKeys().get(degree - 1);

        int idx = lowerBound(middleKey, parent.getKeys());
        parent.getKeys().add(idx, middleKey);

        // Create two new Nodes and add keys and children
        Node newLeftNode = new Node(
                new ArrayList<>(node.getKeys().subList(0, degree - 1)),
                node.isLeaf() ? new ArrayList<>() : new ArrayList<>(node.getChildren().subList(0, degree))
        );
        Node newRightNode = new Node(
                new ArrayList<>(node.getKeys().subList(degree, 2 * degree - 1)),
                node.isLeaf() ? new ArrayList<>() : new ArrayList<>(node.getChildren().subList(degree, 2 * degree))
        );

        // Remove original Node from the Parent
        parent.getChildren().remove(node);
        parent.getChildren().add(idx, newLeftNode);
        parent.getChildren().add(idx + 1, newRightNode);
    }

    private void splitRoot(Node root) {
        Node newRoot = new Node(
                new ArrayList<>(),
                new ArrayList<>()
        );

        int middleKey = root.getKeys().get(degree - 1);
        newRoot.getKeys().add(middleKey);

        Node newLeftNode = new Node(
                new ArrayList<>(root.getKeys().subList(0, degree - 1)),
                root.isLeaf() ? new ArrayList<>() : new ArrayList<>(root.getChildren().subList(0, degree))
        );
        Node newRightNode = new Node(
                new ArrayList<>(root.getKeys().subList(degree, 2 * degree - 1)),
                root.isLeaf() ? new ArrayList<>() : new ArrayList<>(root.getChildren().subList(degree, 2 * degree))
        );

        newRoot.getChildren().add(newLeftNode);
        newRoot.getChildren().add(newRightNode);
        this.root = newRoot;
    }

    /*
        Binary Search implementation.  Returns index of the key in the keys list
        If the element isn't present on the list it returns the idx where it should be inserted
     */
    private int binarySearch(int key, List<Integer> keys) {
        int low = 0, high = keys.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (keys.get(mid).intValue() == key) {
                return mid;
            }

            if (keys.get(mid).intValue() < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    private boolean contains(int key, List<Integer> keys) {
        int low = 0, high = keys.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (keys.get(mid) == key) return true;
            if (keys.get(mid) < key) low = mid + 1;
            else high = mid - 1;
        }
        return false;
    }

    private int lowerBound(int key, List<Integer> keys) {
        int low = 0, high = keys.size();
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (keys.get(mid) < key) low = mid + 1;
            else high = mid;
        }
        return low;
    }
}
