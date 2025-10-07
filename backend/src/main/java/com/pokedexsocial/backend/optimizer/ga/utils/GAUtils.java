package com.pokedexsocial.backend.optimizer.ga.utils;

import java.util.ArrayList;
import java.util.Random;

public class GAUtils {

    /*@
      @ requires list != null;
      @ requires list.size() >= 0 && list.size() < Integer.MAX_VALUE;
      @ ensures list.size() == \old(list.size());
      @*/
    public static <T> void shuffle(ArrayList<T> list) {
        Random random = new Random();
        int n = list.size();
        int i = n - 1;

        // Non c`è bisogno di shuffle
        if(list.size() <= 1){
            return;
        }

        //@ loop_invariant 0 <= i && i < n;
        //@ loop_invariant list.size() == n;
        //@ decreases i;
        while (i > 0) {
            int j = random.nextInt(i + 1);

            T temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);

            i--;
        }
    }
}