package si.uni_lj.fri.taskyapp.data;

/**
 * Created by urgas9 on 26-Apr-16, OpenHours.com
 */
public class CalendarData {

    private String name;
    private String isAllDay;
    private String duration;

    public CalendarData(String name, String duration, String isAllDay){
        super();
        this.name = name;
        this.duration = duration;
        this.isAllDay = isAllDay;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsAllDay() {
        return isAllDay;
    }

    public void setIsAllDay(String isAllDay) {
        this.isAllDay = isAllDay;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "CalendarData{" +
                "name='" + name + '\'' +
                ", isAllDay='" + isAllDay + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
