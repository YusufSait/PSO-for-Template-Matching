package psofortemplatematching;

public class PSOUtility {
	public static int getMaxValue(double[] list) {
		int pos = 0;
		double maxValue = list[0];

		for(int i=0; i<list.length; i++) {
			if(list[i] > maxValue) {
				pos = i;
				maxValue = list[i];
			}
		}

		return pos;
	}
}
