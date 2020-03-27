package com.juhezi.media

import com.juhezi.media.demo.*
import org.junit.Test

import org.junit.Assert.*
import java.util.concurrent.Semaphore

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun produce_customer() {
        val lock = Object()
        val producer = Producer(lock)
        val customer = Customer(lock)

        Thread {
            var count = 0
            while (count < 20) {
                count++
                producer.setValue()
            }
        }.start()

        Thread {
            var count = 0
            while (count < 20) {
                count++
                customer.getValue()
            }
        }.start()

    }

    @Test
    fun test() {
        for (item in twoSum(intArrayOf(3, 2, 4), 6)) {
            println(item)
        }
    }

    // 最简单的写法，想想怎么优化
    fun twoSum(nums: IntArray, target: Int): IntArray {
        for (i in nums.indices) {
            for (j in i + 1 until nums.size) {
                if (nums[i] + nums[j] == target) {
                    return intArrayOf(i, j)
                }
            }
        }
        return intArrayOf(1, 2)
    }

    @Test
    fun testFooBar() {
        val instance = FooBar(10)
        Thread {
            instance.foo {
                print("Foo")
            }
        }.start()
        Thread {
            instance.bar {
                print("Bar")
            }
        }.start()
    }

    @Test
    fun testZeroEvenOdd() {
        val instance = ZeroEvenOdd(5)
        Thread {
            instance.zero {
                print(it)
            }
        }.start()
        Thread {
            instance.odd {
                print(it)
            }
        }.start()
        Thread {
            instance.even {
                print(it)
            }
        }.start()
    }

    /**
     * 研究信号量的使用方法
     * get it
     */
    @Test
    fun testSemaphore() {
        val semaphore = Semaphore(3)
        for (i in 0..10) {
            if (semaphore.tryAcquire()) {
                println("index: $i")
            } else {
                println("index: $i Error")
            }
        }
    }

    /**
     * 测试🌲
     */
    @Test
    fun testTree() {
        val tree = TreeNode(
            1,
            TreeNode(
                2,
                TreeNode(3),
                TreeNode(4)
            ),
            TreeNode(
                5,
                TreeNode(6),
                TreeNode(7)
            )
        )
        println("中序遍历")
        inOrder(tree) {
            print(" $it ")
        }
        println("\n前序遍历")
        preOrder(tree) {
            print(" $it ")
        }
        println("\n后序遍历")
        postOrder(tree) {
            print(" $it ")
        }
        println("\nBFS")
        bfs(tree) {
            print(" $it ")
        }
        println("\n构建二叉树")
        println(
            Solution().buildTree(
                intArrayOf(1, 2, 3, 4, 5, 6, 7),
                intArrayOf(3, 2, 4, 1, 6, 5, 7)
            )
        )
    }

}
