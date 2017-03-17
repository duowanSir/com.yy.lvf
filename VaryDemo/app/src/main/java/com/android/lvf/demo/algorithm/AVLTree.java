package com.android.lvf.demo.algorithm;

import java.util.List;

/**
 * Created by slowergun on 2017.03.17.
 */

public class AVLTree {
    private AVLTreeNode root;//根

    public static <T> AVLTreeNode generateAVLTree(List<AVLTreeNode<T>> nodes) {
        if (nodes == null || nodes.isEmpty())
            return null;
        AVLTreeNode root = null;
        for (int i = 0; i < nodes.size(); i++) {
            AVLTreeNode<T> j = nodes.get(i);
            if (root == null) {
                root = j;
                j.setBalanceFactor(0);
                continue;
            }
            AVLTreeNode next = root;
            boolean flag = true;
            while (flag) {
                if (j.compareTo(next) == 0) {
                    next.setWeight(next.getWeight() + 1);
                    flag = false;
                    continue;
                }
                if (j.compareTo(next) > 0) {
                    if (next.getRight() == null) {
                        next.setBalanceFactor(next.getBalanceFactor() + 1);
                        next.setRight(j);
                        flag = false;
                        continue;
                    }
                    next = next.getRight();
                    continue;
                }
                if (j.compareTo(next) < 0) {
                    if (next.getLeft() == null) {
                        next.setBalanceFactor(next.getBalanceFactor() + 1);
                        next.setLeft(j);
                        flag = false;
                        continue;
                    }
                    next = next.getLeft();
                    continue;
                }
            }
        }
        return null;
    }

    public static class AVLTreeNode<S> implements Comparable<AVLTreeNode> {
        private S           key;
        private int         weight;// 重复次数
        private int         balanceFactor;
        private AVLTreeNode left;//左子树
        private AVLTreeNode right;//右子树

        @Override
        public int compareTo(AVLTreeNode another) {
            return 0;
        }

        public S max(S another) {
            return null;
        }

        public S min(S another) {
            return null;
        }

        public S getKey() {
            return key;
        }

        public void setKey(S key) {
            this.key = key;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getBalanceFactor() {
            return balanceFactor;
        }

        public void setBalanceFactor(int balanceFactor) {
            this.balanceFactor = balanceFactor;
        }

        public AVLTreeNode getLeft() {
            return left;
        }

        public void setLeft(AVLTreeNode left) {
            this.left = left;
        }

        public AVLTreeNode getRight() {
            return right;
        }

        public void setRight(AVLTreeNode right) {
            this.right = right;
        }
    }
}
