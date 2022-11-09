/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-09 00:45:16
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-09 22:54:23
 * @Description:  
 */
package com.example.combine;

public class Node {

    enum CStatus {
        IDLE,
        FIRST,
        SECOND,
        RESULT,
        ROOT
    };

    CStatus cStatus; // node status flag
    Node parent;
    int result;
    int firstValue;
    int secondValue;
    boolean locked;

    /*
     * root node
     */
    public Node() {
        cStatus = CStatus.ROOT;
        locked = false;
    }

    /*
     * non-root node
     */
    public Node(Node parent) {
        this.parent = parent;
        cStatus = CStatus.IDLE;
        locked = false;
    }

    /*
     * non-root node的主动线程和被动线程执行
     */
    synchronized boolean precombine() throws InterruptedException {
        // 长期同步，等待下一轮组合
        while (locked) 
            wait();
        switch (cStatus) {
            case IDLE: // 主动线程，将返回检查并进行组合
                cStatus = CStatus.FIRST;
                return true; // 继续向上推进
            case FIRST: // 被动线程，锁定结点，准备组合
                locked = true;
                cStatus = CStatus.SECOND;
                return false; // 停止推进
            case ROOT: // 到达根节点
                return false; // precombine阶段结束
            default:
                throw new UnsupportedOperationException("precombine: unexpected Node state " + cStatus);
        }
    }

    /*
     * non-root node的主动线程执行
     */
    synchronized int combine(int combined) throws InterruptedException {
        // 如果结点被锁，须等待被动线程执行op stage，设置secondValue并释放锁
        while (locked)
            wait();
        locked = true; // 尝试组合
        firstValue = combined;
        switch (cStatus) {
            case FIRST: // 仅一个线程访问过该结点
                return firstValue;
            case SECOND: // 被动线程到达，进行组合
                return firstValue + secondValue;
            default:
                throw new UnsupportedOperationException("combine: unexpected Node state " + cStatus);
        }
    }

    /*
     * root node和non-root的被动线程执行
     */
    synchronized int op(int combined) throws InterruptedException {
        switch (cStatus) {
            case ROOT:
                int prior = result; // 旧值作为返回值
                result += combined;
                return prior;
            case SECOND: // 当前线程是 stop 结点的被动线程
                secondValue = combined;
                locked = false;
                notifyAll();
                // 等待主动线程组合
                while (cStatus != CStatus.RESULT)
                    wait();
                // 释放锁，重置结点，唤醒其它线程启动下一轮组合
                locked = false;
                cStatus = CStatus.IDLE;
                notifyAll();
                
                return result;
            default:
                throw new UnsupportedOperationException("op: unexpected Node state " + cStatus);
        }
    }

    /*
     * non-root node的主动线程执行
     */
    synchronized void distribute(int prior) {
        switch (cStatus) {
            case FIRST: // 没有第二个进程进入，解锁并重置
                cStatus = CStatus.IDLE;
                locked = false;
                break;
            case SECOND: // 通知第二个进程 result 可用
                result = prior + firstValue;
                cStatus = CStatus.RESULT;
                break;
            default:
                throw new UnsupportedOperationException("distribute: unexpected Node state " + cStatus);
        }
        notifyAll();
    }
}
