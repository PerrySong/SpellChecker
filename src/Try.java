public class Try {

    /*
     * Complete the function below.
     */
    static boolean alert(int[] inputs, int windowSize, float allowedIncrease) {

        int numOfWins = inputs.length - windowSize + 1;
        double curAve;
        double prevAve = oneWindowAverage(inputs, 0, windowSize);
        double maxWin = Double.MIN_VALUE;
        double sum = 0;
        double toltalAverage = 0;
        for(int i = 0; i < numOfWins; i++) {
            curAve = oneWindowAverage(inputs, i, windowSize);
            //If the current average greater than the upper bound
            if(curAve - prevAve > prevAve * allowedIncrease) return true;
            //Find the maxium window average value
            if(curAve > maxWin) maxWin = curAve;
            sum += curAve;
        }
        toltalAverage = sum / numOfWins;
        if(maxWin > toltalAverage * allowedIncrease) return true;
        return false;
    }


    static double oneWindowAverage(int[] inputs, int start, int windowSize) {
        double sum = 0;
        for(int i = start; i < start + windowSize; i++) {
            sum += inputs[i];
        }
        return sum / windowSize;
    }

    public static void main(String[] args) {

        int[] test = new int[6];
        test[0] = 5;
        test[1] = 1;
        test[2] = 2;
        test[3] = 100;
        test[4] = 2;
        test[5] = 2;
        System.out.println(alert(test, 3, (float)1.5));

    }
}


//    The Alerter is a simple monitoring tool, intended to help detect increases in response time for some process. It does that
//        by computing a few statistics about the process across a 'window' of a certain number of runs, and alerting (returning true)
//        if certain thresholds are met.
//
//        It takes the following parameters:
//        - inputs: A list of integer times for the process. This list may be very long
//        - window size: how many runs long a window is, as an integer
//        - allowedIncrease: how far over 'average' a window or value is allowed to be, as a percent. This is represented as a decimal value based on one, so a 50% allowable increase would be represented as 1.5
//
//        Your Alerter should return true if either of the following conditions are met:
//        * Any value is more than the allowed increase above the window average in ALL windows in which it appears.
//        For example:
//        alert({1, 2, 100, 2, 2}, 3, 1.5) should alert: the value 100 appears in three windows, and in all cases is more than 50% over the average value
//        alert({1, 2, 4, 2, 2}, 3, 2) should not alert: the largest outlier is 4, and that value appears in a window with average value 2.6, less than 100% of that average
//        * Any window's average is more than the acceptable increase over any previous window's average value
//        For example:
//        alert({1,2,100,2,2}, 2, 2.5) should alert: Even though no individual value causes an alert, there is a window with average 1.5 and a later window with an average more than 2.5 times larger
//        Otherwise, you should return false.