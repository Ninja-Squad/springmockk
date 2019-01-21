package com.ninjasquad.springmockk

import java.util.concurrent.ConcurrentHashMap

/**
 * Clear strategy used on a mockk bean, applied to a mock via the
 * [MockkBean] annotation.
 *
 * @author Phillip Webb
 * @author JB Nizet
 * @since 1.4.0
 * @see ClearMocksTestExecutionListener
 */
enum class MockkClear {
    /**
     * Reset the mock before the test method runs.
     */
    BEFORE,

    /**
     * Reset the mock after the test method runs.
     */
    AFTER,

    /**
     * Don't reset the mock.
     */
    NONE;

    companion object {
        private val clearModesByMock = ConcurrentHashMap<Any, MockkClear>()

        internal fun set(mock: Any, clear: MockkClear) {
            require(mock.isMock) { "Only mocks can be cleared" }
            clearModesByMock.put(mock, clear)
        }

        /**
         * Get the [MockkClear] associated with the given mock.
         * @param mock the source mock
         * @return the clear type
         */
        fun get(mock: Any): MockkClear {
            return clearModesByMock[mock] ?: NONE
        }
    }
}

fun <T: Any> T.clear(clear: MockkClear): T {
    MockkClear.set(this, clear)
    return this
}
