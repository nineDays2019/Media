package me.juhezi.eternal.other

import me.juhezi.eternal.extension.e
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.global.judge
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 支持同步、异步
 *
 *
 *
 */
class EShell {

    var running = false
        private set

    var callback: EShellCallback? = null
    private lateinit var process: Process
    private lateinit var successBufferReader: BufferedReader
    private lateinit var errorBufferReader: BufferedReader
    private var returnCode = -1
    @Volatile
    private var costTime: Long = 0L

    private var successString = ""
    private var errorString = ""
    private var startTime: Long = 0L

    /**
     * 获取输出结果（全部的）
     */
    fun getResult(): ShellResult? =
        if (running)
            null
        else
            ShellResult(successString + "\n" + errorString, StdType.MERGE)

    /**
     * 获取命令行返回值
     */
    fun getReturnCode() = returnCode

    /**
     * 耗时
     * 单位：微妙
     */
    fun getCostTimeMs() = costTime

    /**
     * 同步执行
     */
    fun run(command: String, timeoutMs: Long = -1) = internalRun(command, timeoutMs, false)

    /**
     * 异步执行
     */
    fun runAsync(command: String, timeoutMs: Long = -1) = internalRun(command, timeoutMs, true)

    private fun prepareProcess(command: String): Process? {
        return try {
            Runtime.getRuntime().exec(command)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupProcess() {
        successBufferReader = BufferedReader(InputStreamReader(process.inputStream))
        errorBufferReader = BufferedReader(InputStreamReader(process.errorStream))
    }

    private fun internalRun(command: String, timeoutMs: Long, async: Boolean): EShell {
        i("Command is $command")
        if (command.isEmpty()) return this
        val tempProcess = prepareProcess(command) ?: return this
        startTime = System.currentTimeMillis()
        process = tempProcess

        setupProcess()

        if (timeoutMs > 0) {
            // 控制线程（超时逻辑）
            Thread(Runnable {

                try {
                    Thread.sleep(timeoutMs)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    // 返回 0 是正常
                    // 如果子进程还没结束，就会报异常
                    val returnCode = process.exitValue()
                    i("Return Code is $returnCode")
                } catch (e: Exception) {
                    e.printStackTrace()
                    process.destroy()
                }

            }).start()
        }

        val inputThread = Thread(Runnable {
            var line: String? = null
            successString = buildString {
                while (judge {
                        line = successBufferReader.readLine()
                        line != null
                    }) {
                    append(line)
                    append("\n")
                }
            }

        })
        inputThread.start()

        val errorThread = Thread(Runnable {
            errorString = buildString {
                var line: String? = null
                while (judge {
                        line = errorBufferReader.readLine()
                        line != null
                    }) {
                    append(line)
                    append("\n")
                }
            }
        })
        errorThread.start()

        // 控制线程
        // 等待子进程
        val controlThread = Thread(Runnable {

            try {
                inputThread.join()
                errorThread.join()
                returnCode = process.waitFor()
                e("执行完成，Code is $returnCode")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                costTime = System.currentTimeMillis() - startTime
                running = false
            }

            // 调用异步回调
            if (async) {
                callback?.onFinish(ShellResult("HelloWorld"), returnCode, costTime)
            }

        })
        controlThread.start()

        if (!async) {   // 同步，需要等子线程执行完毕
            controlThread.join()
        }
        return this
    }

}

interface EShellCallback {

    /**
     * 实时更新输出内容
     */
    fun onOutputUpdate(order: Int, shellResult: ShellResult)

    /**
     * @param shellResult 整体的输出内容
     * @param returnCode Command 执行返回值
     * @param costTimeMs 耗时
     */
    fun onFinish(shellResult: ShellResult, returnCode: Int, costTimeMs: Long)
}

data class ShellResult(val message: String, val type: StdType = StdType.STDOUT)

enum class StdType {
    STDOUT, // 标准输出
    STDERR,  // 标准错误
    MERGE   // 合并之后
}

/*
梳理一下思路
1. 同步执行
    process.waitFor() 获取运行结果，然后调用 getResultCode
    当前进度：
    a. 返回码返回正确
    b. 耗时正确
    c. 返回结果正确
2. 异步执行
 */