package com.juhezi.media.demo;

import java.util.List;

public class LeetCode {

    /**
     * 机器人走方格
     * dp
     *
     * @param x
     * @param y
     * @return
     */
    public static int countWays(int x, int y) {
        int[][] dp = new int[x][y];
        for (int i = 0; i < x; i++) {
            dp[i][0] = 1;
        }
        for (int j = 0; j < y; j++) {
            dp[0][j] = 1;
        }
        for (int i = 1; i < x; i++) {
            for (int j = 1; j < y; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[x - 1][y - 1];
    }

    /**
     * 插入排序
     * 最坏时间复杂度 O(n^2)
     */
    public static void insertSort(int[] array) {
        int current;
        for (int i = 0; i < array.length; i++) {
            current = array[i];
            int j;
            for (j = i - 1; j >= 0 && current < array[j]; j--) {
                array[j + 1] = array[j];    // 向后挪一位
            }
            array[j + 1] = current;
        }
    }

    /**
     * 快速排序
     *
     * @param array
     */
    public static void quickSort(int[] array) {
        internalQuickSort(array, 0, array.length - 1);
    }

    private static void internalQuickSort(int[] array, int left, int right) {
        int pivot, i, j;
        int temp;
        if (left < right) {
            i = left;
            j = right + 1;
            pivot = array[left];
            do {
                do {
                    i++;
                } while (array[i] < pivot);
                do {
                    j--;
                } while ((array[j] > pivot));
                if (i < j) {    // 交换 i 和 j
//                    System.out.println("开始交换：i：" + i + " j：" + j);
                    temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            } while (i < j);
//            System.out.println("交换结束：i：" + i + " j：" + j);
            // pivot 左边的都比 pivot 小，pivot 右边都比 pivot 大
            // 把 pivot 换到中间位置
            temp = array[left];
            array[left] = array[j];
            array[j] = temp;
//            System.out.println("---一轮排序结束--- left：" + left + " right " + right);
//            for (int item : array) {
//                System.out.print(item + "\t");
//            }
//            System.out.println();
            internalQuickSort(array, left, j - 1);
            internalQuickSort(array, j + 1, right);
        }
    }

    private int[] fail(String pat) {
        int[] failure = new int[pat.length()];
        failure[0] = -1;
        int i;
        for (int j = 1; j < pat.length(); j++) {
            i = failure[j - 1];
            while (pat.charAt(j) != pat.charAt(i + 1) && i >= 0) {
                i = failure[i];
            }
            if (pat.charAt(j) == pat.charAt(i + 1)) {
                failure[j] = i + 1;
            } else {
                failure[j] = -1;
            }
        }
        return failure;
    }

    /**
     * KMP 算法
     *
     * @param string
     * @param pat
     * @return
     */
    int pmatch(String string, String pat) {
        int[] failure = fail(pat);
        int i = 0, j = 0;
        int lens = string.length();
        int lenp = pat.length();
        while (i < lens && j < lenp) {
            if (string.charAt(i) == pat.charAt(j)) {
                i++;
                j++;
            } else if (j == 0) {
                i++;
            } else {
                j = failure[j - 1] + 1;
            }
        }
        return ((j == lenp) ? (i - lenp) : -1);
    }

    /**
     * 0 - 1 背包问题
     *
     * @param w 重量
     * @param v 价值
     * @param c 背包容量
     * @return
     */
    int packet(int[] w, int[] v, int c) {
        int length = w.length;
        int[][] memo = new int[2][c + 1];
        for (int j = 0; j <= c; j++) {
            memo[0][j] = j >= w[0] ? v[0] : 0;
        }
        for (int i = 1; i < length; i++) {
            for (int j = 0; j <= c; j++) {
                memo[i][j] = memo[i - 1][j];
                if (j >= w[i]) {
                    memo[i % 2][j] = Math.max(memo[i % 2][j],
                            v[i] + memo[(i - 1) % 2][j - w[i]]);
                }
            }
        }
        return memo[(length - 1) % 2][c];
    }

}
