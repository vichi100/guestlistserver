import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Test {

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		//cal.setTime(new Date());//Set specific Date if you want to
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
		SimpleDateFormat simpleDateformat2 = new SimpleDateFormat("dd/MM/YYYY");

		for(int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
		    cal.set(Calendar.DAY_OF_WEEK, i);
		    Date date = cal.getTime();
		    System.out.println(simpleDateformat.format(date));//Returns Date
		    System.out.println(simpleDateformat2.format(date));
		}


	}

}
