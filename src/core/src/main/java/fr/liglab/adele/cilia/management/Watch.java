/* Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.cilia.management;

import java.util.Calendar;
import java.util.TimeZone;

public class Watch implements java.lang.Comparable {
	private static final long NANO_TO_MS = 1000000L;
	private static TimeZone timeZone = TimeZone.getDefault();

	private final String id;
	private final long nano;
	private long stopTick;
	private boolean isRunning ;
	private Object _lock = new Object();

	public Watch() {
		this(null);
	}

	public Watch(String id) {
		this.nano = System.nanoTime();
		this.id = id;
		this.isRunning=true ;
	}

	public String getId() {
		return this.id;
	}

	public long getElapsedTicks() {
		long value;
		synchronized(_lock) {
		if (isRunning)
			value = System.nanoTime();
		else
			value = stopTick;
		}
		return (value - nano);
	}

	public long getStartTicks() {
		return nano;
	}

	public  void stop() {
		synchronized(_lock) {
		this.stopTick = System.nanoTime();
		this.isRunning=false ;
		}
	}

	public static long getCurrentTicks() {
		return System.nanoTime();
	}

	public static long fromTicksToMs(long ticks) {
		return (ticks / NANO_TO_MS);
	}

	public static String formatDateIso8601(long ticksCounts) {
		StringBuilder retVal = new StringBuilder(23);

		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTimeInMillis(fromTicksToMs(ticksCounts));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int ms = cal.get(Calendar.MILLISECOND);

		retVal.append(year).append('-');
		padTwoDigits(month + 1, retVal).append('-');
		padTwoDigits(day, retVal).append(' ');
		padTwoDigits(hour, retVal).append(':');
		padTwoDigits(minute, retVal).append(':');
		padTwoDigits(second, retVal).append('.');
		return padThreeDigits(ms, retVal).toString();
	}

	private static StringBuilder padTwoDigits(int i, StringBuilder toAppend) {
		if (i < 10) {
			toAppend.append("0");
		}
		return toAppend.append(i);
	}

	private static StringBuilder padThreeDigits(int i, StringBuilder toAppend) {
		if (i < 10) {
			toAppend.append("00");
		} else {
			if ((i > 10) && (i < 100)) {
				toAppend.append("0");
			}
		}
		return toAppend.append(i);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (id != null) {
			sb.append("id [").append(id).append("], ");
		}
		sb.append(formatDateIso8601(getStartTicks()));
		sb.append(" ,ticks [").append(nano).append("]");
		return sb.toString();
	}

	public int compareTo(Object other) {
		long t1 = ((Watch) other).getCurrentTicks();
		long t2 = this.getCurrentTicks();
		if (t1 > t2)
			return -1;
		else if (t1 == t2)
			return 0;
		else
			return 1;
	}

}