package com.ninjasquad.springmockk

import com.ninjasquad.springmockk.example.ExampleService
import com.ninjasquad.springmockk.example.ExampleServiceCaller
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Integration test for [MockkClear]
 * @author JB Nizet
 */
@ExtendWith(SpringExtension::class)
class MockkClearIntegrationTests {
    @MockkBean
    private lateinit var exampleService: ExampleService

    /**
     * Test case for Issue #27. It fails if MockkClear uses a HashMap or a ConcurrentHashMap
     * @see [27](https://github.com/Ninja-Squad/springmockk/issues/27)
     */
    @Test
    fun test() {
        val bean = ExampleServiceCaller(exampleService)
        every { exampleService.greeting() } returns "test"
        bean.sayGreeting()
        verify { exampleService.greeting() }
        confirmVerified(exampleService) // this is what fails when using a HashMap, because hashCode() is considered not verified
    }
}
