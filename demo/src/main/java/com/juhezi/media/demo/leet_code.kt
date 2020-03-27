package com.juhezi.media.demo

import java.util.*

data class TreeNode(
    var value: Int, var left: TreeNode? = null,
    var right: TreeNode? = null
)

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

