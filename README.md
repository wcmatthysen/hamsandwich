hamsandwich
===========
This micro-library provides Java extensions to the [Hamcrest](http://code.google.com/p/hamcrest/) library to provide a convienient way of declaring and combining entity Matchers which can be used in either test or production code.

Installation
============
* Download a binary package from [here](http://code.google.com/p/hamsandwich/downloads/list). The Zip distribution contains all of the Javadoc, dependencies and source code for the project.

or:

* Check out the source code via git: `git clone git@github.com:wcmatthysen/hamsandwich.git` and build it manually. Prerequisites:
    * Java 1.6
    * Ant (we use v1.8) or Maven 2/3.
    * JUnit no dependencies version: junit-dep-*version*.jar - (we use v4.8.1)
    * Simply type `ant` or `ant build` at the command line in the "core" folder to build with Ant.
    * Alternatively, (if you have Maven installed) type `mvn package` to build, and / or `mvn install` to install in your local repository.

Maven
-----
To use hamsandwich with Maven 2/3, you must add the following repository to your project's `pom.xml` file:

```xml
<repository>
  <id>hamsandwich-repo</id>
  <name>hamsandwich repository on GitHub</name>
  <url>http://wcmatthysen.github.com/hamsandwich/repository/</url>
</repository>
```

Then, you can add the following dependency:

```xml
<dependency>
  <groupId>org.hamsandwich</groupId>
  <artifactId>hamsandwich</artifactId>
  <version>1.2</version>
</dependency>
```

Getting Started
===============
If you're not familiar with the concept of Matchers and why they're ace, then it's probably a good idea to start with the [Hamcrest Tutorial](http://code.google.com/p/hamcrest/source/browse/wiki/Tutorial.wiki).

To integrate HamSandwich into your project, the project Jar and contents of the Lib directory are required as dependencies. Note that as an older, incompatible version of Hamcrest (v1.1) is currently bundled with JUnit as standard, there may be classpath clashes if the full JUnit Jar is used.

Say we have an simple object:

```java
package org.hamsandwich.example.wiki;

public class Person {

    private final String name;
    public final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }
}
```

... and a test for that class:

```java
package org.hamsandwich.example.wiki;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PersonTest {

    @Test
    public void detailsStoredProperly() throws Exception {
        Person dave = new Person("Dave", 33);
        Person alan = new Person("Alan", 65);

        assertThat(dave.getName(), is(equalTo("Dave")));
        assertThat(dave.age+10, is(equalTo(43)));
        assertThat(dave.age, is(lessThan(alan.age)));
    }
}
```

Each of the assertions needs to be declared on a separate line in the test. This approach does give us a nice stack trace if the test fails with the line number so you can easily determine which assertion failed, along with a message from the matcher indicating what went wrong.

The downside of this approach is that there is no way to easily group the various assertions into reusable modules, save for extracting a method which contains the 3 assertThat() calls, which would end up something like:

```java
package org.hamsandwich.example.wiki;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PersonTest {

    @Test
    public void detailsStoredProperly() throws Exception {
        Person dave = new Person("Dave", 33);
        Person alan = new Person("Alan", 65);

        assertThePersonDetailsAgainst(dave, alan, "Dave", 43);
    }

    private void assertThePersonDetailsAgainst(Person actual, Person elderPerson, String expectedName, int expectedAge) {
        assertThat(actual.getName(), is(equalTo(expectedName)));
        assertThat(actual.age+10, is(equalTo(expectedAge)));
        assertThat(actual.age, is(lessThan(elderPerson.age)));
    }
}
```

This is a quite ugly and ends up with all of the expected details being passed around, along with the object under test.

Alternatively...
----------------
Using a HamSandwich Matcher you can extract and group the common assertions, providing reuse in a Functional Style without hampering readability. To do so:

* Decide on the common conditions that you want to assert on. These are typically fields, but you can build up less granular, more complex Matchers by composition. In the above example, we will use the name and age fields of the Person class.

* Declare a factory method for each condition annotated with @HamSandwichFactory which will return a subclass of the AdaptingMatcher. This class requires the get() method to be implemented, which describes how to translate the input object (Person), into the output object (name -> String, age -> Integer). Alternatively, you can use a factory method to create a function replaying matcher (as done below for getName()). In this example we have grouped these methods onto a single utility class:

```java
package org.hamsandwich.example.wiki;

import org.hamcrest.Matcher;
import org.hamsandwich.core.AdaptingMatcher;
import org.hamsandwich.core.CannotAdaptException;
import org.hamsandwich.core.HamSandwichFactory;

import static org.hamsandwich.core.ReplayMatcher.on;
import static org.hamsandwich.core.ReplayMatcher.replayMatcher;

public class PersonMatchers {

    @HamSandwichFactory
    public static Matcher<Person> name(Matcher<String>... nameMatchers) {
        return replayMatcher(on(Person.class).getName(), nameMatchers);
    }

    @HamSandwichFactory
    public static Matcher<Person> ageInADecade(Matcher<? super Integer>... ageMatchers) {
        return new AdaptingMatcher<Person, Integer>(ageMatchers) {
            @Override
            public Integer get(Person in) throws CannotAdaptException {
                return in.age + 10;
            }
        };
    }
}
```

Note that the generics definition above involving super definitions Matcher<Integer> is required at the moment because of an issue with the version (1.3RC1) of Hamcrest that ships with HamSandwich. This should be fixed soon in v1.3 as soon as it goes gold. In addition, the generics used above can be omitted from the signatures of these methods if required or desired. However, doing so will generate compiler warnings in your IDE.

* You can now rewrite the test in order to use the above matchers:

```java
package org.hamsandwich.example.wiki;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamsandwich.example.wiki.PersonMatchers.ageInADecade;
import static org.hamsandwich.example.wiki.PersonMatchers.name;

public class PersonTest {

    @Test
    public void detailsStoredProperly() throws Exception {
        Person dave = new Person("Dave", 33);
        Person alan = new Person("Alan", 65);

        assertThat(dave,
                allOf(
                        name(is(equalTo("Dave"))),
                        ageInADecade(is(equalTo(43)), is(lessThan(alan.age)))
                )
        );
    }
}
```

We now have a single call to assertThat() passing in the dave instance, and have also combined the assertions for age into a single variable which itself can be extracted and reused.

"But what about the knowing where the test failed?", I hear you cry...
----------------------------------------------------------------------
Well, firstly, the original version of the test lets you know the exact line number of the assertion failure, but that is actually duplicate information - the message supplied by the matcher should give the actual reason that the test failed. Combining Matchers into a single assertThat() call removes the duplication, but retains the failure reason information by utilising the SelfDescribing feature of Hamcrest. The name of the condition (i.e. the name of the annotated Factory method) is used instead. So, if we change the test to deliberately fail:

```java
package org.hamsandwich.example.wiki;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamsandwich.example.wiki.PersonMatchers.ageInADecade;
import static org.hamsandwich.example.wiki.PersonMatchers.name;

public class PersonTest {

    @Test
    public void detailsStoredProperly() throws Exception {
        Person dave = new Person("Dave", 33);
        Person alan = new Person("Alan", 65);

        assertThat(dave,
                allOf(
                        name(is(equalTo("Mark"))),
                        ageInADecade(is(equalTo(53)), is(lessThan(alan.age)))
                )
        );
    }
}
```

... it now fails with the error message:

```
java.lang.AssertionError: 
Expected: ([Person where name (is "Mark")] and [a Person where ageInADecade (is <53> and is a value less than <65>)])
     but: [Person where name (is "Mark")] is "Mark" was "Dave"
...
```

And Finally...
==============
It is also possible (nay, desirable!) to use these Matchers in production code as a filtering mechanic and there is an example of this usage included in the Zip distribution. An interesting benefit of the Hamcrest SelfDescribing functionality is that these Matchers generate human-readable descriptions from the describeTo() and toString() methods.

The library will also hugely benefit from the introduction of Closures in Java7 (whenever that turns up), as the implementation of the get() method on AdaptingMatcher can be replaced by a Closure passed into the constructor, thus cleaning up a lot of code in the Factory methods.

Have a play. Let us know what you think.

:)

License
=======
BSD License

Copyright (c) 2000-2010, Ham Sandwich project owners
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of
conditions and the following disclaimer. Redistributions in binary form must reproduce
the above copyright notice, this list of conditions and the following disclaimer in
the documentation and/or other materials provided with the distribution.

Neither the name of Hamcrest nor the names of its contributors may be used to endorse
or promote products derived from this software without specific prior written
permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.
