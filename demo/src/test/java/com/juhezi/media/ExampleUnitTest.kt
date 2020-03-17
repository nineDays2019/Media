package com.juhezi.media

import com.juhezi.media.demo.Customer
import com.juhezi.media.demo.Producer
import org.junit.Test

import org.junit.Assert.*

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

}
