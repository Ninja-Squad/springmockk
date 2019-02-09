# SpringMockK

Support for Spring Boot integration tests written in Kotlin using [MockK](https://mockk.io/) instead of Mockito.
 
Spring Boot provides `@MockBean` and `@SpyBean` annotations for integration tests, which create mock/spy beans using Mockito.

This project provides equivalent annotations `MockkBean` and `SpykBean` to do the exact same thing with MockK.

## Principle

All the Mockito-specific classes of the spring-boot-test library, including the automated tests, have been cloned, translated to Kotlin, and adapted to MockK.

This library thus provides the same functionality as the standard Mockito-based Spring beans.

For example (using JUnit 5, but you can of course also use JUnit 4):

```kotlin
@ExtendWith(SpringExtension.class)
@WebMvcTest
class GreetingControllerTest {
    @MockkBean
    private lateinit var greetingService: GreetingService
    
    @Autowired
    private lateinit var controller: GreetingController
    
    @Test
    fun `should greet by delegating to the greeting service`() {
        every { greetingService.greet("John") } returns "Hi John"
        
        assertThat(controller.greet("John")).isEqualTo("Hi John")
        verify { greetingService.greet("John") }
    }
}
```

## Differences with Mockito

 - the MockK defaults are used, which means that mocks created by the annotations are strict (i.e. not relaxed) by default. But [you can configure MockK](https://mockk.io/#settings-file) to use different defaults globally
 - the created mocks can't be serializable as they can be with Mockito (AFAIK, MockK doesn't support that feature)
 - mocks created by the `@MockkBean` annotation can be relaxed by setting the `relaxed` attribute to true. 
   They can't be `relaxUnitFun` yet due to [missing support in the MockK DSL](https://github.com/mockk/mockk/issues/227) at the moment

## Limitations
 - the [issue 5837](https://github.com/spring-projects/spring-boot/issues/5837), which has been fixed for Mockito spies using Mockito-specific features, also exists with MockK, and hasn't been fixed yet. 
   If you have a good idea, please tell!
 - [this is not an official Spring Boot project](https://github.com/spring-projects/spring-boot/issues/15749), so it might not work out of the box for newest versions if backwards incompatible changes are introduced in Spring Boot. 
 Please file issues if you find problems.
 - annotations are looked up on fields, and not on properties (for now). 
   This doesn't matter much until you use a custom qualifier annotation.
   In that case, make sure that it targets fields and not properties, or use `@field:YourQualifier` to apply it on your beans.

## How to build

```
  ./gradlew build
```

## How to use

Just add the jar to your test classpath, and start using the annotations.
