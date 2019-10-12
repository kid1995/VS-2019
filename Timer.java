import java.util.Date;

public class Timer {

    public static long getCurrentTimeStamp(){
        return System.currentTimeMillis();
    }

    public static long calcDeactiveTime(long lastActiveTime){
        return (System.currentTimeMillis() -lastActiveTime)/(1000*60);
    }
}
