package com.android.lvf.demo.algorithm.substate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 烽 on 2017.2.12.
 */

public class LeastCoin {
    public Map<Integer, Integer> mSubStates = new LinkedHashMap<>();

    public void reachSum(int sum, int[] conis) {
        for (int i = 0; i <= sum; i++) {
            if (i == 0 && !mSubStates.containsKey(i)) {
                mSubStates.put(i, 0);
            }
            int min = -1;
            for (int coin : conis) {
                if (i >= coin) {
                    if (!mSubStates.containsKey(i - coin)) {
                        continue;
                    }
                    int current = mSubStates.get(i - coin) + 1;
                    if (min == -1) {
                        min = current;
                    }
                    if (current <= min) {
                        min = current;
                    }
                    mSubStates.put(i, current);
                }
            }
        }
    }

    public static class SubState{
        public List<Integer> mChoices = new ArrayList<>();

    }

}
