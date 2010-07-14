package org.hamsandwich.core;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamsandwich.core.ProxyingMatcher.on;
import static org.junit.Assert.assertThat;

public class ProxyingMatcherTest {

    @Test
    public void proxyingStuff() throws Exception {
        int magicNumber = 5;
        Matcher<ABean> aBeanMatcher = aMatcher(is(equalTo(magicNumber)));
        assertThat(new ABean(5), aBeanMatcher);
    }


    @HamSandwichFactory
    private static Matcher<ABean> aMatcher(Matcher<Integer>... matchers) {
        return ProxyingMatcher.proxy(on(ABean.class).getAnInt(), matchers);
    }

    private class ABean {
        private final int anInt;

        public ABean(int number) {
            anInt = number;
        }

        public int getAnInt() {
            return anInt;
        }
    }
}