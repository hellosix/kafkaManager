package com.lzz.consumerservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by gl49 on 2018/3/16.
 */
public class TestJar {
    public static void main(String args) throws MalformedURLException, ClassNotFoundException {
        URL url1 = new URL("file:D:/work/java/kafka-tool/src/main/resources/lib/test-hello.jar");
        URLClassLoader myClassLoader1 = new URLClassLoader(new URL[] { url1 }, Thread.currentThread()
                .getContextClassLoader());
        Class<?> myClass = myClassLoader1.loadClass("com.newegg.Hello");
        //AbstractAction action1 = (AbstractAction) myClass.newInstance();
        //String str1 = action1.action();
        //System.out.println(str1);
    }
}
