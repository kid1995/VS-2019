public class Timer {

    public static long getCurrentTimeStamp(){
        return System.currentTimeMillis();
    }

    public static long calcDeactiveTime(long lastActiveTime){
        System.out.println("Calc time : " +  (System.currentTimeMillis() -lastActiveTime)/1000);
        return (System.currentTimeMillis() -lastActiveTime)/1000;
    }
}
