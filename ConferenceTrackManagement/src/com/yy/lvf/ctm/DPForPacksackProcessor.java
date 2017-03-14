package com.yy.lvf.ctm;

import com.yy.lvf.ctm.Session.PacksackParam;

public class DPForPacksackProcessor {

	public static boolean[] packsackDp(PacksackParam param) {
		if(param==null){
			return null;
		}
		int[][] opt = new int[param.getGoodsNumber() + 1][param.getPacksackWeight() + 1];
		boolean[][] sol = new boolean[param.getGoodsNumber() + 1][param.getPacksackWeight() + 1];

		for (int n = 1; n <= param.getGoodsNumber(); n++) {
			for (int w = 1; w <= param.getPacksackWeight(); w++) {
				int option1 = opt[n - 1][w];
				int option2 = Integer.MIN_VALUE;
				if (param.getGoodsWeightArr()[n] <= w){
					// 注意opt[n-1]
					option2 = param.getGoodsProfitArr()[n] + opt[n - 1][w - param.getGoodsWeightArr()[n]];
				}
				opt[n][w] = Math.max(option1, option2);
				sol[n][w] = (option2 > option1);
			}
		}
		boolean[] take = new boolean[param.getGoodsNumber() + 1];
		for (int n = param.getGoodsNumber(), w = param.getPacksackWeight(); n > 0; n--) {
			if (sol[n][w]) {
				take[n] = true;
				w = w - param.getGoodsWeightArr()[n];
			} else {
				take[n] = false;
			}
		}
		return take;
	}

}
