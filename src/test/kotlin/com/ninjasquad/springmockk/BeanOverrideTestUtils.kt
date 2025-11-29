package com.ninjasquad.springmockk

import org.springframework.test.context.bean.override.BeanOverrideHandler


/**
 * Test utilities for Bean Overrides.
 *
 * @author Sam Brannen
 * @since 6.2.2
 */
object BeanOverrideTestUtils {

    fun findHandlers(testClass: Class<*>): List<BeanOverrideHandler> {
        return BeanOverrideHandler.forTestClass(testClass)
    }

    fun findAllHandlers(testClass: Class<*>): List<BeanOverrideHandler> {
        val findAllHandlers = BeanOverrideHandler::class.java.getDeclaredMethods().first { it.name == "findAllHandlers" }
        findAllHandlers.setAccessible(true)
        @Suppress("UNCHECKED_CAST")
        return findAllHandlers.invoke(null, testClass) as List<BeanOverrideHandler>
    }

}
