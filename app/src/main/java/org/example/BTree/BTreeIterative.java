package org.example.BTree;

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
                if (currentNode.isLeaf()) {
                    return binarySearch(key, currentNode.getKeys()) != -1;
                }
            }

            if (currentNode.getKeys().getLast() < key) {
                currentNode = currentNode.getChildren().getLast();
                continue;
            }

            int idx = binarySearch(key, currentNode.getKeys());
            if (idx != -1) return true;

            int childIdx = 0;
            List<Integer> keys = currentNode.getKeys();
            while (childIdx < keys.size() && key > keys.get(childIdx)) {
                childIdx++;
            }
            currentNode = currentNode.getChildren().get(childIdx);
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
            if (currentNode.getKeys().size() == 2 * degree - 1) {
                int middleKey = currentNode.getKeys().get(degree - 1);
                if (parent == null) {
                    splitRoot(currentNode, key);
                    currentNode = root;
                } else {
                    splitNode(parent, currentNode, key);
                    currentNode = key < middleKey
                            ? parent.getChildren().get(parent.getKeys().indexOf(middleKey))
                            : parent.getChildren().get(parent.getKeys().indexOf(middleKey) + 1);
                }
            }

            if (currentNode.isLeaf()) {
                for (int k : currentNode.getKeys()) {
                    if (k > key) {
                        currentNode.getKeys().add(currentNode.getKeys().indexOf(k), key);
                        keyAlreadyInserted = true;
                        break;
                    }
                }
                if (!keyAlreadyInserted) {
                    currentNode.getKeys().addLast(key);
                    keyAlreadyInserted = true;
                }
                continue;
            }

            if (currentNode.getKeys().getLast() < key) {
                parent = currentNode;
                currentNode = currentNode.getChildren().getLast();
                continue;
            }

            if (currentNode.getKeys().getFirst() > key) {
                parent = currentNode;
                currentNode = currentNode.getChildren().getFirst();
                continue;
            }

            for (int i = 1; i < currentNode.getKeys().size(); i++) {
                if (key < currentNode.getKeys().get(i)) {
                    parent = currentNode;
                    currentNode = currentNode.getChildren().get(i);
                    break;
                }
            }
        }
        return keyAlreadyInserted;
    }

    private void splitNode(Node parent, Node node, int key) {
        // Add the middle key to the parent Node
        int middleKey = node.getKeys().get(degree - 1);

        for (int k : parent.getKeys()) {
            if (middleKey < k) {
                parent.getKeys().add(parent.getKeys().indexOf(k), middleKey);
                break;
            }
        }

        if (parent.getKeys().getLast() < middleKey) parent.getKeys().addLast(middleKey);

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

        parent.getChildren().add(parent.getKeys().indexOf(middleKey), newLeftNode);
        parent.getChildren().add(parent.getKeys().indexOf(middleKey) + 1, newRightNode);
    }

    private void splitRoot(Node root, int key) {
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
        If output is -1, the number was not found.
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
        return -1;
    }
}
