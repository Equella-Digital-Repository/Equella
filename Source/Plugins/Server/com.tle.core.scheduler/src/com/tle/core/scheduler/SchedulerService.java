package com.tle.core.scheduler;

import java.util.List;

import com.google.common.base.Preconditions;
import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

@SuppressWarnings("nls")
public interface SchedulerService
{
	Schedules getSchedules();

	void setSchedules(Schedules schedules);

	Schedules getServerSchedules();

	void setServerSchedules(Schedules schedules);

	/**
	 * Daily schedule defaults to everyday at 11pm. Weekly schedule defaults to
	 * every Saturday at 10am. Defaults should remain on this class, since
	 * migrations do not add these defaults to the DB.
	 */
	public class Schedules implements ConfigurationProperties
	{
		@Property(key = "schedules.daily.hour")
		private int dailyTaskHour = 23;
		@Property(key = "schedules.weekly.hour")
		private int weeklyTaskHour = 10;
		@Property(key = "schedules.weekly.day")
		private int weeklyTaskDay = 6;

		/**
		 * @return The hour to start daily tasks on, 0-23.
		 */
		public int getDailyTaskHour()
		{
			return dailyTaskHour;
		}

		/**
		 * @param h The hour to start daily tasks on, 0-23.
		 */
		public void setDailyTaskHour(int h)
		{
			Preconditions.checkArgument(0 <= h && h <= 23, "Hour must be between 0 and 23 inclusive");
			this.dailyTaskHour = h;
		}

		/**
		 * @return The hour to start weekly tasks on, 0-23.
		 */
		public int getWeeklyTaskHour()
		{
			return weeklyTaskHour;
		}

		/**
		 * @param h The hour to start weekly tasks on, 0-23.
		 */
		public void setWeeklyTaskHour(int h)
		{
			Preconditions.checkArgument(0 <= h && h <= 23, "Hour must be between 0 and 23 inclusive");
			this.weeklyTaskHour = h;
		}

		/**
		 * @return The day to start weekly tasks on, 0-6 starting Sunday.
		 */
		public int getWeeklyTaskDay()
		{
			return weeklyTaskDay;
		}

		/**
		 * @param d The day to start weekly tasks on, 0-6 starting Sunday.
		 */
		public void setWeeklyTaskDay(int d)
		{
			Preconditions.checkArgument(0 <= d && d <= 6, "Day must be between 0 and 6 inclusive");
			this.weeklyTaskDay = d;
		}
	}

	List<String> getAllSchedulerIds();

	void executeTaskNow(String extId);
}
