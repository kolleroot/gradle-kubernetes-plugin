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
