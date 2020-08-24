package wzy.jsp.simplest.demo.domain.communication;

import wzy.jsp.simplest.demo.component.DateTimeConverter;

public class Task implements Comparable{
    public String Id;
    public String Name;
    public String StartTime;//"yyyy-MM-dd HH:mm:ss"
    public String EndTime;//"yyyy-MM-dd HH:mm:ss"
    public String Machine;
    public int Duration;//unit: minute

    @Override
    public int compareTo(Object o) {
        Task s = (Task) o;

        if (new DateTimeConverter().CalculateCountValueBetweenDates(this.StartTime,s.StartTime)<0) {
            return 1;
        }
        else {
            return -1;
        }
    }
}
