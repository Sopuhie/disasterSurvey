package com.santech.framework.utils.component.cron;

public class CronSchedule {
	// 注意周和日 不能同时设定
	//Calendar 的月数0-11 0是一月


	//每隔X天运行 ruse=true
	//隔多少小时执行一次 ruse=false
	public static String getCronSchedule(int x, boolean ruse, String startTime){
			if(x<=0)return null;
			String time=null;
			String[] s = startTime.split(":");
			if(ruse) {//隔几天
				time= String.format("%s %s %s %s %s %s",s[2],s[1],s[0],"1/"+ x,"*","?");
			}else {//隔小时
				if(x>12)return null;
				time= String.format("%s %s %s %s %s %s",s[2],s[1],x
						,"*","*","?");
			}
		return time;
		
	}

	//指定星期几执行：1-7 ，1代表星期日 2-7星期一到星期六
	public static String getCronSchedule(int[] weekSum, String startTime) {
			String[] s = startTime.split(":");
			String week="";
			if(weekSum.length==0||weekSum==null)return null;
			for (int sun : weekSum) {
				if(sun>7||sun<0)return null;
				week+=sun+",";
			}
			week = week.substring(0,week.length()-1);
			String time= String.format("%s %s %s %s %s %s",s[2],s[1],s[0],"?","*",week);//"0 0 0 ? * "+week+" *"
			return time;
	}
	//指定每个月的第几天 可以选择多个
	public static String getCronSchedule(int[] monthSum, int[] daySum, String startTime) {
		String[] s = startTime.split(":");
		String day="";
		String month="";
		if(daySum.length==0||daySum==null||monthSum.length==0||monthSum==null)return null;
		for (int i = 0; i < daySum.length; i++) {
			if(i<0||i>31)return null;
			day+=daySum[i];
			if(i+1!=daySum.length)day+=",";
		}
		for (int m = 0; m < monthSum.length; m++) {
			if(m<0||m>12)return null;
			month+=monthSum[m];
			if(m+1!=monthSum.length)month+=",";
		}
		//month = month.substring(0,month.length()-1);
		String time= String.format("%s %s %s %s %s %s",s[2],s[1],s[0],day,month,"?");//"0 0 0 "+day+" "+month+" ? "
		return time;
		
	}

	public static void main(String[] args) {
		int [] a = {1};
		System.out.println(getCronSchedule(1,false,"14:15:00"));
	}
}
