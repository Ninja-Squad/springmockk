package com.ninjasquad.springmockk

import com.ninjasquad.springmockk.example.ExampleService
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.reflect.Proxy


/**
 * Tests for [@SpykBean][SpykBean] with a JDK proxy.
 *
 * @author Andy Wilkinson
 * @author JB Nizet
 */
@ExtendWith(SpringExtension::class)
class SpyBeanWithJdkProxyTests {
    @Autowired
    private lateinit var service: ExampleService

    @SpykBean
    private lateinit var repository: ExampleRepository

    /**
     * This test currently fails, because the issue [5837](https://github.com/spring-projects/spring-boot/issues/5837)
     * also exists for MockK. Unfortunately, I have no clear idea of how to fix it.
    @Test
    fun jdkProxyCanBeSpied() {
        val example = service.find("id")
        assertThat(example.id).isEqualTo("id")
        verify { repository.find("id") }
    }
    */

    @Configuration(proxyBeanMethods = false)
    @Import(ExampleService::class)
    class Config {
        @Bean
        fun dateService(): ExampleRepository {
            return Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(ExampleRepository::class.java)
            ) { _, _, args -> Example(args[0] as String) } as ExampleRepository
        }
    }

    class ExampleService(private val repository: ExampleRepository) {
        fun find(id: String): Example {
            return repository.find(id)
        }
    }

    interface ExampleRepository {
        fun find(id: String): Example
    }

    class Example(val id: String)
}
