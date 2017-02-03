/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kolleroot.gradle.kubernetes.standalone

/**
 * Created by stefan on 12.12.16.
 */
class MethodCallTest {
    static void main(String[] args) {

        Closure c = { del ->
            test('asdf')
            def a = 'asdf'

            def i = {
                test(a)
            }

            i.delegate = del
            i.resolveStrategy = Closure.DELEGATE_FIRST

            i()
        }

        c.delegate = new OuterClassWithMethod() {
            @Override
            void run() {
            }
        }
        def inner = new InnerClassWithMethod() {
            @Override
            void run() {
            }

            @Override
            void test(Object asdf) {
                println('Inner' + asdf)
            }
        }

        c(inner)

        def o = new OuterClassWithMethod() {
            @Override
            void run() {
                test('asdf')

                def i = new InnerClassWithMethod() {
                    @Override
                    void run() {
                        test('asdf')
                    }

                    @Override
                    void test(Object o) {
                        println('Inner' + o)
                    }
                }

                i.run()
            }
        }

        o.run()
    }

    static abstract class OuterClassWithMethod {
        abstract void run()

        void test(String s) {
            println('Outer' + s)
        }
    }

    interface InnerClassWithMethod {
        abstract void run()

        void test(Object o);
    }
}
