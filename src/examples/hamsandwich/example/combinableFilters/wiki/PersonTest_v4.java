package hamsandwich.example.combinableFilters.wiki;

import org.junit.Ignore;
import org.junit.Test;

import static hamsandwich.example.combinableFilters.wiki.PersonMatchers.age;
import static hamsandwich.example.combinableFilters.wiki.PersonMatchers.name;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PersonTest_v4 {

    @Test
    @Ignore
    public void detailsStoredProperly() throws Exception {
        Person dave = new Person("Dave", 33);
        Person alan = new Person("Alan", 65);

        assertThat(dave,
                allOf(
                        name(is(equalTo("Mark"))),
                        age(is(equalTo(33)), is(lessThan(alan.age)))
                )
        );
    }
}