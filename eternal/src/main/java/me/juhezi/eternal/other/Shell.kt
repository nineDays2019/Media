package me.juhezi.eternal.other

import me.juhezi.eternal.extension.i
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

// 待测试
class Shell(private var sync: Boolean = true) {

    // shell 进程
    private var process: Process? = null
    // 对应的三个流
    private var successResult: BufferedReader? = null
    private var errorResult: BufferedReader? = null
    private var os: DataOutputStream? = null
    private var running = false
        get() = field
    private var lock: ReadWriteLock = ReentrantReadWriteLock()

    private var result = StringBuffer()

    fun getResult(): String {
        val readLock = lock.readLock()
        readLock.lock()
        try {
            return String(result)
        } finally {
            readLock.unlock()
        }
    }

    fun run(command: java.lang.String, timeout: Int): Shell {
        i("Command is $command")
        if (command.isEmpty) return this
        try {
            process = Runtime.getRuntime().exec("sh")
        } catch (e: Exception) {
            e.printStackTrace()
            return this
        }

        running = true
        successResult = BufferedReader(InputStreamReader(process!!.inputStream))
        errorResult = BufferedReader(InputStreamReader(process!!.errorStream))
        os = DataOutputStream(process!!.outputStream)

        try {
            os!!.write(command.bytes)
            os!!.writeBytes("\n")
            os!!.flush()

            os!!.writeBytes("exit\n")
            os!!.close()

            if (timeout > 0) {
                Thread(Runnable {

                    try {
                        Thread.sleep(timeout.toLong())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        val ret = process!!.exitValue()
                        i("Return Code is $ret")
                    } catch (e: IllegalThreadStateException) {
                        e.printStackTrace()
                        process!!.destroy()
                    }

                }).start()
            }

            /**
             * 处理 input 流
             */
            val inputThread = Thread(Runnable {
                var line: String
                val writeLock = lock.writeLock()
                try {
                    line = successResult!!.readLine()
                    while (line != null) {
                        line += "\n"
                        writeLock.lock()
                        result.append(line)
                        writeLock.unlock()
                        line = successResult!!.readLine()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        successResult!!.close()
                    } catch (e: Exception) {
                    }
                }
            })
            inputThread.start()


            /**
             * 处理 error 流
             */
            val errorThread = Thread(Runnable {
                var line: String
                val writeLock = lock.writeLock()
                try {
                    line = errorResult!!.readLine()
                    while (line != null) {
                        line += "\n"
                        writeLock.lock()
                        result.append(line)
                        writeLock.unlock()
                        line = errorResult!!.readLine()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        errorResult!!.close()
                    } catch (e: Exception) {
                    }
                }
            })
            errorThread.start()

            val thread = Thread(Runnable {
                try {
                    inputThread.join()
                    errorThread.join()
                    process!!.waitFor()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    running = false
                }
            })
            thread.start()

            if (sync) {
                thread.join()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

}