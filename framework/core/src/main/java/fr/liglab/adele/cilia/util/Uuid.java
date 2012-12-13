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

package fr.liglab.adele.cilia.util;

import java.util.Random;
import java.util.regex.Pattern;

public class Uuid {
	private final long msb;
	private final long lsb;
	private static final Random generator = new Random(System.currentTimeMillis());
	
	/** Regex pattern for a UUID */
	private static final Pattern uuidPattern =
		Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");


	private Uuid(byte[] data) {
		long msb = 0;
		long lsb = 0;

		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (data[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (data[i] & 0xff);
		this.msb = msb;
		this.lsb = lsb;
	}
	
	public static Uuid generate() {
		Long ng = new Long(generator.nextLong());
		byte[] randomBytes = new byte[16];
		generator.nextBytes(randomBytes) ;
		randomBytes[6] &= 0x0f; /* clear version */
		randomBytes[6] |= 0x40; /* set to version 4 */
		randomBytes[8] &= 0x3f; /* clear variant */
		randomBytes[8] |= 0x80; /* set to IETF variant */
		return new Uuid(randomBytes);
	}
	
	public String toString() {
		return (digits(msb >> 32, 8) + "-" + digits(msb >> 16, 4) + "-"
				+ digits(msb, 4) + "-" + digits(lsb >> 48, 4) + "-" + digits(
				lsb, 12));
	}

	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}
	
    /**
	 * Returns true if and only if uuid matches the pattern for valid UUIDs.
	 *
	 * @param uuid the <code>String</code> to check.
	 * @return <code>true</code> if and only if the specified.
	 * <code>String</code> matches the pattern; <code>false</code> otherwise.
	 */
	public static boolean isValidUUID(String uuid) {
		return uuidPattern.matcher(uuid).matches();
    }

    /**
	 * Returns true if and only if uuidURN matches the pattern for valid UUID URNs.
	 *
	 * @param uuidURN the <code>String</code> to check.
	 * @return <code>true</code> if and only if the specified.
	 * <code>String</code> matches the pattern; <code>false</code> otherwise.
	 */
	public static boolean isValidUUIDURN(String uuidURN) {
		return uuidURN.startsWith("urn:uuid:") && isValidUUID(uuidURN.substring(9));
    }

}
