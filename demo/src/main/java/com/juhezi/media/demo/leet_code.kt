package com.juhezi.media.demo

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


data class TreeNode(
    var value: Int, var left: TreeNode? = null,
    var right: TreeNode? = null
)

data class ListNode(var value: Int, var next: ListNode? = null)

class Solution {

    // 根据前序遍历和中序遍历重建二叉树
    fun buildTree(preorder: IntArray /*前序遍历*/, inorder: IntArray/*中序遍历*/): TreeNode? {
        return initTree(preorder, 0, preorder.size, inorder, 0, inorder.size)
    }

    fun initTree(
        preorder: IntArray,
        preStart: Int,
        preEnd: Int,
        inorder: IntArray,
        inStart: Int,
        inEnd: Int
    ): TreeNode? {
        if (preStart >= preEnd || inStart >= inEnd) {
            return null
        }
        val rootValue = preorder[preStart]    // 前序遍历第一个数字就是根节点
        val root = TreeNode(rootValue)  // 根节点

        // 根节点所在的位置
        val rootIndex = indexOf(inorder, rootValue, inStart, inEnd)
        val leftLength = rootIndex - inStart    // 左子树对应的长度

        // 构建左子树
        root.left = initTree(
            preorder, preStart + 1, preStart + leftLength + 1,
            inorder, inStart, inStart + leftLength
        )
        // 构建右子树
        root.right = initTree(
            preorder, preStart + 1 + leftLength, preEnd,
            inorder, rootIndex + 1, inEnd
        )
        return root
    }

    // 从中序遍历中找出根节点对应的位置
    fun indexOf(inorder: IntArray, rootValue: Int, begin: Int, end: Int): Int {
        for (i in begin until end) {
            if (inorder[i] == rootValue) {
                return i
            }
        }
        return -1
    }

    // 图的 BFS
    // 多源广度优先搜索
    fun maxDistance(grid: Array<IntArray>): Int {

        fun isOcean(current: Pair<Int, Int>): Boolean {
            val max = grid.size
            if (current.first < 0)
                return false
            if (current.first >= max)
                return false
            if (current.second < 0)
                return false
            if (current.second >= max)
                return false
            if (grid[current.first][current.second] != 0)
                return false
            return true
        }

        val queue = LinkedList<Pair<Int, Int>>()
        grid.forEachIndexed { x, line ->
            line.forEachIndexed { y, value ->
                if (value == 1) {
                    queue.offer(x to y)
                }
            }
        }

        val dx = intArrayOf(0, 0, 1, -1)
        val dy = intArrayOf(1, -1, 0, 0)

        var hasOcean = false
        var current: Pair<Int, Int>? = null

        while (!queue.isEmpty()) {
            current = queue.poll()

            for (i in 0..3) {
                val newX = current.first + dx[i]
                val newY = current.second + dy[i]
                if (!isOcean(newX to newY)) {
                    continue
                }
                grid[newX][newY] = grid[current.first][current.second] + 1
                hasOcean = true
                queue.offer(newX to newY)
            }

        }

        if (!hasOcean || current == null) {
            return -1
        }
        return grid[current.first][current.second] - 1
    }

    // 约瑟夫环
    fun lastRemaining(n: Int, m: Int): Int {
        val list = ArrayList<Int>()
        for (i in 0 until n) {
            list.add(i)
        }
        if (list.isEmpty()) {
            return -1
        }
        var index = 0
        while (list.size != 1) {
            index = (index + m - 1) % list.size
            list.removeAt(index)
        }
        return list[0]
    }

    // 约瑟夫环，倒推
    fun lastRemaining2(n: Int, m: Int): Int {
        var ans = 0
        for (i in 2..n) {
            ans = (ans + m) % i
        }
        return ans
    }

}


/**
 * 翻转链表
 * 一定要他妈记住了
 * 淦
 */
fun reverseList(head: ListNode?): ListNode? {
    var prev: ListNode? = head
    var current: ListNode? = head?.next
    var next: ListNode?
    while (current != null) {
        next = current.next
        current.next = prev
        prev = current
        current = next
    }
    head?.next = null
    return prev
}


/**
 * 中序遍历
 */
fun inOrder(tree: TreeNode?, closure: (Int) -> Unit) {
    if (tree != null) {
        inOrder(tree.left, closure)
        closure(tree.value)
        inOrder(tree.right, closure)
    }
}

/**
 * 前序遍历
 */
fun preOrder(tree: TreeNode?, closure: (Int) -> Unit) {
    if (tree != null) {
        closure(tree.value)
        preOrder(tree.left, closure)
        preOrder(tree.right, closure)
    }
}

/**
 * 后续遍历
 */
fun postOrder(tree: TreeNode?, closure: (Int) -> Unit) {
    if (tree != null) {
        postOrder(tree.left, closure)
        postOrder(tree.right, closure)
        closure(tree.value)
    }
}

/**
 * 深度优先遍历
 * 等于前序优先遍历
 */
fun dfs(tree: TreeNode?, closure: (Int) -> Unit) = preOrder(tree, closure)

/**
 * 广度优先遍历
 */
fun bfs(tree: TreeNode, closure: (Int) -> Unit) {
    val queue = LinkedList<TreeNode>()
    queue.offer(tree)
    while (!queue.isEmpty()) {
        var node = queue.poll()
        closure(node.value)
        if (node.left != null) {
            queue.offer(node.left)
        }
        if (node.right != null) {
            queue.offer(node.right)
        }
    }
}

fun reverseWords(s: String): String {
    var array = s.trim().split(" ")
    var result = ""
    for (i in array.lastIndex downTo 0) {
        if (array[i].isNotEmpty()) {
            result += array[i]
            if (i != 0) {
                result += " "
            }
        }

    }
    return result
}

// 最大子序和
// sum[i] 的定义是以第 i 个元素结尾且和最大的连续子数组
fun maxSubArray(nums: IntArray): Int {
    var sum = nums[0]
    var n = nums[0]
    for (i in 1 until nums.size) {
        if (n > 0) {
            n += nums[i]
        } else {
            n = nums[i]
        }
        if (sum < n) {
            sum = n
        }
    }
    return sum
}

/**
 * 最长公共子序列
 */
fun longestCommonSubsequence(text1: String, text2: String): Int {
    val dp = Array(text1.length + 1) {
        IntArray(text2.length + 1)
    }

    for (i in 1..text1.length) {
        for (j in 1..text2.length) {
            if (text1[i - 1] == text2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1] + 1
            } else {
                dp[i][j] = dp[i - 1][j].coerceAtLeast(dp[i][j - 1])
            }
        }
    }
    return dp[text1.length][text2.length]
}

/**
 * 最长连续序列
 * 要求时间复杂度是 O(n)
 */
fun longestConsecutive(nums: IntArray): Int {
    var max = 0
    var map = HashMap<Int, Int>(nums.size)
    for (num in nums) {
        if (map.containsKey(num)) {
            continue
        }
        map[num] = 1
        if (map.containsKey(num - 1)) {
            map[num] = map[num - 1]!! + map[num]!!
        }
        if (map.containsKey(num + 1)) {
            map[num] = map[num + 1]!! + map[num]!!
        }
        var pre = num - 1
        var next = num + 1
        while (map.containsKey(pre)) {
            map[pre] = map[num]!!
            pre--
        }
        while (map.containsKey(next)) {
            map[next] = map[num]!!
            next++
        }
//        println("num is $num")
//        map.forEach {
//            print("[${it.key}]:${it.value} ")
//        }
//        println("\n---------------")
        if (map[num]!! > max) {
            max = map[num]!!
        }
    }
    return max
}

fun intersection(start1: IntArray, end1: IntArray, start2: IntArray, end2: IntArray): DoubleArray {
    /**
     * 计算 ax + b
     */
    fun generateParam(start: IntArray, end: IntArray): FloatArray {
        val a = (start[1] - end[1]) / (start[0] - end[0]).toFloat()
        val b = start[1] - start[0] * a
        return floatArrayOf(a, b)
    }

    val param1 = generateParam(start1, end1)
    val param2 = generateParam(start2, end2)

    if (param1[0] == param2[0]) {
        return doubleArrayOf()
    }

    val x = (param2[1] - param1[1]) / (param1[0] - param2[0])
    val y = param1[0] * x + param1[1]
    return doubleArrayOf(x.toDouble(), y.toDouble())
}