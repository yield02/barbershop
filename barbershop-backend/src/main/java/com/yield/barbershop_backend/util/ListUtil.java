package com.yield.barbershop_backend.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ListUtil {
    

    public static <T> List<T> difference(List<T> list1, List<T> list2) {
        Set<T> set2 = new HashSet<T>(list2);
        return list1.stream().filter(x -> !set2.contains(x)).collect(Collectors.toList());
    }

}
