/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-09 15:53:19
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-09 21:16:57
 * @Description:  
 */
package com.example.combine;

import java.util.Stack;

public class CombiningTree {

    Node[] leaves;

    public CombiningTree(int size) {
        Node[] nodes = new Node[size];
        // root node
        nodes[0] = new Node();

        for (int i = 1; i < nodes.length; ++i) {
            nodes[i] = new Node(nodes[(i - 1) / 2]);
        }

        // 完全二叉树性质
        leaves = new Node[(size + 1) / 2];
        for (int i = 0; i < leaves.length; ++i) {
            leaves[i] = nodes[nodes.length - i - 1];
        }
    }

    public int getAndIncrement(int threadID)  {
        
        int prior = -1;
        try {

            // Stack is thread-safe
            Stack<Node> stack = new Stack<Node>();
            // 当前线程对应的leaf node
            Node myLeaf = leaves[threadID/ 2];
            // 从leaf node启动
            Node node = myLeaf;

            // precombine stage
            while (node.precombine()) {
                node = node.parent;
            }
            Node stop = node;

            // combine stage
            node = myLeaf;
            int combined = 1;
            while (node != stop) {
                // 增量叠加
                combined = node.combine(combined);
                // 以相反的次序保存结点，随后将向下分配返回值
                stack.push(node);
                node = node.parent;
            }

            // op stage
            prior = stop.op(combined);

            // distribute
            while (!stack.empty()) {
                node = stack.pop();
                node.distribute(prior);
            }
        } catch (InterruptedException e) {
            System.err.println(e.getStackTrace());
        }
        return prior;
    }
}
