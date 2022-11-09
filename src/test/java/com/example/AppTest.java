/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-09 00:22:05
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-09 23:43:09
 * @Description:  
 */
package com.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.combine.CombiningTree;

/**
 * Unit test for simple App.
 */
public class AppTest {
    final static int THREAD_NUM = 10;
    final static int TASK_PER_THREAD = 100000;
    CombiningTree tree;
    AtomicBoolean[] counterTest;

    @BeforeEach
    private void init() {
        tree = new CombiningTree(THREAD_NUM);
        counterTest = new AtomicBoolean[THREAD_NUM * TASK_PER_THREAD];
        for (int i = 0; i < counterTest.length; ++i) {
            counterTest[i] = new AtomicBoolean(false);
        }
    }

    @Test
    public void test() throws InterruptedException {

        Thread[] threads = new Thread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i] = new MyThread(i);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].start();
        }

        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].join();
        }

        long end = System.currentTimeMillis();
        long cost = end - start;
        System.out.println("Combining tree cost time: " + (cost)+ "ms");

        assertTrue(check());
        serial();
    }

    private void serial() {
        for (int i = 0; i < counterTest.length; ++i) {
            counterTest[i] = new AtomicBoolean(false);
        }
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_NUM * TASK_PER_THREAD; ++i) {
            counterTest[i].compareAndSet(false, true);
        }
        long end = System.currentTimeMillis();
        long cost = end - start;
        System.out.println("Sequential algorithm cost time: " + (cost) + "ms");
    }

    private boolean check() {
        for (AtomicBoolean count : counterTest) {
            if (!count.get()) {
                System.err.println("[ERROR]: value " + count + " miss");
                return false;
            }
        }
        return true;
    }

    class MyThread extends Thread {
        int threadID;

        MyThread(int id) {
            this.threadID = id;
        }

        @Override
        public void run() {
            for (int i = 0; i < TASK_PER_THREAD; ++i) {
                int count = -1;

                count = tree.getAndIncrement(threadID);
                if (count >= 0) {
                    if (!counterTest[count].compareAndSet(false, true)) {
                        System.err.println("[ERROR]: duplicate value " + count);
                    }
                } else {
                    System.err.println("[ERROR]: getAndIncrement failed");
                }
            }
            // System.out.println("Thread " + threadID + " complete.");
        }
    }
}
