package me.juhezi.eternal.other

/**
 * 支持同步、异步
 *
 *
 *
 */
class EShell {

    var callback: EShellCallback? = null

    /**
     * 获取输出结果（全部的）
     */
    fun getResult(): ShellResult {
        return ShellResult("to do")
    }

    /**
     * 获取命令行返回值
     */
    fun getReturnCode(): Int {
        return 0
    }

    /**
     * 耗时
     * 单位：微妙
     */
    fun getCostTimeMs(): Long {
        return 0
    }

    /**
     * 同步执行
     */
    fun run(command: String, timeoutMs: Long = -1) {

    }

    /**
     * 异步执行
     */
    fun runAsync(command: String, timeoutMs: Long = -1) {

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
    STDERR  // 标准错误
}