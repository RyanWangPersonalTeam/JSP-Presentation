package wzy.jsp.simplest.demo.component;


import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Use this class to do convert operation between minute unit and count unit, minimum interval is 1 minute
 */
public class DateTimeConverter {

    //Counting at intervals of countValueIntervalInMin minutes
    private int countValueIntervalInMin =5;
    private String dateTimePattern="yyyy-MM-dd HH:mm:ss";

    public DateTimeConverter(){}
    public DateTimeConverter(int countValueIntervalInMin){this.countValueIntervalInMin = countValueIntervalInMin;}
    public DateTimeConverter(int countValueIntervalInMin, String dateTimePattern) throws Exception {
        if(countValueIntervalInMin <1){
            throw new Exception("countingIntervalInMin must bigger than zero!");
        }
        this.countValueIntervalInMin = countValueIntervalInMin;
        this.dateTimePattern=dateTimePattern;
    }

    //Convert the duration in minute unit to count value
    //e.g. minuteValue=10, countingIntervalInMin=5, then return 2
    public int MinToCountValueNum(int minuteValue){
        return minuteValue/this.countValueIntervalInMin;
    }

    public int CountValueToMin(int counter){
        return counter* countValueIntervalInMin;
    }

    //Calculate the count value between two dates
    //e.g.  date1="2019-01-01 12:30:00", date2="2019-01-01 12:40:00", countingIntervalInMin = 5
    //      than return 2
    public int CalculateCountValueBetweenDates(DateTime date1, DateTime date2){
        Period period = new Period(date1, date2, PeriodType.minutes());
        int m = period.getMinutes();
        return m/ countValueIntervalInMin;
    }


    public int CalculateCountValueBetweenDates(String date1, String date2){
        DateTimeFormatter format = DateTimeFormat.forPattern(this.dateTimePattern);
        DateTime dateTimeStart = format.parseDateTime(date1);
        DateTime dateTimeEnd = format.parseDateTime(date2);
        return this.CalculateCountValueBetweenDates(dateTimeStart,dateTimeEnd);
    }

    //Calculate the result date from startDate
    //e.g. startDate="2019-01-01 12:00:00", countValue=2, then return "2019-01-01 12:10:00"
    public DateTime CalculateDateFromCountValue(DateTime startDate, int countValue){
        int minutes=countValue*countValueIntervalInMin;
        DateTime resultDate=startDate.plusMinutes(minutes);
        return resultDate;
    }


    public String CalculateDateFromCountValue(String startDate, int countValue){
        DateTimeFormatter format = DateTimeFormat.forPattern(this.dateTimePattern);
        DateTime dateTimeStart = format.parseDateTime(startDate);
        DateTime resultDate=this.CalculateDateFromCountValue(dateTimeStart,countValue);
        return resultDate.toString(this.dateTimePattern);
    }


    public int getCountValueIntervalInMin() {
        return countValueIntervalInMin;
    }

    public void setCountValueIntervalInMin(int countValueIntervalInMin) {
        this.countValueIntervalInMin = countValueIntervalInMin;
    }

    public String getDateTimePattern() {
        return dateTimePattern;
    }

    public void setDateTimePattern(String dateTimePattern) {
        this.dateTimePattern = dateTimePattern;
    }



}
