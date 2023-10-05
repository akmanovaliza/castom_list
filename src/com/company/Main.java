package com.company;

import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        CustomCopyOnWriteArrayList list1 = new CustomCopyOnWriteArrayList();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        final Iterator<Integer> iterator1 = list1.iterator();
        list1.add(4);
        list1.remove(2);
        iterator1.forEachRemaining(System.out::println);
        System.out.println();
        final Iterator<Integer> iterator2 = list1.iterator();
        iterator2.forEachRemaining(System.out::println);
    }
    
}
